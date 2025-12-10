package org.example.video;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
@Tag(name = "Видео", description = "Управление видеоконтентом")
public class VideoController {

    private final VideoRepository videoRepository;

    @GetMapping
    @Operation(summary = "Получить список всех видео")
    public List<Video> getAll() {
        return videoRepository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить видео по идентификатору")
    public Video getById(@PathVariable Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Video not found"));
    }

    @PostMapping
    @Operation(summary = "Создать новое видео")
    public Video create(@Valid @RequestBody Video video) {
        return videoRepository.save(video);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить существующее видео")
    public Video update(@PathVariable Long id, @Valid @RequestBody Video request) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Video not found"));

        video.setTitle(request.getTitle());
        video.setUrl(request.getUrl());
        video.setDuration(request.getDuration());

        return videoRepository.save(video);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить видео")
    public void delete(@PathVariable Long id) {
        if (!videoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Video not found");
        }
        videoRepository.deleteById(id);
    }
}
