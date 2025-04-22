package com.mattvorst.shared.service;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import com.mattvorst.shared.async.processor.TaskProcessor;
import com.mattvorst.shared.constant.AssetType;
import com.mattvorst.shared.constant.EnvironmentConstants;
import com.mattvorst.shared.dao.FileDao;
import com.mattvorst.shared.dao.model.file.File;
import com.mattvorst.shared.dao.model.file.FileReference;
import com.mattvorst.shared.dao.model.image.CropData;
import com.mattvorst.shared.model.file.S3UploadComplete;
import com.mattvorst.shared.model.file.S3UploadUrl;
import com.mattvorst.shared.task.image.CreateImageCroppedTask;
import com.mattvorst.shared.task.image.CreateImageCroppedTask.Parameters;
import com.mattvorst.shared.util.Environment;
import com.mattvorst.shared.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
public class FileService {

	private static final Logger log = LoggerFactory.getLogger(FileService.class);

	@Autowired private FileDao fileDao;

	@Autowired private TaskProcessor taskProcessor;

	private S3AsyncClient s3AsyncPresignClient;

	public FileService() {
		super();

		s3AsyncPresignClient = AmazonServiceFactory.getS3AsyncClient(Environment.get(EnvironmentConstants.AWS_S3_REGION), Environment.get(EnvironmentConstants.AWS_DEFAULT_PROFILE));
	}



	public S3UploadUrl getPreSignedUrl(String s3Bucket, String s3Key) {
		S3Presigner presigner = S3Presigner.create();

		PutObjectRequest objectRequest = PutObjectRequest.builder()
				.bucket(s3Bucket)
				.key(s3Key)
				.build();

		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
				.putObjectRequest(objectRequest)
				.signatureDuration(Duration.ofMinutes(10))
				.build();

		String presignedUrl = presigner.presignPutObject(presignRequest).url().toString();

		return new S3UploadUrl(presignedUrl, s3Bucket, s3Key);
	}

	public S3UploadComplete uploadImageComplete(UUID userUuid, S3UploadComplete s3UploadComplete) {
		// Copy the s3 object from the temp bucket to the final bucket

		String contentType = s3UploadComplete.getContentType();

		File file = new File();
		UUID fileUuid = Utils.safeToUuid(s3UploadComplete.getS3Key());
		if(fileUuid == null){
			fileUuid = UUID.randomUUID();
		}

		AtomicReference<String> destinationKey = new AtomicReference<>(fileUuid.toString());
		do{
			if(fileDao.getFile(fileUuid).join() == null){
				destinationKey.set(fileUuid.toString());

				file.setFileUuid(fileUuid);
				file.setName(s3UploadComplete.getName());
				file.setContentType(contentType);
				file.setS3Bucket(Environment.get(EnvironmentConstants.AWS_S3_BUCKET_FILE_DATA));
				file.setS3Key(destinationKey.get());
				file.setAssetType(s3UploadComplete.getAssetType());
				file.setSize(s3UploadComplete.getSize());

				s3UploadComplete.setFileUuid(file.getFileUuid());
			}else{
				fileUuid = UUID.randomUUID();
			}
		}while(file.getFileUuid() == null);

		CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
				.sourceBucket(s3UploadComplete.getS3Bucket())
				.sourceKey(s3UploadComplete.getS3Key())
				.destinationBucket(Environment.get(EnvironmentConstants.AWS_S3_BUCKET_FILE_DATA))
				.destinationKey(destinationKey.toString())
				.contentType(contentType)
				.build();

		CopyObjectResponse copyObjectResponse = s3AsyncPresignClient.copyObject(copyObjectRequest).join();
		if(copyObjectResponse != null){
			file.setETag(copyObjectResponse.copyObjectResult().eTag().replaceAll("\"", ""));
		}

		fileDao.saveFile(file).join();

		FileReference fileReference = new FileReference();
		BeanUtils.copyProperties(file, fileReference);
		fileReference.setReferenceUuid(UUID.randomUUID());
		fileReference.setReferenceKey("USER/" + userUuid + "/TYPE/" + file.getAssetType());

		fileDao.saveFileReference(fileReference).join();

		s3AsyncPresignClient.deleteObject(DeleteObjectRequest.builder().bucket(s3UploadComplete.getS3Bucket()).key(s3UploadComplete.getS3Key()).build());

		s3UploadComplete.setS3Bucket(Environment.get(EnvironmentConstants.AWS_S3_BUCKET_FILE_DATA));
		s3UploadComplete.setS3Key(destinationKey.toString());

//		if(AssetType.MARKETING_BANNER_IMAGE.equals(s3UploadComplete.getAssetType())){
//			taskProcessor.processLocally(new Parameters(s3UploadComplete.getS3Bucket(), s3UploadComplete.getS3Key(), s3UploadComplete.getS3Bucket(), s3UploadComplete.getS3Key(), new CropData(0, 0, 1200, 630, 10000), s3UploadComplete.getContentType(), null));
//		}

		return s3UploadComplete;
	}

	public S3UploadUrl getSignedUrl(UUID fileUuid) {
		File file = fileDao.getFile(fileUuid).join();
		if(file == null){
			return null;
		}

		S3Presigner presigner = S3Presigner.create();

		GetObjectRequest objectRequest = GetObjectRequest.builder()
				.bucket(file.getS3Bucket())
				.key(file.getS3Key())
				.build();

		GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
				.getObjectRequest(objectRequest)
				.signatureDuration(Duration.ofMinutes(10))
				.build();

		String presignedUrl = presigner.presignGetObject(presignRequest).url().toString();

		return new S3UploadUrl(presignedUrl, file.getS3Bucket(), file.getS3Key());
	}

	public S3UploadUrl getSignedUrl(String s3Bucket, String s3Key) {

		S3Presigner presigner = S3Presigner.create();

		GetObjectRequest objectRequest = GetObjectRequest.builder()
				.bucket(s3Bucket)
				.key(s3Key)
				.build();

		GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
				.getObjectRequest(objectRequest)
				.signatureDuration(Duration.ofMinutes(10))
				.build();

		String presignedUrl = presigner.presignGetObject(presignRequest).url().toString();

		return new S3UploadUrl(presignedUrl, s3Bucket, s3Key);
	}
}
