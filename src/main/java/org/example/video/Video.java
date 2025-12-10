package org.example.video;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "videos")
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Заголовок видео не должен быть пустым")
    @Size(max = 255, message = "Заголовок не должен превышать 255 символов")
    private String title;

    @NotBlank(message = "URL видео не должен быть пустым")
    @Size(max = 500, message = "URL не должен превышать 500 символов")
    private String url;

    @Min(value = 1, message = "Длительность должна быть положительным числом секунд")
    private Integer duration; // в секундах

    public Video() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }
}
