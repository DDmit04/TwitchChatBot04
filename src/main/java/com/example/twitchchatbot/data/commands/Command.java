package com.example.twitchchatbot.data.commands;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
        scope = Command.class,
        property = "id"
)
public class Command {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String commandCall;
    private String commandAction;
    private String commandCoolDown;
    private boolean commandDisabled = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "colling_command_instance_id")
    private CollingCommandInstance collingCommandInstance;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "regular_command_instance_id")
    private RegularCommandInstance regularCommandInstance;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "command_access_id", referencedColumnName = "id")
    private CommandAccess commandAccess;

}
