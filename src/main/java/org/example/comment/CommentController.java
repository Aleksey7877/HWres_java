package org.example.comment;

import jakarta.validation.Valid;
import org.example.comment.dto.CreateCommentRequest;
import org.example.comment.dto.CreateReplyRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentRepository commentRepository;

    public CommentController(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @PostMapping
    public Comment createComment(@Valid @RequestBody CreateCommentRequest request,
                                 Authentication authentication) {
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
        return commentRepository
                .findByContentTypeAndContentIdOrderByCreatedAtAsc(type, contentId);
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
                                 @RequestBody Map<String, String> body) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));

        String newText = body.get("text");
        if (newText != null && !newText.isBlank()) {
            comment.setText(newText);
            comment.setUpdatedAt(Instant.now());
        }
        return commentRepository.save(comment);
    }

    @DeleteMapping("/{id}")
    public void deleteComment(@PathVariable String id) {
        if (!commentRepository.existsById(id)) {
            throw new CommentNotFoundException(id);
        }
        commentRepository.deleteById(id);
    }
}
