package org.example.expert.support;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.example.expert.domain.user.enums.UserRole;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = TestSecurityContextFactory.class)
public @interface WithMockAuthUser {
	long userId();
	String email();
	String nickname();
	UserRole userRole();
}
