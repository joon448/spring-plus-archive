package org.example.expert.domain.user.dto.response;

import lombok.Getter;

@Getter
public class UserNicknameResponse {

	private final Long id;
	private final String nickname;

	public UserNicknameResponse(Long id, String nickname) {
		this.id = id;
		this.nickname = nickname;
	}
}
