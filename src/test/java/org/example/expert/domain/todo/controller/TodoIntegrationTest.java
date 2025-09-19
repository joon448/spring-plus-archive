package org.example.expert.domain.todo.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

public class TodoIntegrationTest extends IntegrationTestSupport {
	@Autowired
	private WeatherClient weatherClient;

	@Test
	void Todo_생성을_성공한다() throws Exception {
		// given
		User user = saveUser("user@test.com", "pw", "user", UserRole.USER);

		TodoSaveRequest body = new TodoSaveRequest("title", "contents");

		// when & then
		mockMvc.perform(post("/todos")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(body))
			.with(addHeatherBearerToken(user))
			.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.title").value("title"))
			.andExpect(jsonPath("$.contents").value("contents"))
			.andExpect(jsonPath("$.weather").value(weatherClient.getTodayWeather()))
			.andExpect(jsonPath("$.user.id").value(user.getId()))
			.andExpect(jsonPath("$.user.email").value("user@test.com"));
	}

	@Test
	void Todo_조회조건이_없을_때_목록조회를_성공한다() throws Exception {
		// given
		User user = saveUser("user@test.com", "pw", "user", UserRole.USER);
		Todo todo  = saveTodo("title", "contents", "Sunny", user);
		Long todoId = todo.getId();

		// when & then
		mockMvc.perform(get("/todos")
			.with(addHeatherBearerToken(user)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content", hasSize(1)))
			.andExpect(jsonPath("$.content[0].id").value(todoId))
			.andExpect(jsonPath("$.content[0].title").value("title"))
			.andExpect(jsonPath("$.content[0].contents").value("contents"))
			.andExpect(jsonPath("$.content[0].weather").value("Sunny"))
			.andExpect(jsonPath("$.content[0].user.id").value(user.getId()))
			.andExpect(jsonPath("$.page.totalPages").value(1))
			.andExpect(jsonPath("$.page.totalElements").value(1))
			.andExpect(jsonPath("$.page.number").value(0))
			.andExpect(jsonPath("$.page.size").value(10));
	}

	@Test
	void Todo_조회조건이_있을_때_목록조회를_성공한다() throws Exception {
		// given
		User user = saveUser("user@test.com", "pw", "user", UserRole.USER);
		Todo todo  = saveTodo("title", "contents", "Sunny", user);
		Todo todo2  = saveTodo("title2", "contents2", "Cloudy", user);
		Long todoId = todo.getId();

		// when & then
		mockMvc.perform(get("/todos")
				.param("page", "1")
				.param("size", "10")
				.param("start", LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE))
				.param("end", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
				.param("weather", "Sunny")
				.with(addHeatherBearerToken(user)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content", hasSize(1)))
			.andExpect(jsonPath("$.content[0].id").value(todoId))
			.andExpect(jsonPath("$.content[0].title").value("title"))
			.andExpect(jsonPath("$.content[0].contents").value("contents"))
			.andExpect(jsonPath("$.content[0].weather").value("Sunny"))
			.andExpect(jsonPath("$.content[0].user.id").value(user.getId()))
			.andExpect(jsonPath("$.page.totalPages").value(1))
			.andExpect(jsonPath("$.page.totalElements").value(1))
			.andExpect(jsonPath("$.page.number").value(0))
			.andExpect(jsonPath("$.page.size").value(10));
	}

	@Test
	void 단일_Todo_조회를_성공한다() throws Exception {
		// given
		User user = saveUser("user@test.com", "pw", "user", UserRole.USER);
		Todo todo  = saveTodo("title", "contents", "Sunny", user);
		Long todoId = todo.getId();

		// when & then
		mockMvc.perform(get("/todos/{todoId}", todoId).with(addHeatherBearerToken(user)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.title").value("title"))
			.andExpect(jsonPath("$.contents").value("contents"))
			.andExpect(jsonPath("$.weather").value("Sunny"))
			.andExpect(jsonPath("$.user.id").value(user.getId()))
			.andExpect(jsonPath("$.user.email").value(user.getEmail()));
	}

	@Test
	void 단일_Todo를_조회할_때_Todo가_없으면_400_반환() throws Exception {
		// given
		Long todoId = 999L;

		User user = saveUser("user@test.com", "pw", "user", UserRole.USER);

		// when & then
		mockMvc.perform(get("/todos/{todoId}", todoId).with(addHeatherBearerToken(user)))
			.andExpect(status().isBadRequest());
	}

	@Test
	void Todo_검색조건이_없을_때_목록검색을_성공한다() throws Exception {
		// given
		User user = saveUser("user@test.com", "pw", "user", UserRole.USER);
		Todo todo  = saveTodo("title", "contents", "Sunny", user);

		// when & then
		mockMvc.perform(get("/todos/search")
				.with(addHeatherBearerToken(user)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content", hasSize(1)))
			.andExpect(jsonPath("$.content[0].title").value("title"))
			.andExpect(jsonPath("$.content[0].managerCount").value(1))
			.andExpect(jsonPath("$.content[0].commentCount").value(0))
			.andExpect(jsonPath("$.page.totalPages").value(1))
			.andExpect(jsonPath("$.page.totalElements").value(1))
			.andExpect(jsonPath("$.page.number").value(0))
			.andExpect(jsonPath("$.page.size").value(10));
	}

	@Test
	void Todo_검색조건이_있을_때_목록검색을_성공한다() throws Exception {
		// given
		User user = saveUser("user@test.com", "pw", "user", UserRole.USER);
		Todo todo  = saveTodo("title", "contents", "Sunny", user);
		Todo todo2  = saveTodo("title2", "contents2", "Cloudy", user);

		// when & then
		mockMvc.perform(get("/todos/search")
				.param("page", "1")
				.param("size", "10")
				.param("title", "title") // 'title' like로 검색
				.param("start", LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE))
				.param("end", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
				.param("nickname", "user") // 'user' like로 검색
				.with(addHeatherBearerToken(user)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isArray())
			.andExpect(jsonPath("$.content", hasSize(2)))
			.andExpect(jsonPath("$.content[*].title",
				containsInAnyOrder("title", "title2")))
			.andExpect(jsonPath("$.page.totalPages").value(1))
			.andExpect(jsonPath("$.page.totalElements").value(2))
			.andExpect(jsonPath("$.page.number").value(0))
			.andExpect(jsonPath("$.page.size").value(10));
	}
}
