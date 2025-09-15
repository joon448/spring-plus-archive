package org.example.expert.domain.todo.repository;

import java.util.Optional;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.repository.query.Param;

public interface TodoRepositoryCustom {
	Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);
}
