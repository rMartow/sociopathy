package com.example.sociopathy.repo;

import com.example.sociopathy.model.Message;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MessageRepo extends CrudRepository<Message, Long> {
    List<Message> findByTag(String tag);
    List<Message> findAll();
}