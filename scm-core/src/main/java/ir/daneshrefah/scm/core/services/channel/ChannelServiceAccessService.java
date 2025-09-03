//package ir.daneshrefah.scm.core.services.channel;
//
//import ir.daneshrefah.scm.common.constant.TerminalType;
//import ir.daneshrefah.scm.common.data.entity.asset.ChannelServiceAccessEntity;
//import ir.daneshrefah.scm.common.data.mapper.ChannelServiceAccessMapper;
//import ir.daneshrefah.scm.common.data.repository.assets.ChannelServiceAccessRepository;
//import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
//import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
//import ir.daneshrefah.scm.utils.validation.ValidationUtils;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Service
//@RequiredArgsConstructor
//@Deprecated
//public class ChannelServiceAccessService {
//
//    private final Map<TerminalType, List<ChannelServiceAccess>> CACHE = new ConcurrentHashMap<>();
//    private volatile List<ChannelServiceAccess> CACHE_ALL = List.of();
//    private final ChannelServiceAccessRepository channelServiceAccessRepository;
//    private final ChannelServiceAccessMapper channelServiceAccessMapper;
//
//    public List<ChannelServiceAccess> findAllByTerminalType(TerminalType terminalType) {
//        return CACHE.computeIfAbsent(terminalType, key -> channelServiceAccessRepository
//                .findByChannelId(terminalType.getLegacyTerminalId())
//                .stream()
//                .map(channelServiceAccessMapper::toModel)
//                .toList());
//    }
//
//    public List<ChannelServiceAccess> findAll() {
//        if (CACHE_ALL.isEmpty()) {
//            synchronized (this) {
//                if (CACHE_ALL.isEmpty()) {
//                    CACHE_ALL = List.copyOf(channelServiceAccessMapper.toModel(channelServiceAccessRepository.findAll()));
//                }
//            }
//        }
//        return CACHE_ALL;
//    }
//
//    public List<ChannelServiceAccess> findAllByServiceId(Short serviceId) {
//        return findAll().stream()
//                .filter(channelServiceAccess -> channelServiceAccess.getService() != null && java.util.Objects.equals(channelServiceAccess.getService().getId(), serviceId))
//                .toList();
//    }
//
//    public ChannelServiceAccess create(ChannelServiceAccess channelServiceAccess) {
//        ValidationUtils.checkNull(channelServiceAccess.getChannel(), () -> new MissingRequiredInputException("channel"));
//        ValidationUtils.checkNull(channelServiceAccess.getChannel().getId(), () -> new MissingRequiredInputException("channelId"));
//        ValidationUtils.checkNull(channelServiceAccess.getService(), () -> new MissingRequiredInputException("service"));
//        ValidationUtils.checkNull(channelServiceAccess.getService().getId(), () -> new MissingRequiredInputException("serviceId"));
//        ChannelServiceAccessEntity save = channelServiceAccessRepository.save(channelServiceAccessMapper.toEntity(channelServiceAccess));
//        clearCache();
//        return channelServiceAccessMapper.toModel(save);
//    }
//
//    private void clearCache() {
//        CACHE_ALL = List.of();
//        CACHE.clear();
//    }
//}
