package org.example.comment;

public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(String id) {
        super("Comment not found: " + id);
    }
}
