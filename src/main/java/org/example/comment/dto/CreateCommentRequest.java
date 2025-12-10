package org.example.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.comment.ContentType;

import java.util.Map;

public class CreateCommentRequest {

    @NotNull
    private ContentType contentType;

    @NotNull
    private Long contentId;

    @NotBlank
    private String text;

    // опциональные метаданные
    private Map<String, Object> metadata;

    // --- getters/setters ---

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public Long getContentId() {
        return contentId;
    }

    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
