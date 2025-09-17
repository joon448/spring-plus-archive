package org.example.expert.domain.user.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserAdminIntegrationTest extends IntegrationTestSupport {
	@Test
	void changeUserRole를_성공한다() throws Exception {
		// given
		String userRole = "USER";
		User authUser = saveUser("authUser@test.com", "pw", "authUser", UserRole.ADMIN);
		User user = saveUser("user@test.com", "pw", "user", UserRole.ADMIN);

		UserRoleChangeRequest request = new UserRoleChangeRequest(userRole);

		// when & then
		mockMvc.perform(patch("/admin/users/{users}", user.getId())
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(request))
			.with(addHeatherBearerToken(authUser))
			.with(csrf()))
			.andExpect(status().isOk());
	}

	@Test
	void changeUserRole_유저_접근권한_검증실패_시_400_반환 () throws Exception {
		// given
		long userId = 999L;
		String userRole = "USER";
		User authUser = saveUser("authUser@test.com", "pw", "authUser", UserRole.USER);

		UserRoleChangeRequest request = new UserRoleChangeRequest(userRole);

		// when & then
		mockMvc.perform(patch("/admin/users/{users}", userId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.with(addHeatherBearerToken(authUser))
				.with(csrf()))
			.andExpect(status().isForbidden());
	}

	@Test
	void changeUserRole_유저가_없을_시_400_반환 () throws Exception {
		// given
		long userId = 999L;
		String userRole = "USER";
		User authUser = saveUser("authUser@test.com", "pw", "authUser", UserRole.ADMIN);

		UserRoleChangeRequest request = new UserRoleChangeRequest(userRole);

		// when & then
		mockMvc.perform(patch("/admin/users/{users}", userId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.with(addHeatherBearerToken(authUser))
				.with(csrf()))
			.andExpect(status().isBadRequest());
	}
}