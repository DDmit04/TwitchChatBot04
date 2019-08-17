package com.example.twitchchatbot.data.commands;

import com.example.twitchchatbot.data.Channel;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        scope = CollingCommandInstance.class,
        property = "id"
)
@Data
public class CollingCommandInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String commandCallInput;
    private String commandActionInput;
    private String commandCoolDownInput;
    private String selectedCommand;

    @OneToOne(mappedBy = "collingCommandInstance")
    private Channel channel;

    @OneToMany(mappedBy = "collingCommandInstance", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Command> commands = new ArrayList<>();

}