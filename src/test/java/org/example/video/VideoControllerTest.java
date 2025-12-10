package org.example.video;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoControllerTest {

    @Mock
    private VideoRepository videoRepository;

    @InjectMocks
    private VideoController videoController;

    @Test
    void getAll_returnsAllVideos() {
        Video v1 = new Video();
        v1.setId(1L);
        v1.setTitle("Video 1");
        v1.setUrl("https://example.com/1");
        v1.setDuration(100);

        when(videoRepository.findAll()).thenReturn(List.of(v1));

        List<Video> result = videoController.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Video 1");
        verify(videoRepository).findAll();
    }

    @Test
    void getById_returnsVideo() {
        Video v = new Video();
        v.setId(5L);

        when(videoRepository.findById(5L)).thenReturn(Optional.of(v));

        Video result = videoController.getById(5L);

        assertThat(result).isSameAs(v);
        verify(videoRepository).findById(5L);
    }

    @Test
    void getById_whenNotFound_throws404() {
        when(videoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> videoController.getById(99L));
    }

    @Test
    void create_savesVideo() {
        Video toSave = new Video();
        toSave.setTitle("New");
        toSave.setUrl("https://example.com/new");
        toSave.setDuration(123);

        Video saved = new Video();
        saved.setId(10L);
        saved.setTitle(toSave.getTitle());
        saved.setUrl(toSave.getUrl());
        saved.setDuration(toSave.getDuration());

        when(videoRepository.save(toSave)).thenReturn(saved);

        Video result = videoController.create(toSave);

        assertThat(result.getId()).isEqualTo(10L);
        verify(videoRepository).save(toSave);
    }

    @Test
    void update_updatesExistingVideo() {
        Video existing = new Video();
        existing.setId(1L);
        existing.setTitle("Old");
        existing.setUrl("old");
        existing.setDuration(10);

        Video req = new Video();
        req.setTitle("New");
        req.setUrl("new");
        req.setDuration(20);

        when(videoRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(videoRepository.save(existing)).thenReturn(existing);

        Video result = videoController.update(1L, req);

        assertThat(result.getTitle()).isEqualTo("New");
        assertThat(result.getUrl()).isEqualTo("new");
        assertThat(result.getDuration()).isEqualTo(20);
        verify(videoRepository).findById(1L);
        verify(videoRepository).save(existing);
    }

    @Test
    void delete_whenExists_deletes() {
        when(videoRepository.existsById(1L)).thenReturn(true);

        videoController.delete(1L);

        verify(videoRepository).existsById(1L);
        verify(videoRepository).deleteById(1L);
    }

    @Test
    void delete_whenNotExists_throws404() {
        when(videoRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResponseStatusException.class,
                () -> videoController.delete(99L));
    }
}
