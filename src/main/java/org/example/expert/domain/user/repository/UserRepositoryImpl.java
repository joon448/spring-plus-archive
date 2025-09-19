package org.example.expert.domain.user.repository;

import static org.example.expert.domain.user.entity.QUser.*;

import java.util.List;

import org.example.expert.domain.user.dto.response.UserNicknameResponse;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {
	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public List<UserNicknameResponse> searchByNickname(String nickname) {
		return jpaQueryFactory.select(Projections.constructor(
				UserNicknameResponse.class,
				user.id,
				user.nickname
			))
			.from(user)
			.where(user.nickname.lower().eq(nickname.toLowerCase()))
			.fetch();
	}
}
