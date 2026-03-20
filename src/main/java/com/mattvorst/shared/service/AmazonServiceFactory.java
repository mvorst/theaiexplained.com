package com.mattvorst.shared.service;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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
import software.amazon.awssdk.utils.SdkAutoCloseable;

public class AmazonServiceFactory {
	private static final Logger log = LoggerFactory.getLogger(AmazonServiceFactory.class);

	// Shared Netty HTTP client for all AWS services
	private static SdkAsyncHttpClient sharedHttpClient = NettyNioAsyncHttpClient.builder()
			.readTimeout(Duration.ofSeconds(90))
			.build();

	// Track all created clients for shutdown
	private static final List<SdkAutoCloseable> clients = new ArrayList<>();

	public static void shutdown() {
		log.info("Shutting down AWS SDK clients");

		// Close all tracked clients
		for (SdkAutoCloseable client : clients) {
			try {
				client.close();
			} catch (Exception e) {
				log.warn("Error closing AWS client: {}", e.getMessage());
			}
		}
		clients.clear();

		// Close the shared HTTP client
		if (sharedHttpClient != null) {
			log.info("Closing shared Netty HTTP client");
			sharedHttpClient.close();
			sharedHttpClient = null;
		}
	}

	public static AwsCredentialsProvider getAWSCredentialsProvider(String profile){
		return !Utils.empty(profile) ? ProfileCredentialsProvider.builder().profileName(profile).build() : DefaultCredentialsProvider.create();
	}

	public static SqsAsyncClient getSqsAsyncClient(String profile){

		String serviceEndpoint = Environment.get(EnvironmentConstants.AWS_SQS_SERVICE_ENDPOINT);
		String region = Environment.get(EnvironmentConstants.AWS_SQS_REGION);

		SqsAsyncClient sqsAsyncClient = SqsAsyncClient.builder()
				.region(Region.of(region))
				.endpointOverride(Utils.empty(serviceEndpoint) ? null : URI.create(serviceEndpoint))
				.credentialsProvider(getAWSCredentialsProvider(profile))
				.httpClient(sharedHttpClient)
				.build();

		clients.add(sqsAsyncClient);
		return sqsAsyncClient;
	}

	public static DynamoDbAsyncClient getDynamoDbAsyncClient(String region, String profile){
		DynamoDbAsyncClient dynamoDbAsyncClient = DynamoDbAsyncClient.builder()
				.region(Region.of(region))
				.credentialsProvider(getAWSCredentialsProvider(profile))
				.httpClient(sharedHttpClient)
				.build();

		clients.add(dynamoDbAsyncClient);
		return dynamoDbAsyncClient;
	}


	public static S3AsyncClient getS3AsyncClient(String region, String profile){
		S3AsyncClient s3AsyncClient = S3AsyncClient.builder()
				.region(Region.of(region))
				.credentialsProvider(getAWSCredentialsProvider(profile))
				.httpClient(sharedHttpClient)
				.build();

		clients.add(s3AsyncClient);
		return s3AsyncClient;
	}
}
