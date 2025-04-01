package com.mattvorst.shared.service;

import java.net.URI;
import java.time.Duration;

import com.mattvorst.shared.constant.EnvironmentConstants;
import com.mattvorst.shared.util.Environment;
import com.mattvorst.shared.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

public class AmazonServiceFactory {
	private static final Logger log = LoggerFactory.getLogger(AmazonServiceFactory.class);

	static SdkAsyncHttpClient httpS3Client = NettyNioAsyncHttpClient.builder()
			.readTimeout(Duration.ofSeconds(90))
			.build();

	public static AwsCredentialsProvider getAWSCredentialsProvider(String credentialProfile){
		return !Utils.empty(credentialProfile) ? ProfileCredentialsProvider.builder().profileName(credentialProfile).build() : DefaultCredentialsProvider.create();
	}

	public static SqsAsyncClient getSqsAsyncClient(String profile){

		String serviceEndpoint = Environment.get(EnvironmentConstants.AWS_SQS_SERVICE_ENDPOINT);
		String region = Environment.get(EnvironmentConstants.AWS_SQS_REGION);

		SqsAsyncClient sqsAsyncClient = SqsAsyncClient.builder()
				.region(Region.of(region))
				.endpointOverride(Utils.empty(serviceEndpoint) ? null : URI.create(serviceEndpoint))
				.credentialsProvider(getAWSCredentialsProvider(profile))
				.build();

		return sqsAsyncClient;
	}

	public static DynamoDbAsyncClient getDynamoDbAsyncClient(String region, String profile){
		DynamoDbAsyncClient dynamoDbAsyncClient = DynamoDbAsyncClient.builder()
				.region(Region.of(region))
				.credentialsProvider(getAWSCredentialsProvider(profile))
				.build();

		return dynamoDbAsyncClient;
	}


	public static S3AsyncClient getS3AsyncClient(String region, String credentialProfile){
		S3AsyncClient s3AsyncClient = S3AsyncClient.builder()
				.region(Region.of(region))
				.credentialsProvider(getAWSCredentialsProvider(credentialProfile))
				.httpClient(httpS3Client)
				.build();

		return s3AsyncClient;
	}
}
