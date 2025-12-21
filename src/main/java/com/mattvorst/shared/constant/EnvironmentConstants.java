package com.mattvorst.shared.constant;

/*
 * Copyright © 2025 Matthew A Vorst.
 * All Rights Reserved.
 * Permission to use, copy, modify, and distribute this software is strictly prohibited without the explicit written consent of Matthew A Vorst.
 *
 */

public class EnvironmentConstants
{
	public static final String ENV_VORST = "VORST";
	public static final String ENV_PROD = "US-WEST-1";


	/* AWS EC2 Instance Properties - All dynamically pulled from instance, should not be in environment.ml */
	public static final String AWS_INSTANCE_ID = "aws_instance_id";
	public static final String AWS_INSTANCE_TYPE = "aws_instance_type";
	public static final String AWS_LOCAL_IPV4 = "aws_local_ipv4";
	public static final String AWS_AMI_ID = "aws_ami_id";
	public static final String AWS_AVAILABILITY_ZONE = "aws_availability_zone";
	public static final String AWS_REGION = "aws_region";

	public static final String LOG_REQUESTS_LOCALLY = "LOG_REQUESTS_LOCALLY";

	public static final String TWILIO_PHONE_NUMBER = "TWILIO_PHONE_NUMBER";
	public static final String TWILIO_API_SID = "TWILIO_API_SID";
	public static final String TWILIO_API_AUTH_TOKEN = "TWILIO_API_AUTH_TOKEN";

	/* End dynamic EC2 instance properties

	 */
	public static final String BASE_URL = "BASE_URL";
	public static final String BUILD_NUMBER = "BUILD_NUMBER";
	public static final String CDN_URL = "CDN_URL";
	public static final String PUBLIC_FILES_CDN_URL = "PUBLIC_FILES_CDN_URL";
	public static final String EXTERNAL_JS_URL = "EXTERNAL_JS_URL";

	/* AWS Service Properties */
	public static final String AWS_SES_REGION = "AWS_SES_REGION";
	public static final String AWS_DEFAULT_PROFILE = "AWS_DEFAULT_PROFILE";
	public static final String AWS_S3_REGION = "AWS_S3_REGION";

	public static final String AWS_DYNAMO_DB_REGION = "AWS_DYNAMO_DB_REGION";
	public static final String AWS_SQS_REST_REQUEST_QUEUE = "AWS_SQS_REST_REQUEST_QUEUE";
	public static final String AWS_SQS_REST_REQUEST_TRANSFORM_QUEUE = "AWS_SQS_REST_REQUEST_TRANSFORM_QUEUE";
	public static final String AWS_SQS_JOB_QUEUE = "AWS_SQS_JOB_QUEUE";
	public static final String AWS_SQS_LOW_PRIORITY_JOB_QUEUE = "AWS_SQS_LOW_PRIORITY_JOB_QUEUE";
	public static final String AWS_SQS_LOG_QUEUE = "AWS_SQS_LOG_QUEUE";

	public static final String AWS_SQS_SERVICE_ENDPOINT = "AWS_SQS_SERVICE_ENDPOINT";
	public static final String AWS_SQS_REGION = "AWS_SQS_REGION";
	public static final String AWS_S3_BUCKET_FILE_DATA = "AWS_S3_BUCKET_FILE_DATA";
	public static final String AWS_S3_BUCKET_BUILD = "AWS_S3_BUCKET_BUILD";
	public static final String AWS_S3_BUCKET_PUBLIC_FILE_DATA = "AWS_S3_BUCKET_PUBLIC_FILE_DATA";
	public static final String AWS_S3_BUCKET_TEMP_FILE_DATA = "AWS_S3_BUCKET_TEMP_FILE_DATA";
	public static final String AWS_S3_URL_SIGNER_PROFILE = "AWS_S3_URL_SIGNER_PROFILE";
	public static final String AWS_S3_BUCKET_CDN = "AWS_S3_BUCKET_CDN";

	/* GOOGLE AUTH */
	public static final String GOOGLE_OAUTH_CLIENT_ID_ADMIN = "GOOGLE_OAUTH_CLIENT_ID_ADMIN";
	public static final String GOOGLE_OAUTH_CLIENT_KEY_ADMIN = "GOOGLE_OAUTH_CLIENT_KEY_ADMIN";
	public static final String GOOGLE_OAUTH_REDIRECT_URL_ADMIN = "GOOGLE_OAUTH_REDIRECT_URL_ADMIN";

	public static final String GOOGLE_OAUTH_CLIENT_ID_USER = "GOOGLE_OAUTH_CLIENT_ID_USER";
	public static final String GOOGLE_OAUTH_REDIRECT_URL_USER = "GOOGLE_OAUTH_REDIRECT_URL_USER";

	/* Deployment Properties */

	public static final String CURSOR_PASSWORD = "CURSOR_PASSWORD";
	public static final String CURSOR_IV = "CURSOR_IV";
	public static final String TOKEN_KEY_256_LIST = "TOKEN_KEY_256_LIST";
	public static final String TOKEN_IV_256_LIST = "TOKEN_IV_256_LIST";
	public static final String JWT_PUBLIC_KEY = "JWT_PUBLIC_KEY";
	public static final String JWT_PRIVATE_KEY = "JWT_PRIVATE_KEY";
	public static final String JWT_KEY_ID = "JWT_KEY_ID";
	public static final String JWT_ISSUER = "JWT_ISSUER";

	public static final String ADMINISTRATOR_EMAIL = "ADMINISTRATOR_EMAIL";
	public static final String ADMIN_USER_UUID = "ADMIN_USER_UUID";
}
