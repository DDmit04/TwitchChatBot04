package com.example.twitchchatbot.repo;

import com.example.twitchchatbot.data.Channel;
import com.example.twitchchatbot.data.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ChannelRepo extends CrudRepository<Channel, Long> {

    @Query("from Channel ch " +
            "where ch.channelName = :channelName and ch.channelUser = :user " +
            "group by ch")
    Channel findByChannelName(@Param("channelName") String channelName, @Param("user") User user);

}
