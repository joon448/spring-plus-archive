package org.example.expert.support;

import java.util.Collection;
import java.util.Collections;

import org.example.expert.config.JwtAuthenticationToken;
import org.example.expert.domain.common.dto.AuthUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class TestSecurityContextFactory implements WithSecurityContextFactory<WithMockAuthUser> {

	@Override
	public SecurityContext createSecurityContext(WithMockAuthUser customUser) {
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		AuthUser authUser = new AuthUser(customUser.userId(), customUser.email(), customUser.nickname(), customUser.userRole());
		Collection<? extends GrantedAuthority> authorities =
			Collections.singletonList(new SimpleGrantedAuthority(customUser.userRole().getUserRole()));
		JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(authUser, authorities);
		context.setAuthentication(jwtAuthenticationToken);
		return context;
	}
}
