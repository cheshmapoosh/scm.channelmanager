package ir.daneshrefah.scm.notification.client.service;

import ir.daneshrefah.scm.notification.client.mapper.MessageTemplateMapper;
import ir.daneshrefah.scm.notification.client.repository.MessageTemplateRepository;
import ir.daneshrefah.scm.common.model.notification.MessageTemplate;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationTemplate;
import ir.daneshrefah.scm.notification.client.exception.NotificationTemplateNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageTemplateService {
    private List<MessageTemplate> messageTemplates;
    private final MessageTemplateRepository messageTemplateRepository;

    @PostConstruct
    private void init(){
        try {
            messageTemplates = new ArrayList<>();
            messageTemplateRepository
                    .findAll()
                    .stream()
                    .map(MessageTemplateMapper.INSTANCE::toDto)
                    .forEach(messageTemplates::add);
            log.info(">> {} message templates loaded ...",messageTemplates.size());
        }catch (Exception e){
            log.warn(">>> message templates could not loaded ");
        }
    }
    public List<MessageTemplate> findMessageTemplates() {
        return this.messageTemplates;
    }

    public MessageTemplate findMessageTemplateByCode(NotificationTemplate templateCode) {
        return findMessageTemplates()
                .stream()
                .filter(messageTemplate -> messageTemplate.getCode().equals(templateCode))
                .findFirst()
                .orElseThrow(() -> new NotificationTemplateNotFoundException(null, templateCode.getValue()));
    }

}
