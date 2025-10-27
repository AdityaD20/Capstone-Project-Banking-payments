package com.aurionpro.app.repository;

import com.aurionpro.app.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByEntityNameAndEntityId(String entityName, Long entityId);
}