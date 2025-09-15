package org.example.expert.domain.todo.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface TodoRepositoryCustom {
	Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);

	Page<TodoSearchResponse> searchTodosOrderByCreatedAtDesc(String title, LocalDateTime startTime, LocalDateTime endTimeExclusive, String managerNickname, Pageable pageable);
}
