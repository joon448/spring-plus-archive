package org.example.expert.domain.user.controller;

import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.dto.response.ProfileImageDownloadResponse;
import org.example.expert.domain.user.dto.response.ProfileImageUploadResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.ProfileImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class ProfileImageController {
	private final ProfileImageService profileImageService;

	@PostMapping(value = "/{userId}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ProfileImageUploadResponse> upload(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable long userId,
		@RequestPart("file") MultipartFile file
	){
		if (!(authUser.getUserRole().equals(UserRole.ADMIN) || authUser.getId().equals(userId)))
			throw new AuthException("권한이 없습니다.");
		return ResponseEntity.ok(profileImageService.upload(userId, file));
	}

	@GetMapping("/{userId}/profile-image")
	public ResponseEntity<ProfileImageDownloadResponse> getProfileImageUrl(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable long userId,
		@RequestParam(defaultValue = "600") long ttl
	){
		if (!(authUser.getUserRole().equals(UserRole.ADMIN) || authUser.getId().equals(userId)))
			throw new AuthException("권한이 없습니다.");
		return ResponseEntity.ok(profileImageService.getDownloadUrl(userId, ttl));
	}

	@DeleteMapping("{userId}/profile-image")
	public void deleteProfileImage(
		@AuthenticationPrincipal AuthUser authUser,
		@PathVariable long userId
	){
		if (!(authUser.getUserRole().equals(UserRole.ADMIN) || authUser.getId().equals(userId)))
			throw new AuthException("권한이 없습니다.");
		profileImageService.delete(userId);
	}
}
