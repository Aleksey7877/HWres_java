package org.example.comment.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public class CreateReplyRequest {

    @NotBlank
    private String text;

    private Map<String, Object> metadata;

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
