package com.example.twitchchatbot.data;

import com.example.twitchchatbot.data.commands.CollingCommandInstance;
import com.example.twitchchatbot.data.commands.RegularCommandInstance;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Entity
@Data
@EqualsAndHashCode(of = {"id"})
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        scope = Channel.class,
        property = "id"
)
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String channelName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User channelUser;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "colling_commands_instance_id")
    private CollingCommandInstance collingCommandInstance;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "regular_commands_instance_id")
    private RegularCommandInstance regularCommandInstance;

}
