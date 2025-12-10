package org.example.podcast;

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
class PodcastControllerTest {

    @Mock
    private PodcastRepository podcastRepository;

    @InjectMocks
    private PodcastController podcastController;

    @Test
    void getAll_returnsAllPodcasts() {
        Podcast p = new Podcast();
        p.setId(1L);
        p.setTitle("Tech talks");
        p.setAudioUrl("https://example.com/podcast");
        p.setEpisodes(List.of("ep1", "ep2"));

        when(podcastRepository.findAll()).thenReturn(List.of(p));

        List<Podcast> result = podcastController.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Tech talks");
        verify(podcastRepository).findAll();
    }

    @Test
    void getById_returnsPodcast() {
        Podcast p = new Podcast();
        p.setId(2L);
        when(podcastRepository.findById(2L)).thenReturn(Optional.of(p));

        Podcast result = podcastController.getById(2L);

        assertThat(result).isSameAs(p);
        verify(podcastRepository).findById(2L);
    }

    @Test
    void getById_whenNotFound_throws404() {
        when(podcastRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> podcastController.getById(99L));
    }

    @Test
    void create_savesPodcast() {
        Podcast toSave = new Podcast();
        toSave.setTitle("New pod");
        toSave.setAudioUrl("url");
        toSave.setEpisodes(List.of("e1"));

        Podcast saved = new Podcast();
        saved.setId(5L);
        saved.setTitle(toSave.getTitle());
        saved.setAudioUrl(toSave.getAudioUrl());
        saved.setEpisodes(toSave.getEpisodes());

        when(podcastRepository.save(toSave)).thenReturn(saved);

        Podcast result = podcastController.create(toSave);

        assertThat(result.getId()).isEqualTo(5L);
        verify(podcastRepository).save(toSave);
    }

    @Test
    void update_updatesPodcast() {
        Podcast existing = new Podcast();
        existing.setId(3L);
        existing.setTitle("Old");
        existing.setAudioUrl("old");
        existing.setEpisodes(List.of("old"));

        Podcast req = new Podcast();
        req.setTitle("New");
        req.setAudioUrl("new");
        req.setEpisodes(List.of("n1", "n2"));

        when(podcastRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(podcastRepository.save(existing)).thenReturn(existing);

        Podcast result = podcastController.update(3L, req);

        assertThat(result.getTitle()).isEqualTo("New");
        assertThat(result.getAudioUrl()).isEqualTo("new");
        assertThat(result.getEpisodes()).containsExactly("n1", "n2");

        verify(podcastRepository).findById(3L);
        verify(podcastRepository).save(existing);
    }

    @Test
    void delete_whenExists_deletes() {
        when(podcastRepository.existsById(1L)).thenReturn(true);

        podcastController.delete(1L);

        verify(podcastRepository).existsById(1L);
        verify(podcastRepository).deleteById(1L);
    }

    @Test
    void delete_whenNotExists_throws404() {
        when(podcastRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResponseStatusException.class,
                () -> podcastController.delete(99L));
    }
}
