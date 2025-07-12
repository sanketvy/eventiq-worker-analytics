package com.eventiq.worker.analytics.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("analytics")
@Data
@Builder
public class Analytics {

    @PrimaryKey
    private AnalyticsPrimaryKey primaryKey;

    private Long sessionCount;

    private Long totalEventsCount;

    private Long errorCount;

    private Long clicksCount;

    private Long pageVisitedCount;

    private Long milestoneCount;

    private Long formsCount;
}
