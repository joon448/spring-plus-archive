package org.example.expert.domain.user.repository;

import java.util.List;

import org.example.expert.domain.user.dto.response.UserNicknameResponse;

public interface UserRepositoryCustom {
	List<UserNicknameResponse> searchByNickname(String nickname);
}
