package com.aurionpro.app.service;

import com.aurionpro.app.entity.enums.ActionType;
import com.aurionpro.app.entity.user.User;

public interface AuditLogService {
    void logAction(User actor, ActionType action, String entityName, Long entityId, String details);
}