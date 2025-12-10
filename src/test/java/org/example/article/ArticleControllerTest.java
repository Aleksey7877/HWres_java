package org.example.article;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleControllerTest {

    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private ArticleController articleController;

    @Test
    void getAll_returnsAllArticles() {
        Article a1 = new Article();
        a1.setId(1L);
        a1.setTitle("A1");
        a1.setText("T1");
        a1.setAuthor("Alex");
        a1.setPublishedAt(LocalDate.now().toString());

        when(articleRepository.findAll()).thenReturn(List.of(a1));

        List<Article> result = articleController.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("A1");
        verify(articleRepository).findAll();
    }

    @Test
    void getById_whenNotFound_throws404() {
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> articleController.getById(99L));

        verify(articleRepository).findById(99L);
    }

    @Test
    void create_savesArticle() {
        Article toSave = new Article();
        toSave.setTitle("New");
        toSave.setText("Content");
        toSave.setAuthor("Admin");
        toSave.setPublishedAt(LocalDate.now().toString());

        Article saved = new Article();
        saved.setId(10L);
        saved.setTitle(toSave.getTitle());
        saved.setText(toSave.getText());
        saved.setAuthor(toSave.getAuthor());
        saved.setPublishedAt(toSave.getPublishedAt());

        when(articleRepository.save(any(Article.class))).thenReturn(saved);

        Article result = articleController.create(toSave);

        assertThat(result.getId()).isEqualTo(10L);
        verify(articleRepository).save(toSave);
    }


    @Test
    void getById_returnsArticle() {
        Article a1 = new Article();
        a1.setId(1L);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(a1));

        Article result = articleController.getById(1L);

        assertThat(result).isSameAs(a1);
        verify(articleRepository).findById(1L);
    }

    @Test
    void update_updatesAndSaves() {
        Article existing = new Article();
        existing.setId(1L);
        existing.setTitle("Old");

        when(articleRepository.findById(1L)).thenReturn(Optional.of(existing));

        Article req = new Article();
        req.setTitle("New");
        req.setText("T");
        req.setAuthor("A");
        req.setPublishedAt("2025-12-11");

        when(articleRepository.save(existing)).thenReturn(existing);

        Article result = articleController.update(1L, req);

        assertThat(result.getTitle()).isEqualTo("New");
        verify(articleRepository).save(existing);
    }

    @Test
    void update_whenNotFound_throws404() {
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        Article req = new Article();
        assertThrows(ResponseStatusException.class,
                () -> articleController.update(99L, req));
    }

    @Test
    void delete_whenNotFound_throws404() {
        when(articleRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResponseStatusException.class,
                () -> articleController.delete(99L));
    }

    @Test
    void delete_existing_deletesAndNoException() {
        when(articleRepository.existsById(1L)).thenReturn(true);

        articleController.delete(1L);

        verify(articleRepository).deleteById(1L);
    }
}
