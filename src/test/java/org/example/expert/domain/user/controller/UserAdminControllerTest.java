package org.example.expert.domain.user.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.BDDMockito.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserAdminService;
import org.example.expert.support.ControllerTestSupport;
import org.example.expert.support.WithMockAuthUser;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

@WebMvcTest(UserAdminController.class)
public class UserAdminControllerTest extends ControllerTestSupport {

	@MockBean
	private UserAdminService userAdminService;

	@Test
	@WithMockAuthUser(userId = 1L, email = "admin@example.com", nickname = "admin", userRole = UserRole.ADMIN)
	void ChangeUserRole에_성공한다() throws Exception {
		// given
		long userId = 1L;
		String userRole = "USER";
		UserRoleChangeRequest request = new UserRoleChangeRequest(userRole);

		willDoNothing().given(userAdminService).changeUserRole(eq(userId), any(UserRoleChangeRequest.class));

		// when & then
		mockMvc.perform(patch("/admin/users/{userId}", userId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(content().string(""));
	}

	@Test
	@WithMockAuthUser(userId = 1L, email = "admin@example.com", nickname = "admin", userRole = UserRole.ADMIN)
	void changeUserRole_유저가_없을_시_400_반환 () throws Exception {
		// given
		long userId = 1L;
		String userRole = "USER";
		UserRoleChangeRequest request = new UserRoleChangeRequest(userRole);

		willThrow(new InvalidRequestException("User not found"))
			.given(userAdminService).changeUserRole(eq(userId), any(UserRoleChangeRequest.class));

		// when & then
		mockMvc.perform(patch("/admin/users/{userId}", userId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.with(csrf()))
			.andExpect(status().isBadRequest());
	}
}
