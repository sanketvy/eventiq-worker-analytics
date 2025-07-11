package com.eventiq.worker.analytics.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
@Data
@Builder
public class AnalyticsPrimaryKey {

    @PrimaryKeyColumn(name = "projectId", type = PrimaryKeyType.PARTITIONED, ordinal = 0)
    private String projectId;

    @PrimaryKeyColumn(name = "timestamp", type = PrimaryKeyType.PARTITIONED, ordinal = 1)
    private Long timestamp;
}
