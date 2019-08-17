package com.example.twitchchatbot.controller;

import ch.qos.logback.classic.Logger;
import com.example.twitchchatbot.data.Channel;
import com.example.twitchchatbot.data.User;
import com.example.twitchchatbot.repo.ChannelRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("channelsDataApi")
public class SaveChannelsDataController {

    private ChannelRepo channelRepo;

    @Autowired
    public SaveChannelsDataController(ChannelRepo channelRepo) {
        this.channelRepo = channelRepo;
    }


    @GetMapping("/{channelName}")
    private Channel getChannel(@PathVariable String channelName, @AuthenticationPrincipal User user) {
        return channelRepo.findByChannelName(channelName, user);
    }

    @PostMapping
    private Channel setChannel(@RequestBody Channel channel, @AuthenticationPrincipal User user) {
        channel.setChannelUser(user);
        channel.getCollingCommandInstance().getCommands().forEach(command -> {
            command.setCollingCommandInstance(null);
            command.setCollingCommandInstance(channel.getCollingCommandInstance());
        });
        channel.getRegularCommandInstance().getCommands().forEach(command -> {
            command.setRegularCommandInstance(null);
            command.setRegularCommandInstance(channel.getRegularCommandInstance());
        });
        return channelRepo.save(channel);
    }
}
