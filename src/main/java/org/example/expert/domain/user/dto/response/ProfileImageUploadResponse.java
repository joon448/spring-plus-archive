package org.example.expert.domain.user.dto.response;

import lombok.Getter;

@Getter
public class ProfileImageUploadResponse {

	private final Long id;
	private final String key;

	public ProfileImageUploadResponse(Long id, String key) {
		this.id = id;
		this.key = key;
	}
}
