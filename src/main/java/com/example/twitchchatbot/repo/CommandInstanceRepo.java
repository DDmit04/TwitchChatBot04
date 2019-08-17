package com.example.twitchchatbot.repo;

import com.example.twitchchatbot.data.commands.CollingCommandInstance;
import org.springframework.data.repository.CrudRepository;

public interface CommandInstanceRepo extends CrudRepository<CollingCommandInstance, Long> {
}
