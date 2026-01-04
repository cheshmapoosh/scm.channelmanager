package ir.daneshrefah.scm.uaa.service.messages;

import ir.daneshrefah.scm.uaa.controller.message.AdvertisementDTO;
import ir.daneshrefah.scm.uaa.controller.message.LoginMessageRequestDTO;
import ir.daneshrefah.scm.uaa.controller.message.LoginMessageResponseDTO;
import ir.daneshrefah.scm.uaa.domain.client.ClientVersion;
import ir.daneshrefah.scm.uaa.repository.activation.LoginMessageRepository;
import ir.daneshrefah.scm.uaa.repository.activation.domain.LoginMessageEntity;
import ir.daneshrefah.scm.uaa.repository.activation.domain.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
@Slf4j
@Service
public class LoginMessageService {
    private final LoginMessageRepository repository;
    private final static Sort DEFAULT_SORT = Sort.by("createdDate").descending();
    public LoginMessageService(LoginMessageRepository repository) {
        this.repository = repository;
    }

    public List<LoginMessageResponseDTO> findMessages(LoginMessageRequestDTO loginMessageRequestDTO, String application) {
        List<LoginMessageEntity> loginMessageEntities ;
        ClientVersion clientDTO = loginMessageRequestDTO.getClientDTO();
        LocalDateTime now = LocalDateTime.now();

        PageRequest createdDate = PageRequest.of(0, loginMessageRequestDTO.getFetchSize(), Sort.by("createdDate").descending());
        List<Long> clients = new ArrayList<>();
        clients.add(clientDTO.getId());
        clients.add(0L);
        List<String> applications = new ArrayList<>();
        applications.add(application);
        applications.add("ALL");


        if (loginMessageRequestDTO.isGetExpiredMessage()) {
            log.info("requested for inbox message");
            loginMessageEntities = getExpiredMessages(loginMessageRequestDTO, createdDate,clients,applications);
        } else {
            loginMessageEntities = getNonExpiredMessages(loginMessageRequestDTO,clients,applications, createdDate, now);
        }


        List<LoginMessageResponseDTO> loginMessageResponseDTO = new ArrayList<>();
        for (LoginMessageEntity entity : loginMessageEntities) {
            LoginMessageResponseDTO dto = new LoginMessageResponseDTO();
            dto.setId(entity.getId());
            dto.setMessageType(entity.getMessageType());
            AdvertisementDTO advertisementDTO = new AdvertisementDTO();
            advertisementDTO.setIcon(entity.getIcon());
            advertisementDTO.setText(entity.getText());
            advertisementDTO.setUrl(entity.getUrl());
            dto.setAdvertisement(advertisementDTO);
            dto.setActive(entity.getIsActive());
            dto.setBody(entity.getBody());
            dto.setClient(entity.getClient());
            dto.setExpirationDate(
                    entity.getExpirationDate()
                            .atZone(ZoneId.systemDefault())
                            .toInstant().toEpochMilli() / 1000
            );
            dto.setCreatedDate(
                    entity.getCreatedDate()
                            .atZone(ZoneId.systemDefault())
                            .toInstant().toEpochMilli() / 1000
            );

            loginMessageResponseDTO.add(dto);
        }

        return loginMessageResponseDTO;

    }
    private List<LoginMessageEntity> getExpiredMessages(LoginMessageRequestDTO requestDTO, PageRequest createdDate,List<Long> clients, List<String> applications) {
        MessageType messageType = requestDTO.getMessageType();
        boolean isAllMessageType = messageType.equals(MessageType.ALL);

        List<LoginMessageEntity> allLoginMessages = repository.doFindAll(DEFAULT_SORT).stream().distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(allLoginMessages)) {
            return new ArrayList<>();
        }

        return allLoginMessages.stream().filter(loginMessage -> {
            boolean match = clients.stream().anyMatch(c -> Objects.equals(c, loginMessage.getClient()));
            match = match && applications.stream().anyMatch(a -> StringUtils.equals(a, loginMessage.getApplication()));

            ZonedDateTime requestZonedDateTime =  requestDTO.getCreatedDate() != null ? requestDTO.getCreatedDate().atZone(ZoneId.systemDefault()) : LocalDateTime.MAX.atZone(ZoneId.systemDefault());
            ZonedDateTime messageZonedDateTime = loginMessage.getCreatedDate().atZone(ZoneId.systemDefault());
            match = match && (requestZonedDateTime.isEqual(messageZonedDateTime) || requestZonedDateTime.isAfter(messageZonedDateTime));

            match = match && loginMessage.getIsActive();
            match = match && (isAllMessageType || Objects.equals(messageType, loginMessage.getMessageType()));
            return match;
        }).collect(Collectors.toList());
    }

    public List<LoginMessageEntity> getNonExpiredMessages(LoginMessageRequestDTO requestDTO,List<Long> clients, List<String> applications , PageRequest createdDate,
                                                    LocalDateTime now) {
        log.info("Executing getNonExpiredMessages method...");
        MessageType messageType = requestDTO.getMessageType();
        boolean isAllMessageType = messageType.equals(MessageType.ALL);

        List<LoginMessageEntity> allLoginMessages = repository.doFindAll(DEFAULT_SORT).stream().distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(allLoginMessages)) {
            return new ArrayList<>();
        }

        return allLoginMessages.stream().filter(loginMessage -> {
            boolean match = clients.stream().anyMatch(c -> Objects.equals(c, loginMessage.getClient()));
            match = match && applications.stream().anyMatch(a -> StringUtils.equals(a, loginMessage.getApplication()));

            ZonedDateTime requestZonedDateTime =  requestDTO.getCreatedDate() != null ? requestDTO.getCreatedDate().atZone(ZoneId.systemDefault()) : LocalDateTime.MIN.atZone(ZoneId.systemDefault());
            ZonedDateTime messageZonedDateTime = loginMessage.getCreatedDate().atZone(ZoneId.systemDefault());
            match = match && messageZonedDateTime.isAfter(requestZonedDateTime);

            match = match && loginMessage.getIsActive();
            match = match && (isAllMessageType || MessageType.ADVERTISEMENT.equals(loginMessage.getMessageType()) || Objects.equals(messageType, loginMessage.getMessageType()));
            match = match && loginMessage.getExpirationDate().isAfter(now);
            return match;
        }).collect(Collectors.toList());
    }

}
