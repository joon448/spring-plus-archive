package org.example.expert.domain.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class UserAdminServiceTest {
	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserAdminService userAdminService;

	@Test
	public void changeUserRole을_성공한다() {
		// given
		long userId = 1L;
		String userRole = "ADMIN";

		User user = new User("user@test.com", "pw", "user", UserRole.USER);
		ReflectionTestUtils.setField(user, "id", userId);

		UserRoleChangeRequest request = new UserRoleChangeRequest(userRole);

		given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));

		// when
		userAdminService.changeUserRole(userId, request);

		// then
		assertThat(user.getUserRole().name()).isEqualTo(userRole);
	}

	@Test
	public void changeUserRole_유저_없을_시_400_반환() {
		// given
		long userId = 1L;
		String userRole = "ADMIN";

		UserRoleChangeRequest request = new UserRoleChangeRequest(userRole);

		given(userRepository.findById(eq(userId))).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> userAdminService.changeUserRole(userId, request))
			.isInstanceOf(InvalidRequestException.class)
			.hasMessage("User not found");
	}
}
