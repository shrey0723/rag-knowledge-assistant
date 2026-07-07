package com.ittools.ragassistant.repository;

import com.ittools.ragassistant.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}
