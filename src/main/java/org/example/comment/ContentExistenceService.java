package org.example.comment;

import lombok.RequiredArgsConstructor;
import org.example.article.ArticleRepository;
import org.example.video.VideoRepository;
import org.example.podcast.PodcastRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentExistenceService {

    private final ArticleRepository articleRepository;
    private final VideoRepository videoRepository;
    private final PodcastRepository podcastRepository;

    public boolean exists(ContentType type, Long contentId) {
        return switch (type) {
            case ARTICLE -> articleRepository.existsById(contentId);
            case VIDEO -> videoRepository.existsById(contentId);
            case PODCAST -> podcastRepository.existsById(contentId);
        };
    }
}