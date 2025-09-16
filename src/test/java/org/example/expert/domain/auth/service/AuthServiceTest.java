package org.example.expert.domain.auth.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.example.expert.config.JwtUtil;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
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
public class AuthServiceTest {
	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtUtil jwtUtil;

	@InjectMocks
	private AuthService authService;

	@Test
	public void signup_성공_시_토큰_반환() {
		//given
		SignupRequest signupRequest = new SignupRequest("user@test.com", "pw" ,"user", UserRole.USER.name());
		given(userRepository.existsByEmail(eq(signupRequest.getEmail()))).willReturn(false);

		String hashedPassword = "hashedPw";
		given(passwordEncoder.encode(eq(signupRequest.getPassword()))).willReturn(hashedPassword);

		User user = new User(signupRequest.getEmail(), signupRequest.getPassword(), signupRequest.getNickname(), UserRole.USER);
		ReflectionTestUtils.setField(user, "id", 1L);
		given(userRepository.save(any(User.class))).willReturn(user);

		String bearerToken = "Bearer XXXXX.XXXX.XXX";
		given(jwtUtil.createToken(eq(user.getId()), eq(user.getEmail()), eq(user.getNickname()), eq(UserRole.USER))).willReturn(
			bearerToken);

		//when
		SignupResponse signupResponse = authService.signup(signupRequest);

		//then
		assertThat(signupResponse).isNotNull();
		assertThat(signupResponse.getBearerToken()).isEqualTo(bearerToken);
	}

	@Test
	public void signup_중복_이메일_시_400_반환() {
		//given
		SignupRequest signupRequest = new SignupRequest("user@test.com", "pw" ,"user", UserRole.USER.name());
		given(userRepository.existsByEmail(eq(signupRequest.getEmail()))).willReturn(true);

		//when
		InvalidRequestException exception = assertThrows(InvalidRequestException.class,
			() -> authService.signup(signupRequest));

		//then
		assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
	}

	@Test
	public void signin_성공_시_토큰_반환() {
		//given
		SigninRequest signinRequest = new SigninRequest("user@test.com", "pw");
		User user = new User("user@test.com", "pw", "user", UserRole.USER);
		ReflectionTestUtils.setField(user, "id", 1L);

		given(userRepository.findByEmail(eq(signinRequest.getEmail()))).willReturn(Optional.of(user));

		given(passwordEncoder.matches(eq(signinRequest.getPassword()), eq(user.getPassword()))).willReturn(true);

		String bearerToken = "Bearer XXXXX.XXXX.XXX";
		given(jwtUtil.createToken(eq(user.getId()), eq(user.getEmail()), eq(user.getNickname()), eq(UserRole.USER))).willReturn(
			bearerToken);

		//when
		SigninResponse signinResponse = authService.signin(signinRequest);

		//then
		assertThat(signinResponse).isNotNull();
		assertThat(signinResponse.getBearerToken()).isEqualTo(bearerToken);
	}

	@Test
	public void signin_이메일_존재하지_않는_경우_400_반환() {
		//given
		SigninRequest signinRequest = new SigninRequest("user@test.com", "pw");
		User user = new User("user@test.com", "pw", "user", UserRole.USER);
		ReflectionTestUtils.setField(user, "id", 1L);

		given(userRepository.findByEmail(eq(signinRequest.getEmail()))).willReturn(Optional.empty());

		//when
		InvalidRequestException exception = assertThrows(InvalidRequestException.class,
			() -> authService.signin(signinRequest));

		//then
		assertEquals("가입되지 않은 유저입니다.", exception.getMessage());
	}

	@Test
	public void signin_비밀번호_일치하지_않는_경우_401_반환() {
		//given
		SigninRequest signinRequest = new SigninRequest("user@test.com", "pw");
		User user = new User("user@test.com", "pw", "user", UserRole.USER);
		ReflectionTestUtils.setField(user, "id", 1L);

		given(userRepository.findByEmail(eq(signinRequest.getEmail()))).willReturn(Optional.of(user));

		given(passwordEncoder.matches(eq(signinRequest.getPassword()), eq(user.getPassword()))).willReturn(false);

		//when
		AuthException exception = assertThrows(AuthException.class,
			() -> authService.signin(signinRequest));

		//then
		assertEquals("잘못된 비밀번호입니다.", exception.getMessage());

	}
}
