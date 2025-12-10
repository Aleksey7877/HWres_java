package org.example.comment;

import org.example.comment.dto.CreateCommentRequest;
import org.example.comment.dto.CreateReplyRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentController commentController;


    @Test
    void createComment_setsAuthor_andSaves() {
        CreateCommentRequest req = new CreateCommentRequest();
        req.setContentType(ContentType.ARTICLE);
        req.setContentId(2L);
        req.setText("тест комментария");

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
        verify(commentRepository).save(any(Comment.class));
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    void createComment_withMetadata_andAnonymousUser_setsMetadataAndAnonymous() {
        CreateCommentRequest req = new CreateCommentRequest();
        req.setContentType(ContentType.ARTICLE);
        req.setContentId(3L);
        req.setText("коммент с метаданными");
        req.setMetadata(Map.of("ip", "127.0.0.1"));

        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, Comment.class));

        Comment result = commentController.createComment(req, null);

        assertThat(result.getAuthorUsername()).isEqualTo("anonymous");
        assertThat(result.getMetadata()).containsEntry("ip", "127.0.0.1");

        verify(commentRepository).save(any(Comment.class));
        verifyNoMoreInteractions(commentRepository);
    }


    @Test
    void addReply_whenCommentNotFound_throws() {
        when(commentRepository.findById("missing")).thenReturn(Optional.empty());

        CreateReplyRequest req = new CreateReplyRequest();
        req.setText("ответ");

        assertThrows(
                CommentNotFoundException.class,
                () -> commentController.addReply("missing", req, null)
        );

        verify(commentRepository).findById("missing");
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    void addReply_addsReplyToComment() {
        Comment comment = new Comment();
        comment.setId("c1");
        comment.setReplies(new ArrayList<>());
        comment.setCreatedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());

        when(commentRepository.findById("c1")).thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(comment);

        CreateReplyRequest req = new CreateReplyRequest();
        req.setText("reply text");

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user2");

        Comment result = commentController.addReply("c1", req, auth);

        assertThat(result.getReplies()).hasSize(1);
        assertThat(result.getReplies().get(0).getAuthorUsername()).isEqualTo("user2");

        verify(commentRepository).findById("c1");
        verify(commentRepository).save(comment);
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    void addReply_withMetadata_andAnonymousUser_setsMetadataAndAnonymous() {
        Comment comment = new Comment();
        comment.setId("c2");
        comment.setReplies(new ArrayList<>());
        comment.setCreatedAt(Instant.now());
        comment.setUpdatedAt(Instant.now());

        when(commentRepository.findById("c2")).thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(comment);

        CreateReplyRequest req = new CreateReplyRequest();
        req.setText("reply with metadata");
        req.setMetadata(Map.of("client", "mobile"));

        Comment result = commentController.addReply("c2", req, null);

        assertThat(result.getReplies()).hasSize(1);
        assertThat(result.getReplies().get(0).getAuthorUsername()).isEqualTo("anonymous");
        assertThat(result.getReplies().get(0).getMetadata())
                .containsEntry("client", "mobile");

        verify(commentRepository).findById("c2");
        verify(commentRepository).save(comment);
        verifyNoMoreInteractions(commentRepository);
    }


    @Test
    void getById_returnsComment() {
        Comment c = new Comment();
        c.setId("c42");
        when(commentRepository.findById("c42")).thenReturn(Optional.of(c));

        Comment result = commentController.getById("c42");

        assertThat(result).isSameAs(c);
        verify(commentRepository).findById("c42");
    }

    @Test
    void getById_whenNotFound_throws() {
        when(commentRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class,
                () -> commentController.getById("missing"));
    }

    @Test
    void getByContent_returnsList() {
        Comment c1 = new Comment();
        c1.setId("c1");
        when(commentRepository
                .findByContentTypeAndContentIdOrderByCreatedAtAsc(ContentType.ARTICLE, 2L))
                .thenReturn(List.of(c1));

        List<Comment> result = commentController.getByContent(ContentType.ARTICLE, 2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isSameAs(c1);
        verify(commentRepository)
                .findByContentTypeAndContentIdOrderByCreatedAtAsc(ContentType.ARTICLE, 2L);
    }


    @Test
    void updateComment_changesTextAndSaves() {
        Comment c = new Comment();
        c.setId("c1");
        c.setText("old");

        when(commentRepository.findById("c1")).thenReturn(Optional.of(c));
        when(commentRepository.save(c)).thenReturn(c);

        Comment result = commentController.updateComment(
                "c1",
                Map.of("text", "new text")
        );

        assertThat(result.getText()).isEqualTo("new text");
        verify(commentRepository).findById("c1");
        verify(commentRepository).save(c);
    }

    @Test
    void deleteComment_whenNotExists_throws() {
        when(commentRepository.existsById("missing")).thenReturn(false);

        assertThrows(CommentNotFoundException.class,
                () -> commentController.deleteComment("missing"));
    }

    @Test
    void deleteComment_existing_deletes() {
        when(commentRepository.existsById("c1")).thenReturn(true);

        commentController.deleteComment("c1");

        verify(commentRepository).existsById("c1");
        verify(commentRepository).deleteById("c1");
    }
}
