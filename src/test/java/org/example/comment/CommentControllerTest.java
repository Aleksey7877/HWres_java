package org.example.comment;

import org.example.comment.dto.CreateCommentRequest;
import org.example.comment.dto.CreateReplyRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ContentExistenceService contentExistenceService;

    @InjectMocks
    private CommentController commentController;

    @Test
    void createComment_savesAndReturns() {
        CreateCommentRequest req = new CreateCommentRequest();
        req.setContentType(ContentType.ARTICLE);
        req.setContentId(2L);
        req.setText("тест комментария");

        when(contentExistenceService.exists(ContentType.ARTICLE, 2L)).thenReturn(true);

        Comment saved = new Comment();
        saved.setId("id123");
        saved.setContentType(ContentType.ARTICLE);
        saved.setContentId(2L);
        saved.setAuthorUsername("user1");
        saved.setText("тест комментария");
        saved.setCreatedAt(Instant.now());
        saved.setUpdatedAt(Instant.now());

        when(commentRepository.save(any(Comment.class))).thenReturn(saved);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");

        Comment result = commentController.createComment(req, auth);

        assertThat(result.getId()).isEqualTo("id123");
        assertThat(result.getAuthorUsername()).isEqualTo("user1");

        verify(contentExistenceService).exists(ContentType.ARTICLE, 2L);
        verify(commentRepository).save(any(Comment.class));
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    void createComment_whenContentNotFound_throws404() {
        CreateCommentRequest req = new CreateCommentRequest();
        req.setContentType(ContentType.VIDEO);
        req.setContentId(999L);
        req.setText("nope");

        when(contentExistenceService.exists(ContentType.VIDEO, 999L)).thenReturn(false);

        Authentication auth = mock(Authentication.class);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> commentController.createComment(req, auth)
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(contentExistenceService).exists(ContentType.VIDEO, 999L);
        verifyNoInteractions(commentRepository);
    }

    @Test
    void getByContent_returnsSortedList() {
        Comment c1 = new Comment();
        c1.setId("c1");
        c1.setCreatedAt(Instant.parse("2025-12-10T10:00:00Z"));

        Comment c2 = new Comment();
        c2.setId("c2");
        c2.setCreatedAt(Instant.parse("2025-12-10T11:00:00Z"));

        when(commentRepository.findByContentTypeAndContentIdOrderByCreatedAtAsc(ContentType.ARTICLE, 2L))
                .thenReturn(List.of(c1, c2));

        List<Comment> result = commentController.getByContent(ContentType.ARTICLE, 2L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo("c1");
        assertThat(result.get(1).getId()).isEqualTo("c2");

        assertThat(result.get(0).getCreatedAt()).isBefore(result.get(1).getCreatedAt());

        verify(commentRepository).findByContentTypeAndContentIdOrderByCreatedAtAsc(ContentType.ARTICLE, 2L);
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    void addReply_whenCommentNotFound_throws() {
        when(commentRepository.findById("missing")).thenReturn(Optional.empty());

        CreateReplyRequest req = new CreateReplyRequest();
        req.setText("ответ");

        Authentication auth = mock(Authentication.class); // без стаба getName()

        assertThrows(
                CommentNotFoundException.class,
                () -> commentController.addReply("missing", req, auth)
        );

        verify(commentRepository).findById("missing");
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    void addReply_addsReplyToComment() {
        Comment comment = new Comment();
        comment.setId("c1");
        comment.setReplies(new ArrayList<>());

        when(commentRepository.findById("c1")).thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(comment);

        CreateReplyRequest req = new CreateReplyRequest();
        req.setText("reply");

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user2");

        Comment result = commentController.addReply("c1", req, auth);

        assertThat(result.getReplies()).hasSize(1);
        assertThat(result.getReplies().get(0).getText()).isEqualTo("reply");
        assertThat(result.getReplies().get(0).getAuthorUsername()).isEqualTo("user2");

        verify(commentRepository).findById("c1");
        verify(commentRepository).save(comment);
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    void updateComment_byAuthor_updatesText() {
        Comment c = new Comment();
        c.setId("c1");
        c.setAuthorUsername("user1");
        c.setText("old text");

        when(commentRepository.findById("c1")).thenReturn(Optional.of(c));
        when(commentRepository.save(c)).thenReturn(c);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");
        when(auth.getAuthorities()).thenReturn((Collection) List.of(new SimpleGrantedAuthority("ROLE_USER")));

        Comment result = commentController.updateComment("c1", Map.of("text", "new text"), auth);

        assertThat(result.getText()).isEqualTo("new text");
        verify(commentRepository).save(c);
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    void updateComment_byAnotherUser_notAdmin_forbidden() {
        Comment c = new Comment();
        c.setId("c1");
        c.setAuthorUsername("author");

        when(commentRepository.findById("c1")).thenReturn(Optional.of(c));

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("intruder");
        when(auth.getAuthorities()).thenReturn((Collection) List.of(new SimpleGrantedAuthority("ROLE_USER")));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> commentController.updateComment("c1", Map.of("text", "hack"), auth)
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(commentRepository, never()).save(any());
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    void deleteComment_byAdmin_deletes() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin");
        when(auth.getAuthorities()).thenReturn((Collection) List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        commentController.deleteComment("c1", auth);

        verify(commentRepository).deleteById("c1");
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    void deleteComment_byNotAuthor_notAdmin_forbidden() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("intruder");
        when(auth.getAuthorities()).thenReturn((Collection) List.of(new SimpleGrantedAuthority("ROLE_USER")));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> commentController.deleteComment("c1", auth)
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verify(commentRepository, never()).deleteById(anyString());
        verifyNoMoreInteractions(commentRepository);
    }
}