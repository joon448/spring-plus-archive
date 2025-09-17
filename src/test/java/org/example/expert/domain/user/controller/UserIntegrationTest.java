package org.example.expert.domain.user.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserIntegrationTest extends IntegrationTestSupport {
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void getUser를_성공한다() throws Exception {
		// given
		User authUser = saveUser("authUser@test.com", "pw", "authUser", UserRole.USER);
		User user = saveUser("user@test.com", "pw", "user", UserRole.USER);

		// when & then
		mockMvc.perform(get("/users/{userId}", user.getId())
				.with(addHeatherBearerToken(authUser)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(user.getId()))
			.andExpect(jsonPath("$.email").value(user.getEmail()));
	}

	@Test
	void getUser_유저가_없을_시_400_반환 () throws Exception {
		// given
		User authUser = saveUser("authUser@test.com", "pw", "authUser", UserRole.USER);
		long userId = 999L;

		// when & then
		mockMvc.perform(get("/users/{userId}", userId)
				.with(addHeatherBearerToken(authUser)))
			.andExpect(status().isBadRequest());
	}

	@Test
	void changePassword를_성공한다() throws Exception {
		// given
		String oldPassword = "OldPassword1";
		String newPassword = "NewPassword1";
		String oldHashedPassword = passwordEncoder.encode(oldPassword);

		User authUser = saveUser("authUser@test.com", oldHashedPassword, "authUser", UserRole.USER);

		UserChangePasswordRequest request = new UserChangePasswordRequest(oldPassword, newPassword);

		// when & then
		mockMvc.perform(put("/users")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(request))
			.with(addHeatherBearerToken(authUser))
			.with(csrf()))
			.andExpect(status().isOk());
	}

	@Test
	void changePassword_규칙위반_시_400_반환 () throws Exception {
		// given
		String oldPassword = "OldPassword1";
		String newPassword = "newPassword";
		String oldHashedPassword = passwordEncoder.encode(oldPassword);

		User authUser = saveUser("authUser@test.com", oldHashedPassword, "authUser", UserRole.USER);

		UserChangePasswordRequest request = new UserChangePasswordRequest(oldPassword, newPassword);

		// when & then
		mockMvc.perform(put("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.with(addHeatherBearerToken(authUser))
				.with(csrf()))
			.andExpect(status().isBadRequest());
	}

	@Test
	void changePassword_검증실패_시_400_반환 () throws Exception {
		// given
		String oldPassword = "OldPassword1";
		String newPassword = "NewPassword1";
		String wrongPassword = "wrongPassword";
		String oldHashedPassword = passwordEncoder.encode(oldPassword);

		User authUser = saveUser("authUser@test.com", oldHashedPassword, "authUser", UserRole.USER);

		UserChangePasswordRequest request = new UserChangePasswordRequest(wrongPassword, newPassword);

		// when & then
		mockMvc.perform(put("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
				.with(addHeatherBearerToken(authUser))
				.with(csrf()))
			.andExpect(status().isBadRequest());
	}
}
