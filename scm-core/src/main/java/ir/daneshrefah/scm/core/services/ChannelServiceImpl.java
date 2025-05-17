//package ir.daneshrefah.scm.core.services;
//
//import com.fasterxml.jackson.databind.node.NullNode;
//import ir.daneshrefah.scm.common.data.entity.terminal.TerminalEntity;
//import ir.daneshrefah.scm.common.data.repository.TerminalRepository;
//import ir.daneshrefah.scm.common.dto.channel.ChannelCreateRequest;
//import ir.daneshrefah.scm.common.dto.channel.ChannelDeleteRequest;
//import ir.daneshrefah.scm.common.dto.channel.ChannelEditRequest;
//import ir.daneshrefah.scm.common.dto.channel.ChannelFindRequest;
//import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
//import ir.daneshrefah.scm.common.exception.DuplicatedRecordFoundException;
//import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
//import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
//import ir.daneshrefah.scm.common.exception.RecordVersionException;
//import ir.daneshrefah.scm.common.model.terminal.Channel;
//import ir.daneshrefah.scm.common.service.channel.ChannelService;
//import ir.daneshrefah.scm.core.entity.terminal.ChannelEntity;
//import ir.daneshrefah.scm.core.mapper.ChannelMapper;
//import ir.daneshrefah.scm.core.repository.ChannelRepository;
//import ir.daneshrefah.scm.utils.string.StringUtils;
//import ir.daneshrefah.scm.utils.validation.ValidationUtils;
//import lombok.RequiredArgsConstructor;
//import org.springframework.orm.ObjectOptimisticLockingFailureException;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Objects;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@RequiredArgsConstructor
//@Service
//public class ChannelServiceImpl implements ChannelService {
//
//    private final ChannelRepository channelRepository;
//    private final TerminalRepository terminalRepository;
//    private List<Channel> channels;
//
//    @Override
//    public List<Channel> findAllChannels() {
//        if (null == channels || channels.isEmpty()) {
//            synchronized (this) {
//                channels = ChannelMapper.INSTANCE.entitiesToModels(channelRepository.findAll());
//            }
//        }
//        return channels;
//    }
//
//    private void cleanChannelCacheList() {
//        if (Objects.nonNull(this.channels)){
//            this.channels.clear();
//        }
//    }
//
//    @Override
//    public Optional<Channel> findChannelById(String id) {
//        if (StringUtils.isEmpty(id)) {
//            return Optional.empty();
//        }
//        return channelRepository.findById(id).map(ChannelMapper.INSTANCE::toModel);
//    }
//
//    @Override
//    public Optional<Channel> findChannelByCode(String code) {
//        if (StringUtils.isEmpty(code)) {
//            return Optional.empty();
//        }
//        return findAllChannels().stream().filter(channel -> code.equals(channel.getCode())).findFirst();
//    }
//
//    @Override
//    public PagedResponseData<Channel> findPagedChannels(ChannelFindRequest request) {
//        List<Channel> channelList = findAllChannels().stream()
//                .filter(channel -> Objects.isNull(request) || StringUtils.isEmpty(request.getCode()) || request.getCode().equalsIgnoreCase(channel.getCode()))
//                .filter(channel -> Objects.isNull(request) || StringUtils.isEmpty(request.getTerminalCode()) || request.getTerminalCode().equalsIgnoreCase(channel.getTerminal().getCode()))
//                .filter(channel -> Objects.isNull(request) || Objects.isNull(request.getProtocol()) || request.getProtocol().equals(channel.getProtocol()))
//                .filter(channel -> Objects.isNull(request) || StringUtils.isEmpty(request.getTitle()) || (StringUtils.isEmpty(channel.getTitle()) ? StringUtils.EMPTY : channel.getTitle()).toLowerCase().contains(request.getTitle().toLowerCase()))
//                .filter(channel -> Objects.isNull(request) || StringUtils.isEmpty(request.getCreator()) || (StringUtils.isEmpty(channel.getCreator()) ? StringUtils.EMPTY : channel.getCreator()).toLowerCase().contains(request.getCreator().toLowerCase()))
//                .filter(channel -> Objects.isNull(request) || StringUtils.isEmpty(request.getLastEditor()) || (StringUtils.isEmpty(channel.getLastEditor()) ? StringUtils.EMPTY : channel.getLastEditor()).toLowerCase().contains(request.getLastEditor().toLowerCase()))
//                .collect(Collectors.toList());
//        return new PagedResponseData<>(request, channelList);
//    }
//
//    @Override
//    public Channel createChannel(ChannelCreateRequest request) {
//        validateCreateChannelRequest(request);
//        TerminalEntity terminalEntity = terminalRepository
//                .findByCode(request.getTerminalCode())
//                .orElseThrow(() -> new NoMatchRecordFoundException("terminal"));
//        ChannelEntity channelEntity = mapToChannelEntity(request, terminalEntity);
//        ChannelEntity saved = channelRepository.save(channelEntity);
//        cleanChannelCacheList();
//        return findChannelById(saved.getId()).orElse(null);
//    }
//
//    @Override
//    public void deleteChannel(ChannelDeleteRequest request) {
//        validateDeleteChannelRequest(request);
//        channelRepository.findById(request.getId())
//                .ifPresentOrElse(channelEntity -> {
//                    if (!channelEntity.getLastEditDate().equals(request.getLastEditDate())) {
//                        throw new RecordVersionException("channel");
//                    }
//                    try {
//                        channelRepository.delete(channelEntity);
//                    } catch (ObjectOptimisticLockingFailureException e) {
//                        throw new RecordVersionException("channel");
//                    }
//                    cleanChannelCacheList();
//                }, () -> {
//                    throw new NoMatchRecordFoundException("channel");
//                });
//    }
//
//    @Override
//    public Channel editChannel(ChannelEditRequest request) {
//        validateEditChannelRequest(request);
//        ChannelEntity foundChannel = channelRepository
//                .findById(request.getId()).orElseThrow(() -> new NoMatchRecordFoundException("channel"));
//        fillDynamicUpdateProperties(foundChannel, request);
//        try {
//            channelRepository.save(foundChannel);
//        } catch (ObjectOptimisticLockingFailureException e) {
//            throw new RecordVersionException("channel");
//        }
//        cleanChannelCacheList();
//        return findChannelById(request.getId()).orElse(null);
//    }
//
//    private void fillDynamicUpdateProperties(ChannelEntity foundChannel, ChannelEditRequest request) {
//        foundChannel.setLastEditDate(request.getLastEditDate());
//        if (Objects.nonNull(request.getProtocol())) {
//            foundChannel.setProtocol(request.getProtocol());
//        }
//        if (StringUtils.isNotEmpty(request.getCode()) && !foundChannel.getCode().equalsIgnoreCase(request.getCode())) {
//            channelRepository.findByCode(request.getCode()).ifPresent(channelEntity -> {
//                throw new DuplicatedRecordFoundException("channel");
//            });
//            foundChannel.setCode(request.getCode());
//        }
//        if (StringUtils.isNotEmpty(request.getTitle()) && !foundChannel.getTitle().equals(request.getTitle())) {
//            foundChannel.setTitle(request.getTitle());
//        }
//        if (StringUtils.isNotEmpty(request.getChannelClassName()) && !foundChannel.getChannelClassName().equals(request.getChannelClassName())) {
//            foundChannel.setChannelClassName(request.getChannelClassName());
//        }
//        if (Objects.nonNull(request.getMetadata()) && !foundChannel.getMetadata().equals(request.getMetadata())) {
//            foundChannel.setMetadata(request.getMetadata());
//        }
//        if (StringUtils.isNotEmpty(request.getTerminalCode()) && !foundChannel.getTerminal().getCode().equalsIgnoreCase(request.getTerminalCode())) {
//            TerminalEntity foundTerminal = terminalRepository.findByCode(request.getTerminalCode().toUpperCase()).orElseThrow(() -> new NoMatchRecordFoundException("terminal"));
//            foundChannel.setTerminal(foundTerminal);
//        }
//
//    }
//
//    private void validateEditChannelRequest(ChannelEditRequest request) {
//        String id = request.getId();
//        LocalDateTime lastEditDate = request.getLastEditDate();
//        ValidationUtils.checkBlankString(id, () -> new MissingRequiredInputException("id"));
//        ValidationUtils.checkNull(lastEditDate, () -> new MissingRequiredInputException("lastEditDate"));
//    }
//
//    private void validateDeleteChannelRequest(ChannelDeleteRequest request) {
//        String id = request.getId();
//        LocalDateTime lastEditDate = request.getLastEditDate();
//        ValidationUtils.checkBlankString(id, () -> new MissingRequiredInputException("id"));
//        ValidationUtils.checkNull(lastEditDate, () -> new MissingRequiredInputException("lastEditDate"));
//    }
//
//    private ChannelEntity mapToChannelEntity(ChannelCreateRequest request, TerminalEntity terminalEntity) {
//        ChannelEntity channelEntity = new ChannelEntity();
//        channelEntity.setTerminal(terminalEntity);
//        channelEntity.setCode(request.getCode());
//        channelEntity.setTitle(request.getTitle());
//        channelEntity.setProtocol(request.getProtocol());
//        channelEntity.setChannelClassName(request.getChannelClassName());
//        channelEntity.setMetadata(Objects.nonNull(request.getMetadata()) ? request.getMetadata() : NullNode.getInstance());
//        return channelEntity;
//    }
//
//
//    private void validateCreateChannelRequest(ChannelCreateRequest request) {
//        channelRepository.findByCode(request.getCode()).ifPresent(channelEntity -> {
//            throw new DuplicatedRecordFoundException("channel");
//        });
//        request.setTerminalCode(request.getTerminalCode().toUpperCase());
//    }
//}
