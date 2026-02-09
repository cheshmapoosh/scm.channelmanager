package ir.daneshrefah.scm.common.log.service;


import ir.daneshrefah.scm.common.log.entity.message.MessageLogEntity;

import java.util.List;

public interface MessageLogService {
    void saveAll(List<MessageLogEntity> messageLogEntities);
}
