package org.example.expert.domain.comment.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

public class CommentIntegrationTest extends IntegrationTestSupport {
	@Test
	void Comment_생성을_성공한다() throws Exception{
		// given
		User user = saveUser("user@test.com", "pw", "user", UserRole.USER);
		Todo todo  = saveTodo("title", "contents", "Sunny", user);
		Long todoId = todo.getId();

		String body = """
			{
			  	"contents": "contents"
			}
			""";

		// when & then
		mockMvc.perform(post("/todos/{todoId}/comments", todoId)
			.contentType(MediaType.APPLICATION_JSON)
			.content(body)
			.with(addHeatherBearerToken(user))
			.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.contents").value("contents"))
			.andExpect(jsonPath("$.user.id").value(user.getId()))
			.andExpect(jsonPath("$.user.email").value("user@test.com"));
	}

	@Test
	void Comment_목록조회를_성공한다() throws Exception {
		// given
		User user = saveUser("user@test.com", "pw", "user", UserRole.USER);
		Todo todo  = saveTodo("title", "contents", "Sunny", user);
		Long todoId = todo.getId();
		Comment comment1 = saveComment("contents1", user, todo);
		Comment comment2 = saveComment("contents2", user, todo);

		// when & then
		mockMvc.perform(get("/todos/{todoId}/comments", todoId)
				.with(addHeatherBearerToken(user)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$", hasSize(2)))
			.andExpect(jsonPath("$[0].id").isNumber())
			.andExpect(jsonPath("$[0].contents").value("contents1"))
			.andExpect(jsonPath("$[0].user.email").value("user@test.com"))
			.andExpect(jsonPath("$[1].id").isNumber())
			.andExpect(jsonPath("$[1].contents").value("contents2"))
			.andExpect(jsonPath("$[1].user.email").value("user@test.com"));
	}

	@Test
	void Comment_목록조회시_Comment가_없으면_빈_목록을_반환한다() throws Exception {
		// given
		User user = saveUser("user@test.com", "pw", "user", UserRole.USER);
		Todo todo  = saveTodo("title", "contents", "Sunny", user);
		Long todoId = todo.getId();

		// when & then
		mockMvc.perform(get("/todos/{todoId}/comments", todoId)
				.with(addHeatherBearerToken(user)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$", hasSize(0)));
	}
}
