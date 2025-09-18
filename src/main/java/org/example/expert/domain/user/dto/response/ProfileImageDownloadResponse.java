package org.example.expert.domain.user.dto.response;

import lombok.Getter;

@Getter
public class ProfileImageDownloadResponse {

	private final String url;
	private final Long expiresIn;

	public ProfileImageDownloadResponse(String url, Long expiresIn) {
		this.url = url;
		this.expiresIn = expiresIn;
	}
}
