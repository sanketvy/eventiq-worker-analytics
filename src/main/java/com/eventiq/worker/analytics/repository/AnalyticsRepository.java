package com.eventiq.worker.analytics.repository;

import com.eventiq.worker.analytics.model.Analytics;
import com.eventiq.worker.analytics.model.AnalyticsPrimaryKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyticsRepository extends CassandraRepository<Analytics, AnalyticsPrimaryKey> {
}
