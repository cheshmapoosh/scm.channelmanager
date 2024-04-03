package ir.daneshrefah.scm.gateway.controller;

import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.service.channel.ChannelFindRequest;
import ir.daneshrefah.scm.common.service.channel.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
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
        return channelService.findAllChannels();
    }

    @GetMapping("/paged")
    public ResponseEntity<PagedResponseData<Channel>> getChannelPage(ChannelFindRequest request) {
        PagedResponseData<Channel> channels = channelService.findPagedChannels(request);
        return ResponseEntity.ok(channels);
    }
}
