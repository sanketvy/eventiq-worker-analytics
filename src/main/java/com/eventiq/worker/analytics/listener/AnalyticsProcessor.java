package com.eventiq.worker.analytics.listener;

import com.eventiq.shared.dto.Event;
import com.eventiq.worker.analytics.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Consumer;

@Configuration
public class AnalyticsProcessor {

    RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public AnalyticsProcessor(RedisTemplate<String, Object> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    @Bean
    public Consumer<List<Event>> processAnalytics(){
        return events -> {
            long keyTime = ceilToNext5Minutes(LocalDateTime.now());
            events.forEach(event -> {
                String key = Constants.KEY_PREFIX + event.getProjectId() + "-" + keyTime;

                redisTemplate.opsForSet().add(Constants.HOT_PROJECTS, event.getProjectId());
                redisTemplate.opsForHash().increment(key, Constants.TOTAL_EVENTS_COUNT, 1);

                if(event.getType().equals(Constants.EVENT_SESSION_START)){
                    redisTemplate.opsForHash().increment(key, Constants.SESSION_COUNT, 1);
                } else if (event.getType().equals(Constants.EVENT_SESSION_END)){
                    redisTemplate.opsForHash().increment(key, Constants.SESSION_COUNT, -1);
                } else if (event.getType().equals(Constants.EVENT_CLICK)){
                    redisTemplate.opsForHash().increment(key, Constants.CLICK_COUNT, 1);
                } else if (event.getType().equals(Constants.EVENT_VISITED)){
                    redisTemplate.opsForHash().increment(key, Constants.VISITS_COUNT, 1);
                } else if (event.getType().equals(Constants.EVENT_ERROR)){
                    redisTemplate.opsForHash().increment(key, Constants.ERROR_COUNT, 1);
                } else if (event.getType().equals(Constants.MILESTONE)){
                    redisTemplate.opsForHash().increment(key, Constants.MILESTONE_COUNT, 1);
                } else if (event.getType().equals(Constants.EVENT_FORM_INITIATED)){
                    redisTemplate.opsForHash().increment(key, Constants.FORMS_COUNT, 1);
                } else if (event.getType().equals(Constants.EVENT_FORM_SUBMIT)){
                    redisTemplate.opsForHash().increment(key, Constants.FORMS_COUNT, -1);
                }
            });
        };
    }

    public static long ceilToNext5Minutes(LocalDateTime dateTime) {
        int minute = dateTime.getMinute();
        int minutesToAdd = 5 - (minute % 5);
        LocalDateTime truncated = dateTime.truncatedTo(ChronoUnit.MINUTES);
        return truncated.plusMinutes(minutesToAdd).toEpochSecond(ZoneOffset.UTC);
    }
}
