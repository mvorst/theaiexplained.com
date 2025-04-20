package com.mattvorst.shared.model.file;

import java.util.UUID;

import com.mattvorst.shared.constant.AssetType;

public class S3UploadComplete {
	private String s3Bucket;
	private String s3Key;
	private String downloadUrl;
	private String contentType;
	private String name;
	private AssetType assetType;
	private long size;
	private UUID fileUuid;

	public S3UploadComplete() {
		super();
	}

	public String getS3Key() {
		return s3Key;
	}

	public void setS3Key(String s3Key) {
		this.s3Key = s3Key;
	}

	public String getS3Bucket() {
		return s3Bucket;
	}

	public void setS3Bucket(String s3Bucket) {
		this.s3Bucket = s3Bucket;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AssetType getAssetType() {
		return assetType;
	}

	public void setAssetType(AssetType assetType) {
		this.assetType = assetType;
	}

	public UUID getFileUuid() {
		return fileUuid;
	}

	public void setFileUuid(UUID fileUuid) {
		this.fileUuid = fileUuid;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
}
