package org.example.expert.support;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@Import(SecurityConfig.class)
public abstract class ControllerTestSupport {

	@MockBean
	protected JwtUtil jwtUtil;

	@Autowired
	protected ObjectMapper objectMapper;

	@Autowired
	protected MockMvc mockMvc;
}
