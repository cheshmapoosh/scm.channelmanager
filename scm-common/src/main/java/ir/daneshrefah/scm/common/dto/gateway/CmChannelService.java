package ir.daneshrefah.scm.common.dto.gateway;

import ir.daneshrefah.scm.common.model.gateway.CmChannel;

import java.util.List;
import java.util.Optional;

public interface CmChannelService {
    List<CmChannel> findAllChannels();

    Optional<CmChannel> findChannelById(Integer id);

    Optional<CmChannel> findChannelByCode(String code);
}
