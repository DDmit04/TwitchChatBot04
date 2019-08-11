package com.example.twitchchatbot.controller;

import com.example.twitchchatbot.data.Feedback;
import com.example.twitchchatbot.repo.FeedbackRepo;
import com.example.twitchchatbot.service.MailSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Pattern;

@RestController
@RequestMapping("feedback")
public class FeedbackController {

    private static final Pattern checkEmail = Pattern.compile(
            "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$"
    );

    private MailSenderService mailSender;
    private FeedbackRepo feedbackRepo;

    @Autowired
    public FeedbackController(MailSenderService mailSender, FeedbackRepo feedbackRepo) {
        this.mailSender = mailSender;
        this.feedbackRepo = feedbackRepo;
    }

    @PostMapping()
    public Feedback getFeedback(@RequestBody Feedback feedback) {
        Feedback savedFeedback = feedbackRepo.save(feedback);
        if(!feedback.getFeedbackText().equals("")
                && !feedback.getEmail().equals("")
                && checkEmail.matcher(feedback.getEmail()).matches()) {
            mailSender.send("orderly chaos feedback from " + feedback.getEmail(), feedback.getFeedbackText());
        }
        return savedFeedback;
    }
}
