package org.example.expert.domain.user.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserService;
import org.example.expert.support.ControllerTestSupport;
import org.example.expert.support.WithMockAuthUser;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;

@WebMvcTest(UserController.class)
public class UserControllerTest extends ControllerTestSupport {

	@MockBean
	private UserService userService;

	@Test
	@WithMockAuthUser(userId = 1L, email = "admin@example.com", nickname = "admin", userRole = UserRole.ADMIN)
	void getUser에_성공한다() throws Exception {
		// given
		long userId = 1L;
		User user = new User("user@test.com", "pw", "user", UserRole.USER);
		ReflectionTestUtils.setField(user, "id", userId);
		UserResponse userResponse = new UserResponse(user.getId(), user.getEmail());

		given(userService.getUser(userId)).willReturn(userResponse);

		// when & then
		mockMvc.perform(get("/users/{userId}", userId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(user.getId()))
			.andExpect(jsonPath("$.email").value(user.getEmail()));
	}

	@Test
	@WithMockAuthUser(userId = 1L, email = "admin@example.com", nickname = "admin", userRole = UserRole.ADMIN)
	void user_단건_조회_시_유저가_존재하지_않아_예외가_발생한다() throws Exception {
		// given
		long userId = 1L;

		given(userService.getUser(userId)).willThrow(new InvalidRequestException("User not found"));

		// when & then
		mockMvc.perform(get("/users/{userId}", userId))
			.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockAuthUser(userId = 1L, email = "admin@example.com", nickname = "admin", userRole = UserRole.ADMIN)
	void changePassword에_성공한다() throws Exception {
		// given
		long userId = 1L;
		String oldPassword = "OldPassword1";
		String newPassword = "Newpassword1";
		UserChangePasswordRequest request = new UserChangePasswordRequest(oldPassword, newPassword);

		willDoNothing().given(userService).changePassword(eq(userId), any(UserChangePasswordRequest.class));

		// when & then
		mockMvc.perform(put("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(content().string(""));
	}

	@Test
	@WithMockAuthUser(userId = 1L, email = "admin@example.com", nickname = "admin", userRole = UserRole.ADMIN)
	void changePassword_규칙위반_시_400_반환 () throws Exception {
		// given
		long userId = 1L;
		String oldPassword = "OldPassword1";
		String newPassword = "newpassword";
		UserChangePasswordRequest request = new UserChangePasswordRequest(oldPassword, newPassword);

		willThrow(new InvalidRequestException("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다."))
			.given(userService).changePassword(eq(userId), any(UserChangePasswordRequest.class));

		// when & then
		mockMvc.perform(put("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.with(csrf()))
			.andExpect(status().isBadRequest());
	}
}
