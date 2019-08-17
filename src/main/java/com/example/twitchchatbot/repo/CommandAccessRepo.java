package com.example.twitchchatbot.repo;

import com.example.twitchchatbot.data.commands.CommandAccess;
import org.springframework.data.repository.CrudRepository;

public interface CommandAccessRepo extends CrudRepository<CommandAccess, Long> {
}
