package org.example.expert.config;

import java.util.Collection;

import org.example.expert.domain.common.dto.AuthUser;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
	private final AuthUser authUser;

	public JwtAuthenticationToken(AuthUser authUser, Collection<? extends GrantedAuthority> authorities) {
		super(authorities);
		this.authUser = authUser;
		setAuthenticated(true);
	}

	@Override
	public Object getCredentials() {
		return null;
	}

	@Override
	public Object getPrincipal() {
		return authUser;
	}
}
