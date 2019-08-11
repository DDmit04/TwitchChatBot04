package com.example.twitchchatbot.data;

import lombok.Data;

import javax.persistence.*;

@Data
@Table
@Entity
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String email;
    private String feedbackText;

}
