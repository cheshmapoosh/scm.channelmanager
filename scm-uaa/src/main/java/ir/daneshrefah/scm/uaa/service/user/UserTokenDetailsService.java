package ir.daneshrefah.scm.uaa.service.user;

import ir.daneshrefah.scm.common.data.entity.UserTokenDetails;
import ir.daneshrefah.scm.common.data.repository.userTokenDetails.UserTokenDetailsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserTokenDetailsService {

    private final UserTokenDetailsRepository tokenDetailsRepository;

    public Optional<UserTokenDetails> findByPersonIdAndTerminalLegacyTerminalId(Long personId, Long terminalLegacyTerminalId) {
        return tokenDetailsRepository.findByPersonIdAndTerminalLegacyTerminalId(personId, terminalLegacyTerminalId);
    }

    public List<UserTokenDetails> findByPersonId(Long personId) {
        return tokenDetailsRepository.findByPersonId(personId);
    }

    public Optional<UserTokenDetails> findByPersonUsernameAndTerminalLegacyTerminalId(String username, Long channelId) {
        return tokenDetailsRepository.findByPersonUsernameAndTerminalLegacyTerminalId(username, channelId);
    }

    public boolean hasOTPAssignment(String username, Long channelId) {
        if (StringUtils.hasText(username)) {
            long count = tokenDetailsRepository.countByPersonUsernameAndTerminalLegacyTerminalId(username, channelId);
            return count >= 1;
        }
        return false;
    }

    public void persist(UserTokenDetails userTokenDetails) {
        tokenDetailsRepository.save(userTokenDetails);
    }
}
