package org.example.expert.domain.auth.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.auth.service.AuthService;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.support.ControllerTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

@WebMvcTest(AuthController.class)
public class AuthControllerTest extends ControllerTestSupport {

	@MockBean
	private AuthService authService;

	@Test
	void signup에_성공한다() throws Exception {
		// given
		SignupRequest signupRequest = new SignupRequest("user@test.com", "pw", "user", UserRole.USER.name());

		String bearerToken = "Bearer XXXXX.XXXX.XXX";
		SignupResponse response = new SignupResponse(bearerToken);

		given(authService.signup(any(SignupRequest.class))).willReturn(response);

		// when & then
		mockMvc.perform(post("/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signupRequest))
				.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.bearerToken").value(bearerToken));
	}

	@Test
	void signup_중복이메일일_경우_예외가_발생한다() throws Exception {
		// given
		SignupRequest signupRequest = new SignupRequest("user@test.com", "pw", "user", UserRole.USER.name());

		given(authService.signup(any(SignupRequest.class)))
			.willThrow(new InvalidRequestException("이미 존재하는 이메일입니다."));

		// when & then
		mockMvc.perform(post("/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signupRequest))
				.with(csrf()))
			.andExpect(status().isBadRequest());
	}

	@Test
	void signin에_성공한다() throws Exception {
		// given
		SigninRequest signinRequest = new SigninRequest("user@test.com", "pw");

		String bearerToken = "Bearer XXXXX.XXXX.XXX";
		SigninResponse response = new SigninResponse(bearerToken);

		given(authService.signin(any(SigninRequest.class))).willReturn(response);

		// when & then
		mockMvc.perform(post("/auth/signin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signinRequest))
				.with(csrf()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.bearerToken").value(bearerToken));
	}

	@Test
	void signin_없는_이메일일_경우_예외가_발생한다() throws Exception {
		// given
		SigninRequest signinRequest = new SigninRequest("user@test.com", "pw");

		given(authService.signin(any(SigninRequest.class)))
			.willThrow(new InvalidRequestException("가입되지 않은 유저입니다."));

		// when & then
		mockMvc.perform(post("/auth/signin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signinRequest))
				.with(csrf()))
			.andExpect(status().isBadRequest());
	}

	@Test
	void signin_비밀번호가_일치하지_않는_경우_예외가_발생한다() throws Exception {
		// given
		SigninRequest signinRequest = new SigninRequest("user@test.com", "pw");

		given(authService.signin(any(SigninRequest.class)))
			.willThrow(new AuthException("잘못된 비밀번호입니다."));

		// when & then
		mockMvc.perform(post("/auth/signin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signinRequest))
				.with(csrf()))
			.andExpect(status().isUnauthorized());
	}
}
