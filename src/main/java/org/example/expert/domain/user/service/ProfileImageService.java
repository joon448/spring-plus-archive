package org.example.expert.domain.user.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.response.ProfileImageDownloadResponse;
import org.example.expert.domain.user.dto.response.ProfileImageUploadResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileImageService {
	private final S3Client s3Client;
	private final S3Presigner s3Presigner;
	private final UserRepository userRepository;

	@Value("${app.s3.bucket}") String bucket;

	private static final long MAX = 5 * 1024 * 1024;
	private static final Set<String> ALLOWED_TYPE = Set.of("image/png", "image/jpeg", "image/jpg");

	public ProfileImageUploadResponse upload(long userId, MultipartFile file) {
		validate(file);
		String key = buildKey(userId, file.getOriginalFilename());

		try(InputStream in = file.getInputStream()){
			s3Client.putObject(
				PutObjectRequest.builder()
				.bucket(bucket)
				.key(key)
				.contentType(file.getContentType())
				.build(),
				RequestBody.fromInputStream(in, file.getSize()));
		} catch (IOException e){
			throw new InvalidRequestException("Failed to upload profile image");
		}

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new InvalidRequestException("User not found"));

		user.updateProfileImageKey(key);
		return new ProfileImageUploadResponse(userId, key);
	}

	public void delete(long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new InvalidRequestException("User not found"));
		String key = user.getProfileImageKey();
		if(key != null){
			s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
			user.removeProfileImage();
		}
	}

	public ProfileImageDownloadResponse getDownloadUrl(long userId, long ttl){
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new InvalidRequestException("User not found"));
		String key = user.getProfileImageKey();
		if (key == null) {
			throw new InvalidRequestException("프로필 이미지가 없습니다.");
		}

		GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();
		GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
			.signatureDuration(Duration.ofSeconds(ttl))
			.getObjectRequest(getObjectRequest).build();
		return new ProfileImageDownloadResponse(
			s3Presigner.presignGetObject(getObjectPresignRequest).url().toString(), ttl);
	}

	private void validate(MultipartFile file) {
		if(file.isEmpty()){
			throw new InvalidRequestException("File is empty");
		}
		if(!ALLOWED_TYPE.contains(file.getContentType())){
			throw new InvalidRequestException("File type not supported (Supported: jpg, jpeg, png)");
		}
		if(file.getSize() > MAX){
			throw new InvalidRequestException("File is too large (Max: 5MB)");
		}
	}

	private String buildKey(long userId, String name) {
		String ext = (name!=null && name.contains(".")) ? name.substring(name.lastIndexOf('.')+1) : "png";
		return "profiles/%d/%s.%s".formatted(userId, UUID.randomUUID(), ext.toLowerCase());
	}
}
