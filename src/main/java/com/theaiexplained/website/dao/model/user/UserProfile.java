package com.theaiexplained.website.dao.model.user;

import java.util.UUID;

import com.mattvorst.shared.dao.model.image.CropData;
import com.mattvorst.shared.model.DefaultAuditable;
import software.amazon.awssdk.enhanced.dynamodb.extensions.annotations.DynamoDbVersionAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class UserProfile extends DefaultAuditable {

	public static final String TABLE_NAME = "user_profile";
	private UUID userUuid;
	private UUID defaultHomeUuid;
	private String firstName;
	private String lastName;
	private UUID profileImageFileUuid;
	private String profileImageS3Bucket;
	private String profileImageS3Key;
	private CropData profileImageCropData;
	private String timeZone;
	private String defaultEmailAddress;
	private String defaultLocale;
	private Long version;

	@DynamoDbPartitionKey
	public UUID getUserUuid() {
		return userUuid;
	}

	public void setUserUuid(UUID userUuid) {
		this.userUuid = userUuid;
	}

	public UUID getDefaultHomeUuid() {
		return defaultHomeUuid;
	}

	public void setDefaultHomeUuid(UUID defaultHomeUuid) {
		this.defaultHomeUuid = defaultHomeUuid;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public UUID getProfileImageFileUuid() {
		return profileImageFileUuid;
	}

	public void setProfileImageFileUuid(UUID profileImageFileUuid) {
		this.profileImageFileUuid = profileImageFileUuid;
	}

	public String getProfileImageS3Bucket() {
		return profileImageS3Bucket;
	}

	public void setProfileImageS3Bucket(String profileImageS3Bucket) {
		this.profileImageS3Bucket = profileImageS3Bucket;
	}

	public String getProfileImageS3Key() {
		return profileImageS3Key;
	}

	public void setProfileImageS3Key(String profileImageS3Key) {
		this.profileImageS3Key = profileImageS3Key;
	}

	public CropData getProfileImageCropData() {
		return profileImageCropData;
	}

	public void setProfileImageCropData(CropData profileImageCropData) {
		this.profileImageCropData = profileImageCropData;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public String getDefaultEmailAddress() {
		return defaultEmailAddress;
	}

	public void setDefaultEmailAddress(String defaultEmailAddress) {
		this.defaultEmailAddress = defaultEmailAddress;
	}

	public String getDefaultLocale() {
		return defaultLocale;
	}

	public void setDefaultLocale(String defaultLocale) {
		this.defaultLocale = defaultLocale;
	}

	@DynamoDbVersionAttribute
	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}
}
