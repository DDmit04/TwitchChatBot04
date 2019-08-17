package com.example.twitchchatbot.repo;

import com.example.twitchchatbot.data.commands.Command;
import org.springframework.data.repository.CrudRepository;

public interface CommandRepo extends CrudRepository<Command, Long> {
}
