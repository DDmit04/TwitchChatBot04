package com.example.twitchchatbot.repo;

import com.example.twitchchatbot.data.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, String>{

}
