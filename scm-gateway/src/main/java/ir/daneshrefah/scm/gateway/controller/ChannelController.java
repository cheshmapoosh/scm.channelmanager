package ir.daneshrefah.scm.gateway.controller;

import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.core.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-15
 */
@RestController
@RequestMapping("/channel")
public class ChannelController extends AbstractController {

    @Autowired
    private ChannelService channelService;

    @GetMapping
    public List<Channel> getChannelList() {
//        return "Hello from Spring MVC Controller!";
        return channelService.findAllChannelList();
    }

    @GetMapping("/paged")
    public ResponseEntity<PagingResponse<Channel>> getChannelPage(Pageable pageable) {
        Page<Channel> channels = channelService.findPagedChannelList(pageable);
        PagingResponse<Channel> response = createPagingResponse(channels.getContent(), pageable, channels.getTotalElements());
        return ResponseEntity.ok(response);
    }
}
