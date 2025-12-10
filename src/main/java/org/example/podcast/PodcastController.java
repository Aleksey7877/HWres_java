package org.example.podcast;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/podcasts")
@RequiredArgsConstructor
@Tag(name = "Подкасты", description = "Управление подкастами")
public class PodcastController {

    private final PodcastRepository podcastRepository;

    @GetMapping
    @Operation(summary = "Получить список всех подкастов")
    public List<Podcast> getAll() {
        return podcastRepository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить подкаст по идентификатору")
    public Podcast getById(@PathVariable Long id) {
        return podcastRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Podcast not found"));
    }

    @PostMapping
    @Operation(summary = "Создать новый подкаст")
    public Podcast create(@Valid @RequestBody Podcast podcast) {
        return podcastRepository.save(podcast);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить существующий подкаст")
    public Podcast update(@PathVariable Long id, @Valid @RequestBody Podcast request) {
        Podcast podcast = podcastRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Podcast not found"));

        podcast.setTitle(request.getTitle());
        podcast.setAudioUrl(request.getAudioUrl());
        podcast.setEpisodes(request.getEpisodes());

        return podcastRepository.save(podcast);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить подкаст")
    public void delete(@PathVariable Long id) {
        if (!podcastRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Podcast not found");
        }
        podcastRepository.deleteById(id);
    }
}
