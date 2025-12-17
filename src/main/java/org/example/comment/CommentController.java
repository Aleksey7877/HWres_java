package org.example.comment;

import jakarta.validation.Valid;
import org.example.comment.dto.CreateCommentRequest;
import org.example.comment.dto.CreateReplyRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentRepository commentRepository;
    private final ContentExistenceService contentExistenceService;

    public CommentController(CommentRepository commentRepository,
                             ContentExistenceService contentExistenceService) {
        this.commentRepository = commentRepository;
        this.contentExistenceService = contentExistenceService;
    }

    @PostMapping
    public Comment createComment(@Valid @RequestBody CreateCommentRequest request,
                                 Authentication authentication) {

        boolean exists = contentExistenceService.exists(request.getContentType(), request.getContentId());
        if (!exists) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Контент не найден");
        }

        String username = authentication != null ? authentication.getName() : "anonymous";

        Comment comment = new Comment();
        comment.setContentType(request.getContentType());
        comment.setContentId(request.getContentId());
        comment.setText(request.getText());
        comment.setAuthorUsername(username);
        comment.setCreatedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());

        if (request.getMetadata() != null) {
            comment.setMetadata(new HashMap<>(request.getMetadata()));
        }

        return commentRepository.save(comment);
    }

    @GetMapping("/by-content")
    public List<Comment> getByContent(@RequestParam("type") ContentType type,
                                      @RequestParam("contentId") Long contentId) {
        return commentRepository.findByContentTypeAndContentIdOrderByCreatedAtAsc(type, contentId);
    }

    @GetMapping("/{id}")
    public Comment getById(@PathVariable String id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));
    }

    @PostMapping("/{id}/replies")
    public Comment addReply(@PathVariable String id,
                            @Valid @RequestBody CreateReplyRequest request,
                            Authentication authentication) {

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));

        String username = authentication != null ? authentication.getName() : "anonymous";

        CommentReply reply = new CommentReply();
        reply.setId(UUID.randomUUID().toString());
        reply.setAuthorUsername(username);
        reply.setText(request.getText());
        reply.setCreatedAt(Instant.now());
        if (request.getMetadata() != null) {
            reply.setMetadata(new HashMap<>(request.getMetadata()));
        }

        comment.getReplies().add(reply);
        comment.setUpdatedAt(Instant.now());

        return commentRepository.save(comment);
    }

    @PutMapping("/{id}")
    public Comment updateComment(@PathVariable String id,
                                 @RequestBody Map<String, String> body,
                                 Authentication authentication) {

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));

        String username = authentication != null ? authentication.getName() : null;

        if (username == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Требуется авторизация");
        }

        boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
        boolean isAuthor = username.equals(comment.getAuthorUsername());

        if (!isAuthor && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Редактировать может только автор или ADMIN");
        }

        String newText = body.get("text");
        if (newText != null && !newText.isBlank()) {
            comment.setText(newText);
            comment.setUpdatedAt(Instant.now());
        }

        return commentRepository.save(comment);
    }

    @DeleteMapping("/{id}")
    public void deleteComment(@PathVariable String id,
                              Authentication authentication) {

        String username = authentication != null ? authentication.getName() : null;
        if (username == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Требуется авторизация");
        }

        boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
        if (!isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Удалять может только ADMIN");
        }


        commentRepository.deleteById(id);
    }


    private boolean hasRole(Authentication authentication, String role) {
        if (authentication == null) return false;
        Collection<? extends GrantedAuthority> auths = authentication.getAuthorities();
        if (auths == null) return false;
        for (GrantedAuthority a : auths) {
            if (role.equals(a.getAuthority())) return true;
        }
        return false;
    }
}