package com.mattvorst.shared.model.file;

public class S3UploadUrl {
	private String url;
	private String s3Bucket;
	private String s3Key;

	public S3UploadUrl() {
		super();
	}

	public S3UploadUrl(String url, String s3Bucket, String s3Key) {
		this();
		this.url = url;
		this.s3Bucket = s3Bucket;
		this.s3Key = s3Key;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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
}
