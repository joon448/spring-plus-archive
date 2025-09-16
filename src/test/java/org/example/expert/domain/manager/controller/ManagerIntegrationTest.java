package org.example.expert.domain.manager.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

public class ManagerIntegrationTest extends IntegrationTestSupport {
	@Test
	void Manager_생성을_성공한다() throws Exception {
		// given
		User owner = saveUser("owner@test.com", "pw", "owner", UserRole.USER);
		User managerUser = saveUser("manager@test.com", "pw", "manager", UserRole.USER);
		Todo todo = saveTodo("title", "contents", "Sunny", owner);

		ManagerSaveRequest body = new ManagerSaveRequest(managerUser.getId());

		// when & then
		mockMvc.perform(post("/todos/{todoId}/managers",todo.getId())
				.with(addHeatherBearerToken(owner))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body))
				.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.user.id").value(managerUser.getId()))
			.andExpect(jsonPath("$.user.email").value(managerUser.getEmail()));
	}

	@Test
	void Manager_목록조회를_성공한다() throws Exception {
		// given
		User owner = saveUser("owner@test.com", "pw", "owner", UserRole.USER);
		User managerUser1 = saveUser("manager1@test.com", "pw", "manager1", UserRole.USER);
		User managerUser2 = saveUser("manager2@test.com", "pw", "manager2", UserRole.USER);
		Todo todo = saveTodo("title", "contents", "Sunny", owner);

		Manager manager1 = saveManager(managerUser1, todo);
		Manager manager2 = saveManager(managerUser2, todo);

		// when & then
		mockMvc.perform(get("/todos/{todoId}/managers",todo.getId())
				.with(addHeatherBearerToken(owner)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$", hasSize(3)))
			.andExpect(jsonPath("$[*].user.email",
				containsInAnyOrder(owner.getEmail(), managerUser1.getEmail(), managerUser2.getEmail())));
	}

	@Test
	void Manager_삭제를_성공한다() throws Exception {
		// given
		User owner = saveUser("owner@test.com", "pw", "owner", UserRole.USER);
		User managerUser = saveUser("manager@test.com", "pw", "manager", UserRole.USER);
		Todo todo = saveTodo("title", "contents", "Sunny", owner);
		Manager manager = saveManager(managerUser, todo);

		mockMvc.perform(delete("/todos/{todoId}/managers/{managerId}", todo.getId(), manager.getId())
				.with(addHeatherBearerToken(owner))
				.with(csrf()))
			.andExpect(status().isOk());
	}
}
