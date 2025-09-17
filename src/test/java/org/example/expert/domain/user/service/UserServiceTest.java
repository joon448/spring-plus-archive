package org.example.expert.domain.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
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
public class UserServiceTest {
	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserService userService;

	@Test
	public void getUser_성공_시_DTO_반환() {
		// given
		long userId = 1L;
		User user = new User("user@test.com", "pw", "user", UserRole.USER);
		ReflectionTestUtils.setField(user, "id", userId);
		given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));

		// when
		UserResponse userResponse = userService.getUser(userId);

		// then
		assertThat(userResponse).isNotNull();
		assertThat(userResponse.getId()).isEqualTo(userId);
		assertThat(userResponse.getEmail()).isEqualTo("user@test.com");
	}

	@Test
	public void getUser_유저_없을_시_400_반환() {
		// given
		long userId = 1L;
		given(userRepository.findById(eq(userId))).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> userService.getUser(userId))
			.isInstanceOf(InvalidRequestException.class)
			.hasMessage("User not found");
	}

	@Test
	public void changePassword_성공 (){
		// given
		long userId = 1L;
		// 비밀번호 규칙: 8자 이상, 숫자와 대문자 포함
		String oldPassword = "OldPassword1";
		String newPassword = "NewPassword1";
		String newHashedPassword = "newHashedPassword";
		UserChangePasswordRequest request = new UserChangePasswordRequest(oldPassword, newPassword);

		User user = new User("user@test.com", oldPassword, "user", UserRole.USER);
		ReflectionTestUtils.setField(user, "id", userId);
		given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));

		given(passwordEncoder.matches(eq(newPassword), eq(user.getPassword()))).willReturn(false);

		given(passwordEncoder.matches(eq(oldPassword), eq(user.getPassword()))).willReturn(true);

		given(passwordEncoder.encode(eq(request.getNewPassword()))).willReturn(newHashedPassword);

		// when
		userService.changePassword(userId, request);

		// then
		assertThat(user.getPassword()).isEqualTo(newHashedPassword);
	}

	@Test
	public void changePassword_비밀번호_규칙_위반_시_400_반환 (){
		// given
		long userId = 1L;
		// 비밀번호 규칙: 8자 이상, 숫자와 대문자 포함
		String oldPassword = "OldPassword1";
		String newPassword = "newpassword";
		UserChangePasswordRequest request = new UserChangePasswordRequest(oldPassword, newPassword);

		User user = new User("user@test.com", oldPassword, "user", UserRole.USER);
		ReflectionTestUtils.setField(user, "id", userId);

		// when & then
		assertThatThrownBy(() -> userService.changePassword(userId, request))
			.isInstanceOf(InvalidRequestException.class)
			.hasMessage("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.");
	}
	
	@Test
	public void changePassword_유저가_없을_시_400_반환 (){
		// given
		long userId = 1L;
		// 비밀번호 규칙: 8자 이상, 숫자와 대문자 포함
		String oldPassword = "OldPassword1";
		String newPassword = "NewPassword1";
		UserChangePasswordRequest request = new UserChangePasswordRequest(oldPassword, newPassword);

		given(userRepository.findById(eq(userId))).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(()->userService.changePassword(userId, request))
			.isInstanceOf(InvalidRequestException.class)
			.hasMessage("User not found");
	}

	@Test
	public void changePassword_새_비밀번호가_기존과_같을_시_400_반환 (){
		// given
		long userId = 1L;
		// 비밀번호 규칙: 8자 이상, 숫자와 대문자 포함
		String oldPassword = "OldPassword1";
		String newPassword = "OldPassword1";
		UserChangePasswordRequest request = new UserChangePasswordRequest(oldPassword, newPassword);

		User user = new User("user@test.com", oldPassword, "user", UserRole.USER);
		ReflectionTestUtils.setField(user, "id", userId);
		given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));

		given(passwordEncoder.matches(eq(newPassword), eq(user.getPassword()))).willReturn(true);

		// when & then
		assertThatThrownBy(()->userService.changePassword(userId, request))
			.isInstanceOf(InvalidRequestException.class)
			.hasMessage("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
	}

	@Test
	public void changePassword_입력한_비밀번호가_기존과_같지_않을_시_400_반환 (){
		// given
		long userId = 1L;
		// 비밀번호 규칙: 8자 이상, 숫자와 대문자 포함
		String oldPassword = "OldPassword1";
		String wrongPassword = "WrongPassword1";
		String newPassword = "NewPassword1";
		UserChangePasswordRequest request = new UserChangePasswordRequest(wrongPassword, newPassword);

		User user = new User("user@test.com", oldPassword, "user", UserRole.USER);
		ReflectionTestUtils.setField(user, "id", userId);
		given(userRepository.findById(eq(userId))).willReturn(Optional.of(user));

		given(passwordEncoder.matches(eq(newPassword), eq(user.getPassword()))).willReturn(false);

		given(passwordEncoder.matches(eq(wrongPassword), eq(user.getPassword()))).willReturn(false);

		// when & then
		assertThatThrownBy(()->userService.changePassword(userId, request))
			.isInstanceOf(InvalidRequestException.class)
			.hasMessage("잘못된 비밀번호입니다.");
	}
}
