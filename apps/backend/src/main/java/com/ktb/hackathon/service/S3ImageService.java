package com.ktb.hackathon.service;

import com.ktb.hackathon.config.S3Properties;
import com.ktb.hackathon.exception.ImageStorageException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
public class S3ImageService {

	private static final Logger log = LoggerFactory.getLogger(S3ImageService.class);

	private static final String JPEG = "image/jpeg";
	private static final String PNG = "image/png";
	private static final String WEBP = "image/webp";

	private final S3Client s3Client;
	private final S3Presigner s3Presigner;
	private final S3Properties properties;

	public S3ImageService(
		S3Client s3Client,
		S3Presigner s3Presigner,
		S3Properties properties
	) {
		this.s3Client = s3Client;
		this.s3Presigner = s3Presigner;
		this.properties = properties;
	}

	public String uploadProfileImage(Long parentAccountId, Long childProfileId, MultipartFile file) {
		validate(file);

		String contentType = file.getContentType();
		String extension = extensionOf(contentType);
		String objectKey = buildObjectKey(parentAccountId, childProfileId, extension);

		PutObjectRequest request = PutObjectRequest.builder()
			.bucket(properties.bucket())
			.key(objectKey)
			.contentType(contentType)
			.contentLength(file.getSize())
			.build();

		try (InputStream inputStream = file.getInputStream()) {
			s3Client.putObject(request, RequestBody.fromInputStream(inputStream, file.getSize()));
			return objectKey;
		} catch (IOException | S3Exception | SdkClientException exception) {
			throw new ImageStorageException(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"IMAGE_UPLOAD_FAILED",
				"프로필 이미지 업로드에 실패했습니다."
			);
		}
	}

	public String createReadUrl(String objectKey) {
		if (!StringUtils.hasText(objectKey)) {
			return null;
		}

		try {
			GetObjectRequest getObjectRequest = GetObjectRequest.builder()
				.bucket(properties.bucket())
				.key(objectKey)
				.build();

			GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
				.signatureDuration(properties.presignedUrlExpiration())
				.getObjectRequest(getObjectRequest)
				.build();

			return s3Presigner.presignGetObject(presignRequest).url().toString();
		} catch (SdkClientException exception) {
			throw new ImageStorageException(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"IMAGE_URL_CREATION_FAILED",
				"프로필 이미지 URL 생성에 실패했습니다."
			);
		}
	}

	public void deleteQuietly(String objectKey) {
		if (!StringUtils.hasText(objectKey)) {
			return;
		}

		try {
			s3Client.deleteObject(DeleteObjectRequest.builder()
				.bucket(properties.bucket())
				.key(objectKey)
				.build());
		} catch (S3Exception | SdkClientException exception) {
			log.warn("기존 프로필 이미지 삭제에 실패했습니다. objectKey={}", objectKey, exception);
		}
	}

	private void validate(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new ImageStorageException(
				HttpStatus.BAD_REQUEST,
				"EMPTY_IMAGE_FILE",
				"이미지 파일은 필수입니다."
			);
		}

		if (file.getSize() > properties.maxFileSize().toBytes()) {
			throw new ImageStorageException(
				HttpStatus.PAYLOAD_TOO_LARGE,
				"IMAGE_FILE_TOO_LARGE",
				"이미지 파일은 5MB 이하만 업로드할 수 있습니다."
			);
		}

		extensionOf(file.getContentType());
	}

	private String extensionOf(String contentType) {
		if (!StringUtils.hasText(contentType)) {
			throw new ImageStorageException(
				HttpStatus.BAD_REQUEST,
				"UNSUPPORTED_IMAGE_TYPE",
				"JPEG, PNG, WebP 이미지만 업로드할 수 있습니다."
			);
		}

		return switch (contentType) {
			case JPEG -> "jpg";
			case PNG -> "png";
			case WEBP -> "webp";
			default -> throw new ImageStorageException(
				HttpStatus.BAD_REQUEST,
				"UNSUPPORTED_IMAGE_TYPE",
				"JPEG, PNG, WebP 이미지만 업로드할 수 있습니다."
			);
		};
	}

	private String buildObjectKey(Long parentAccountId, Long childProfileId, String extension) {
		String prefix = properties.profileImagePrefix();
		return "%s/%d/%d/%s.%s".formatted(
			prefix,
			parentAccountId,
			childProfileId,
			UUID.randomUUID(),
			extension
		);
	}
}
