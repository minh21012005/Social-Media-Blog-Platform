package com.socialmediablog.platform.services.article.infrastructure.adapter;

import com.socialmediablog.platform.services.article.domain.aggregate.Article;
import com.socialmediablog.platform.services.article.domain.aggregate.ArticleStats;
import com.socialmediablog.platform.services.article.infrastructure.entity.JpaArticleEntity;
import com.socialmediablog.platform.services.article.infrastructure.persistence.SpringDataJpaArticleRepository;
import com.socialmediablog.platform.services.article.infrastructure.persistence.SpringDataJpaArticleStatsRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Background job that periodically calculates trending scores for all published articles
 * using a HackerNews-inspired time-decay algorithm:
 *
 *   score = (views*1 + claps*2 + comments*5 + bookmarks*10) / (hoursElapsed + 2)^1.5
 *
 * The top-N article IDs ordered by score are stored in Redis for fast retrieval.
 */
@Component
public class TrendingScoreJob {

    private static final Logger log = LoggerFactory.getLogger(TrendingScoreJob.class);

    /** Redis key holding the ordered list of trending article IDs (most trending first) */
    public static final String TRENDING_IDS_KEY = "article:trending:ids";

    /** Maximum number of top trending articles retained */
    private static final int TOP_N = 20;

    private final SpringDataJpaArticleRepository articleRepository;
    private final SpringDataJpaArticleStatsRepository statsRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final CacheManager cacheManager;

    @Value("${trending.job.page-size:200}")
    private int pageSize;

    @Value("${trending.cache.ttl-minutes:20}")
    private long cacheTtlMinutes;

    public TrendingScoreJob(
            SpringDataJpaArticleRepository articleRepository,
            SpringDataJpaArticleStatsRepository statsRepository,
            RedisTemplate<String, String> redisTemplate,
            CacheManager cacheManager
    ) {
        this.articleRepository = articleRepository;
        this.statsRepository = statsRepository;
        this.redisTemplate = redisTemplate;
        this.cacheManager = cacheManager;
    }

    @Scheduled(cron = "${trending.job.cron:0 */15 * * * *}")
    @Transactional(readOnly = true)
    public void recalculate() {
        log.info("[TrendingJob] Starting trending score recalculation...");
        long startMs = System.currentTimeMillis();

        try {
            Instant now = Instant.now();
            Instant thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS);
            List<ScoredArticle> scoredArticles = new ArrayList<>();
            int currentPage = 0;

            // Scan only published articles from the last 30 days in batches to avoid OOM and keep calculation fast
            while (true) {
                Page<JpaArticleEntity> page =
                        articleRepository.findByStatusAndPublishedAtGreaterThanEqual(
                                "PUBLISHED", thirtyDaysAgo,
                                PageRequest.of(currentPage, pageSize)
                        );

                if (page.isEmpty()) {
                    break;
                }

                for (var entity : page.getContent()) {
                    Article article = entity.toDomain();
                    if (article.publishedAt() == null) {
                        continue;
                    }

                    ArticleStats stats = statsRepository
                            .findByArticleId(article.id().value())
                            .map(se -> se.toDomain())
                            .orElse(null);

                    double score = computeScore(stats, article.publishedAt(), now);
                    scoredArticles.add(new ScoredArticle(article.id().value(), score));
                }

                if (!page.hasNext()) {
                    break;
                }
                currentPage++;
            }

            // Sort descending by score, keep top-N IDs
            List<String> topIds = scoredArticles.stream()
                    .sorted(Comparator.comparingDouble(ScoredArticle::score).reversed())
                    .limit(TOP_N)
                    .map(s -> s.articleId().toString())
                    .collect(Collectors.toList());

            // Atomically replace the Redis list
            redisTemplate.delete(TRENDING_IDS_KEY);
            if (!topIds.isEmpty()) {
                redisTemplate.opsForList().rightPushAll(TRENDING_IDS_KEY, topIds);
                // Keep the key alive slightly longer than the Spring cache TTL
                redisTemplate.expire(TRENDING_IDS_KEY, cacheTtlMinutes + 5, TimeUnit.MINUTES);
            }

            // Evict the Spring @Cacheable trending cache so the next HTTP request re-reads Redis
            evictCache("homepage:trending");

            long elapsed = System.currentTimeMillis() - startMs;
            log.info("[TrendingJob] Done – scored {} articles in {}ms, stored top {} IDs to Redis",
                    scoredArticles.size(), elapsed, topIds.size());

        } catch (Exception ex) {
            log.error("[TrendingJob] Recalculation failed: {}", ex.getMessage(), ex);
        }
    }

    /**
     * HackerNews-inspired engagement-weighted score with time decay.
     *
     *   score = (views×1 + claps×2 + comments×5 + bookmarks×10) / (hoursElapsed + 2)^1.5
     *
     * Articles published more recently naturally score higher for the same engagement level.
     */
    private double computeScore(ArticleStats stats, Instant publishedAt, Instant now) {
        long views     = stats != null ? stats.viewCount()     : 0;
        long claps     = stats != null ? stats.clapCount()     : 0;
        long comments  = stats != null ? stats.commentCount()  : 0;
        long bookmarks = stats != null ? stats.bookmarkCount() : 0;

        double numerator   = (views * 1.0) + (claps * 2.0) + (comments * 5.0) + (bookmarks * 10.0);
        double hoursElapsed = Math.max(0, ChronoUnit.HOURS.between(publishedAt, now));
        double decay        = Math.pow(hoursElapsed + 2, 1.5);

        return numerator / decay;
    }

    private void evictCache(String cacheName) {
        try {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        } catch (Exception ex) {
            log.warn("[TrendingJob] Failed to evict cache '{}': {}", cacheName, ex.getMessage());
        }
    }

    private record ScoredArticle(UUID articleId, double score) {
    }
}
