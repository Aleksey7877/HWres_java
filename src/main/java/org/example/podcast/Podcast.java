package org.example.podcast;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "podcasts")
public class Podcast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Заголовок подкаста не должен быть пустым")
    @Size(max = 255, message = "Заголовок не должен превышать 255 символов")
    private String title;

    @NotBlank(message = "URL аудио не должен быть пустым")
    @Size(max = 500, message = "URL аудио не должен превышать 500 символов")
    private String audioUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "podcast_episodes", joinColumns = @JoinColumn(name = "podcast_id"))
    @Column(name = "episode")
    private List<
            @NotBlank(message = "Название эпизода не должно быть пустым")
            @Size(max = 500, message = "Название эпизода не должно превышать 500 символов")
                    String> episodes = new ArrayList<>();

    public Podcast() {
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

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public List<String> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(List<String> episodes) {
        this.episodes = episodes;
    }
}
