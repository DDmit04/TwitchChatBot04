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
        Channel loadedChannel = null;
        if(user != null) {
            loadedChannel = channelRepo.findByChannelName(channelName, user);
        }
        return loadedChannel;
    }

    @PostMapping
    private Channel setChannel(@RequestBody Channel channel, @AuthenticationPrincipal User user) {
        Channel savedChannel = null;
        if(user != null) {
            if (channel.getChannelUser() == null) {
                channel.setChannelUser(user);
            }
            channel.getCollingCommandInstance().getCommands().forEach(command -> {
                command.setCollingCommandInstance(channel.getCollingCommandInstance());
            });
            channel.getRegularCommandInstance().getCommands().forEach(command -> {
                command.setRegularCommandInstance(channel.getRegularCommandInstance());
            });
            savedChannel = channelRepo.save(channel);
        }
        return savedChannel;
    }
}
