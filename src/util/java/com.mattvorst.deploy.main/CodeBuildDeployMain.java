package com.mattvorst.deploy.main;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.mattvorst.shared.constant.EnvironmentConstants;
import com.mattvorst.shared.constant.PropertyType;
import com.mattvorst.shared.dao.model.system.SystemProperty;
import com.mattvorst.shared.service.AmazonServiceFactory;
import com.mattvorst.shared.util.Environment;
import com.mattvorst.shared.util.Utils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

public class CodeBuildDeployMain {

	static ExecutorService executorService = new ThreadPoolExecutor(10, 20, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

	S3AsyncClient amazonS3Destination;
	DynamoDbAsyncClient dynamoDbAsyncClient;
	DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

	public static void main(String[] args) {

		Environment.instance(EnvironmentConstants.ENV_VORST);

		CodeBuildDeployMain main = new CodeBuildDeployMain();

		main.run();

		System.exit(0);
	}

	private void run(){
		String destRegion = Environment.get(EnvironmentConstants.AWS_REGION); //"us-west-1";
		String destBucket = Environment.get(EnvironmentConstants.AWS_S3_BUCKET_CDN); //"us-west-1.ci.mattvorst.com";

		amazonS3Destination = AmazonServiceFactory.getS3AsyncClient(destRegion, "");
		dynamoDbAsyncClient = AmazonServiceFactory.getDynamoDbAsyncClient(destRegion, "");
		dynamoDbEnhancedAsyncClient = DynamoDbEnhancedAsyncClient.builder()
				.dynamoDbClient(dynamoDbAsyncClient)
				.build();

		int availableBuildNumber = 0;
		try {
			availableBuildNumber = getBuildNumber();
		} catch (Throwable t) {
			System.err.println("Unable to get build number from deploy");
			t.printStackTrace();
		}

		if (availableBuildNumber > 0) {
			try {
				deployToCDN(availableBuildNumber, destBucket);

				SystemProperty buildNumberProperty = new SystemProperty();
				buildNumberProperty.setPropertyType(PropertyType.CI_BUILD_NUMBER_AVAILABLE);
				buildNumberProperty.setValue(Long.toString(availableBuildNumber));

				updateSystemProperty(buildNumberProperty).join();
			} catch (Throwable t) {
				System.err.println("Dev / CI Build (" + availableBuildNumber + ") NOT Copied to " + destBucket);
				throw new RuntimeException(t);
			}

			try{
				int deployedBuildNumber = 0;
				SystemProperty systemProperty = getSystemProperty(PropertyType.CI_BUILD_NUMBER_DEPLOYED).join();
				if(!Utils.empty(systemProperty.getValue())){
					try{
						deployedBuildNumber = Integer.parseInt(systemProperty.getValue());
					}catch (Exception e){}
				}

				createUserSystemMessage(deployedBuildNumber, availableBuildNumber);
			}catch (Exception e){
				System.err.println("Dev / CI Build (" + availableBuildNumber + ") User Message NOT Saved!");
				e.printStackTrace();
			}
		} else {
			System.err.println("Deploy aborted - invalid build number");
		}
	}

	private CompletableFuture<Void> updateSystemProperty(SystemProperty systemProperty) {
		DynamoDbAsyncTable<SystemProperty> table = dynamoDbEnhancedAsyncClient.table(SystemProperty.TABLE_NAME, TableSchema.fromBean(SystemProperty.class));
		return table.putItem(systemProperty);
	}

	public CompletableFuture<SystemProperty> getSystemProperty(PropertyType propertyType) {
		DynamoDbAsyncTable<SystemProperty> table = dynamoDbEnhancedAsyncClient.table(SystemProperty.TABLE_NAME, TableSchema.fromBean(SystemProperty.class));
		return table.getItem(Key.builder().partitionValue(propertyType.toString()).build());
	}

//	public CompletableFuture<UserSystemMessage> getUserSystemMessage(UUID messageUuid) {
//		DynamoDbAsyncTable<UserSystemMessage> table = dynamoDbEnhancedAsyncClient.table(UserSystemMessage.TABLE_NAME, TableSchema.fromBean(UserSystemMessage.class));
//		return table.getItem(Key.builder().partitionValue(messageUuid.toString()).build());
//	}
//
//	public CompletableFuture<Void> saveUserSystemMessage(UserSystemMessage userSystemMessage) {
//		DynamoDbAsyncTable<UserSystemMessage> table = dynamoDbEnhancedAsyncClient.table(UserSystemMessage.TABLE_NAME, TableSchema.fromBean(UserSystemMessage.class));
//		return table.putItem(userSystemMessage);
//	}
//
//	private CompletableFuture<DynamoResultList<UserSystemMessage>> getUserSystemMessageListByUserUuid(UUID userUuid, int pageSize, Map<String, AttributeValue> exclusiveStartKey) {
//		final DynamoDbAsyncTable<UserSystemMessage> table = dynamoDbEnhancedAsyncClient.table(UserSystemMessage.TABLE_NAME, TableSchema.fromBean(UserSystemMessage.class));
//		final DynamoDbAsyncIndex<UserSystemMessage> dynamoDbAsyncIndex = table.index("userUuid-createdDateAndMessageUuid-index");
//
//		QueryEnhancedRequest request = QueryEnhancedRequest.builder()
//				.queryConditional(QueryConditional.keyEqualTo(Key.builder().partitionValue(userUuid.toString()).build()))
//				.limit(pageSize)
//				.scanIndexForward(false)
//				.exclusiveStartKey(exclusiveStartKey)
//				.build();
//
//		return DynamoDbUtils.queryWithPagination(dynamoDbAsyncIndex, request);
//	}
//
	private void createUserSystemMessage(int deployedBuildNumber, int availableBuildNumber){

		System.out.println("Send (Dev / CI) User Message");
//
//		UUID userUuid = Environment.getUuid(EnvironmentConstants.ADMIN_USER_UUID);
//
//		AtomicReference<UserSystemMessage> userSystemMessageAtomicReference = new AtomicReference<>();
//		DynamoResultList<UserSystemMessage> dynamoResultList = new DynamoResultList<>();
//		do {
//			dynamoResultList = getUserSystemMessageListByUserUuid(userUuid, 20, dynamoResultList.getLastEvaluatedKey()).join();
//			Streams.of(dynamoResultList.getList()).forEach(userSystemMessage -> {
//				if(SystemMessageType.SERVER_UPGRADE_AVAILABLE.equals(userSystemMessage.getSystemMessageType())){
//					userSystemMessageAtomicReference.set(userSystemMessage);
//				}
//			});
//		}while(!dynamoResultList.empty());
//
//		UserSystemMessage userSystemMessage = userSystemMessageAtomicReference.get();
//		if(userSystemMessage == null) {
//			System.out.println("- Message not found; Creating User Message");
//			userSystemMessage = new UserSystemMessage();
//			userSystemMessage.setUserUuid(userUuid);
//			userSystemMessage.setSystemMessageType(SystemMessageType.SERVER_UPGRADE_AVAILABLE);
//			int count = 0;
//			do{
//				UUID messageUuid = UUID.randomUUID();
//				if(getUserSystemMessage(messageUuid).join() == null){
//					userSystemMessage.setMessageUuid(messageUuid);
//				}
//			}while(userSystemMessage.getMessageUuid() == null && count++ < 10);
//		}else{
//			System.out.println("- Message found; Updating User Message");
//		}
//
//		userSystemMessage.setTitle("Server Upgrade Available (" + deployedBuildNumber + " -> " + availableBuildNumber + ")");
//		userSystemMessage.setMessage("Server version " + availableBuildNumber + " is available. Please upgrade the server from version " + deployedBuildNumber + ".");
//		userSystemMessage.setCreatedDate(new Date());
//		userSystemMessage.setUpdatedDate(new Date());
//
//		saveUserSystemMessage(userSystemMessage).join();
//
//		System.out.println("- User Message Saved");
	}

	private int getBuildNumber() {
		int buildNumber = 0;

		System.out.println("Getting build number from WAR file.");

		FileInputStream inputStream = null;

		try {
			inputStream = new FileInputStream("./src/main/resources/environment.xml");

			System.out.println(" - Parsing environment.xml.");

			SAXReader reader = new SAXReader();
			Document document = reader.read(inputStream);
			Element root = document.getRootElement();

			Iterator environmentItr = root.elementIterator("environment");
			while (environmentItr.hasNext()) {
				Element environmentElement = (Element) environmentItr.next();

				String environmentId = environmentElement.attributeValue("id");
				if (environmentId == null) {
					Iterator propertyItr = environmentElement.element("properties").elementIterator("property");
					while (propertyItr.hasNext()) {
						Element propertyElement = (Element) propertyItr.next();
						String key = propertyElement.attributeValue("id");
						if (EnvironmentConstants.BUILD_NUMBER.equalsIgnoreCase(key)) {
							String value = propertyElement.getTextTrim();
							if (value != null) {
								buildNumber = Integer.parseInt(value);
								System.out.println(" - Build number " + buildNumber + " found, closing file.");
							}
						}
					}
				}
			}

		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Throwable t) {}
			}
		}

		return buildNumber;
	}

	private void deployToCDN(long buildNumber, String bucketName) throws IOException {

		System.out.println("Deploying Assets to CDN (Dev / CI)");

		int filesCopied = 0;

		FileInputStream inputStream = null;
		ZipInputStream zipInputStream = null;

		try {
			inputStream = new FileInputStream("./build/distributions/MattVorst_Docs-CI-0.0.1.zip");
			zipInputStream = new ZipInputStream(inputStream);

			ZipEntry zipEntry = null;
			do {
				zipEntry = zipInputStream.getNextEntry();
				if (zipEntry != null) {
					if (!zipEntry.isDirectory()) {
						String filename = zipEntry.getName().replaceFirst("html/", "cdn/" + buildNumber + "/");

							ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
							final byte[] buffer = new byte[1024];
							int length;
							while ((length = zipInputStream.read(buffer, 0, buffer.length)) >= 0) {
								outputStream.write(buffer, 0, length);
							}

							outputStream.flush();

							ByteArrayInputStream tempInputStream = new ByteArrayInputStream(outputStream.toByteArray());

							outputStream.close();

						if(!filename.toLowerCase().endsWith(".htaccess")
								&& !filename.toLowerCase().endsWith(".jsx")
								&& !filename.toLowerCase().endsWith(".sh")
								&& !filename.toLowerCase().endsWith("eslint.config.js")
								&& !filename.toLowerCase().endsWith("package.json")
								&& !filename.toLowerCase().endsWith("package-lock.json")
								&& !filename.toLowerCase().endsWith("rollup.config.js")
								&& !filename.toLowerCase().endsWith("vite.config.js")
								&& !filename.toLowerCase().endsWith("yarn.lock")) {

							String mimeType = Files.probeContentType(new File(filename).toPath());

							PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(filename).contentType(mimeType).acl(ObjectCannedACL.PUBLIC_READ).build();
							CompletableFuture<PutObjectResponse> response = amazonS3Destination.putObject(putObjectRequest,
									AsyncRequestBody.fromInputStream(tempInputStream, (long) tempInputStream.available(), executorService));

							System.out.println(response.join().eTag());

							filesCopied++;
						}

						tempInputStream.close();
						zipInputStream.closeEntry();
					}
				}
			} while (zipEntry != null);

			System.out.println("Files copied to " + bucketName + ": " + filesCopied);

			if (zipInputStream != null) {
				zipInputStream.closeEntry();
				zipInputStream.close();
				zipInputStream = null;
			}
		} finally {
			if (zipInputStream != null) {
				try {
					zipInputStream.close();
				} catch (Throwable t) {}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Throwable t) {}
			}
		}
	}
}
