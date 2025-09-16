package org.example.expert.domain.comment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.service.CommentService;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.support.ControllerTestSupport;
import org.example.expert.support.WithMockAuthUser;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.TestSecurityContextHolder;

@WebMvcTest(CommentController.class)
public class CommentControllerTest extends ControllerTestSupport {

	@MockBean
	private CommentService commentService;

	@Test
	@WithMockAuthUser(userId = 1L, email = "admin@example.com", nickname = "admin", userRole = UserRole.ADMIN)
	void comment_생성에_성공한다() throws Exception {
		// given
		long todoId = 1L;
		long commentId = 1L;
		AuthUser authUser = (AuthUser) TestSecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User user = User.fromAuthUser(authUser);
		CommentSaveRequest commentSaveRequest = new CommentSaveRequest("contents");
		UserResponse userResponse = new UserResponse(user.getId(), user.getEmail());
		CommentSaveResponse response = new CommentSaveResponse(
			commentId,
			"contents",
			userResponse
		);
		given(commentService.saveComment(any(AuthUser.class), eq(todoId), any(CommentSaveRequest.class)))
			.willReturn(response);

		// when & then
		mockMvc.perform(post("/todos/{todoId}/comments", todoId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(commentSaveRequest))
				.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(commentId))
			.andExpect(jsonPath("$.contents").value("contents"))
			.andExpect(jsonPath("$.user.email").value("admin@example.com"));;
	}

	@Test
	@WithMockAuthUser(userId = 1L, email = "admin@example.com", nickname = "admin", userRole = UserRole.ADMIN)
	void getComments_성공_시_List_DTO_반환() throws Exception {
		// given
		long todoId = 10L;

		UserResponse user1 = new UserResponse(1L, "user1@test.com");
		UserResponse user2 = new UserResponse(2L, "user2@test.com");

		CommentResponse comment1 = new CommentResponse(1L, "contents1", user1);
		CommentResponse comment2 = new CommentResponse(2L, "contents2", user2);

		given(commentService.getComments(eq(todoId))).willReturn(List.of(comment1, comment2));

		// when & then
		mockMvc.perform(get("/todos/{todoId}/comments", todoId))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$[0].id").value(1))
			.andExpect(jsonPath("$[0].contents").value("contents1"))
			.andExpect(jsonPath("$[0].user.email").value("user1@test.com"))
			.andExpect(jsonPath("$[1].id").value(2))
			.andExpect(jsonPath("$[1].contents").value("contents2"))
			.andExpect(jsonPath("$[1].user.email").value("user2@test.com"));
	}
}
