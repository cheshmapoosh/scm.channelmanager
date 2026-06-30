package ir.daneshrefah.scm.uaa.service.activation.pwa.services.authentication;

import ir.daneshrefah.scm.uaa.common.constants.PwaOauthMessage;
import ir.daneshrefah.scm.uaa.common.utils.ErrorUtils;
import ir.daneshrefah.scm.uaa.config.PwaAuthenticationConfigProperties;
import ir.daneshrefah.scm.uaa.domain.pwa.WhiteList;
import ir.daneshrefah.scm.uaa.mapper.WhiteListMapper;
import ir.daneshrefah.scm.uaa.repository.activation.WhiteListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static ir.daneshrefah.scm.uaa.common.utils.Constants.OAUTH2_ERROR_CODE_INVALID_USER;

@Service
@ConditionalOnBean(name = "activationDataSource")
@RequiredArgsConstructor
@Slf4j
public class PwaWhiteListService {

    private final WhiteListRepository whiteListRepository;
    private final PwaAuthenticationConfigProperties properties;
    private final WhiteListMapper mapper;


    public Optional<WhiteList> findByUsername(String username) {
        log.debug("Request to find user from username: {}", username);
        return whiteListRepository.findByUsername(username).map(mapper::toModel);
    }

    @Transactional(transactionManager = "activationTransactionManager", propagation = Propagation.NOT_SUPPORTED)
    public void checkWhiteList(String username) {
        if (Boolean.TRUE.equals(properties.getWhiteListEnabled())) {
            log.info("Whitelist is ON");
            if (findByUsername(username).isEmpty()) {
                log.warn("User '{}' was not found in whitelist",username);
                ErrorUtils.throwError(OAUTH2_ERROR_CODE_INVALID_USER,PwaOauthMessage.INVALID_CREDENTIALS.name());
            }
        }
    }
}
