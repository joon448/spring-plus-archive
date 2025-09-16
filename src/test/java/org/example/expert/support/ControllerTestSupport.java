package org.example.expert.support;

import org.example.expert.config.JwtUtil;
import org.springframework.boot.test.mock.mockito.MockBean;

public abstract class ControllerTestSupport {

	@MockBean
	protected JwtUtil jwtUtil;
}
