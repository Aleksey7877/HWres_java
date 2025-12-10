package org.example.comment;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class CommentReply {

    private String id;

    @NotBlank
    private String authorUsername;

    @NotBlank
    private String text;

    private Instant createdAt = Instant.now();

    // гибкая схема метаданных
    private Map<String, Object> metadata = new HashMap<>();

    public CommentReply() {
    }

    public CommentReply(String id, String authorUsername, String text) {
        this.id = id;
        this.authorUsername = authorUsername;
        this.text = text;
        this.createdAt = Instant.now();
    }

    // --- getters/setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
