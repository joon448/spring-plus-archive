package org.example.expert.domain.auth.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

public class AuthIntegrationTest extends IntegrationTestSupport {

	@Autowired
	ObjectMapper objectMapper;
	
	@Test
	void 회원가입_후_로그인에_성공하고_일정_목록을_조회한다() throws Exception {
		SignupRequest signupRequest = new SignupRequest("admin@example.com", "pw", "admin", UserRole.ADMIN.name());
		mockMvc.perform(post("/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signupRequest))
				.with(csrf()))
			.andExpect(status().isOk());

		SigninRequest signinRequest = new SigninRequest(signupRequest.getEmail(), signupRequest.getPassword());
		MvcResult mvcResult = mockMvc.perform(post("/auth/signin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signinRequest))
				.with(csrf()))
			.andExpect(status().isOk())
			.andReturn();

		String bearerToken = JsonPath.parse(mvcResult.getResponse().getContentAsString())
			.read("$.bearerToken", String.class);

		mockMvc.perform(get("/todos")
				.header("Authorization", bearerToken))
			.andExpect(status().isOk());
	}

	@Test
	void 회원가입_후_비밀번호가_일치하지_않는_경우_로그인에_실패한다() throws Exception {
		SignupRequest signupRequest = new SignupRequest("admin@example.com", "pw", "admin", UserRole.ADMIN.name());
		mockMvc.perform(post("/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signupRequest))
				.with(csrf()))
			.andExpect(status().isOk());

		String wrongPassword = "wrongPassword";
		SigninRequest signinRequest = new SigninRequest(signupRequest.getEmail(), wrongPassword);
		mockMvc.perform(post("/auth/signin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signinRequest))
				.with(csrf()))
			.andExpect(status().isUnauthorized())
			.andReturn();
	}
}
