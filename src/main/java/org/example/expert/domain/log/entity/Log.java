package org.example.expert.domain.log.entity;

import org.example.expert.domain.common.entity.Timestamped;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "log")
public class Log extends Timestamped {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId;

	private Long managerId;

	private Long todoId;

	public Log(Long userId, Long managerId, Long todoId) {
		this.userId = userId;
		this.managerId = managerId;
		this.todoId = todoId;
	}
}
