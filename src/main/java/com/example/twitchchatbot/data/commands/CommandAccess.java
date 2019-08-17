package com.example.twitchchatbot.data.commands;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Data
@EqualsAndHashCode(of = {"id"})
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        scope = CommandAccess.class,
        property = "id"
)
public class CommandAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private boolean moderator;
    private boolean subscriber;
    private boolean unSubscriber;

    @OneToOne(mappedBy = "commandAccess")
    private Command command;

}

