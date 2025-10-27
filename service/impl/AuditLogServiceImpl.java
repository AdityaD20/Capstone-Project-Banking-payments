package com.aurionpro.app.service.impl;

import com.aurionpro.app.entity.AuditLog;
import com.aurionpro.app.entity.enums.ActionType;
import com.aurionpro.app.entity.user.User;
import com.aurionpro.app.repository.AuditLogRepository;
import com.aurionpro.app.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void logAction(User actor, ActionType action, String entityName, Long entityId, String details) {
        AuditLog log = new AuditLog();
        log.setUser(actor);
        log.setAction(action);
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setDetails(details);
        auditLogRepository.save(log);
    }
}