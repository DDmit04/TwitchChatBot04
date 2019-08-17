package com.example.twitchchatbot.data.commands;

import com.example.twitchchatbot.data.Channel;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(of = {"id"})
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        scope = RegularCommandInstance.class,
        property = "id"
)
public class RegularCommandInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String commandCallInput;
    private String commandActionInput;
    private String commandCoolDownInput;
    private String selectedCommand;

    @OneToOne(mappedBy = "regularCommandInstance")
    private Channel channel;

    @OneToMany(mappedBy = "regularCommandInstance", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Command> commands = new ArrayList<>();
}