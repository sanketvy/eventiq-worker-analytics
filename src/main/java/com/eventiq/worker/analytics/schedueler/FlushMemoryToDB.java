package com.eventiq.worker.analytics.schedueler;

import com.eventiq.worker.analytics.model.Analytics;
import com.eventiq.worker.analytics.model.AnalyticsPrimaryKey;
import com.eventiq.worker.analytics.repository.AnalyticsRepository;
import com.eventiq.worker.analytics.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class FlushMemoryToDB {

    RedisTemplate<String, Object> redisTemplate;

    AnalyticsRepository analyticsRepository;

    public FlushMemoryToDB(RedisTemplate<String, Object> redisTemplate, AnalyticsRepository analyticsRepository){
        this.redisTemplate = redisTemplate;
        this.analyticsRepository = analyticsRepository;
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void flushCacheDataToDB(){
        boolean leader = Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(Constants.DISTRIBUTED_LEADER, "lock", 4 * 60 + 59, TimeUnit.SECONDS)
        );

        if (!leader) return;

        Set<Object> projects = redisTemplate.opsForSet().members(Constants.HOT_PROJECTS);

        if (projects == null || projects.isEmpty()) return;

        projects.forEach(project ->{
            long timestamp = floorToLast5Minutes(LocalDateTime.now());
            String key = Constants.KEY_PREFIX + project + "-" + timestamp;

            try{
                Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

                AnalyticsPrimaryKey primaryKey = AnalyticsPrimaryKey.builder().projectId(project.toString()).timestamp(timestamp).build();
                Analytics analytics = Analytics.builder().primaryKey(primaryKey)
                        .clicksCount(Long.valueOf(entries.getOrDefault(Constants.CLICK_COUNT, 0).toString()))
                        .errorCount(Long.valueOf(entries.getOrDefault(Constants.ERROR_COUNT, 0).toString()))
                        .pageVisitedCount(Long.valueOf(entries.getOrDefault(Constants.VISITS_COUNT, 0).toString()))
                        .formsCount(Long.valueOf(entries.getOrDefault(Constants.FORMS_COUNT, 0).toString()))
                        .sessionCount(Long.valueOf(entries.getOrDefault(Constants.SESSION_COUNT, 0).toString()))
                        .milestoneCount(Long.valueOf(entries.getOrDefault(Constants.MILESTONE_COUNT, 0).toString()))
                        .build();

                analyticsRepository.save(analytics);
                redisTemplate.delete(key);
            } catch (Exception ex){
                log.error("Error processing project {}, for timestamp: {}", project, timestamp);
            }
        });
        redisTemplate.delete(Constants.HOT_PROJECTS);
    }

    public static long floorToLast5Minutes(LocalDateTime dateTime) {
        int minute = dateTime.getMinute();
        int minutesToSubtract = minute % 5;
        LocalDateTime truncated = dateTime.truncatedTo(ChronoUnit.MINUTES);
        return truncated.minusMinutes(minutesToSubtract).toEpochSecond(ZoneOffset.UTC);
    }

}
