package com.shopping.book.common.aspect;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErrorLogRepository extends ElasticsearchRepository<ErrorLog, String> {
}