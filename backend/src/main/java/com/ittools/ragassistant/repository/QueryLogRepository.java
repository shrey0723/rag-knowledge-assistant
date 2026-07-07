package com.ittools.ragassistant.repository;

import com.ittools.ragassistant.model.QueryLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QueryLogRepository extends JpaRepository<QueryLog, Long> {
    List<QueryLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
