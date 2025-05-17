package ir.daneshrefah.scm.core.services.gateway;

import ir.daneshrefah.scm.common.model.gateway.Channel;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ChannelServiceImpl implements ChannelService {
    @Override
    public Optional<Channel> findChannelByCode(String code) {
        return Optional.empty();
    }
}
