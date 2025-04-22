package com.mattvorst.shared.task.image;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mattvorst.shared.async.model.AbstractTaskParameters;
import com.mattvorst.shared.async.model.AsyncTask;
import com.mattvorst.shared.async.model.QueueRunnable;
import com.mattvorst.shared.async.processor.TaskProcessor;
import com.mattvorst.shared.constant.EnvironmentConstants;
import com.mattvorst.shared.dao.model.image.CropData;
import com.mattvorst.shared.util.ContentTypeUtils;
import com.mattvorst.shared.util.Environment;
import com.mattvorst.shared.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@AsyncTask("CreateImageCroppedTask")
public class CreateImageCroppedTask extends QueueRunnable {

	private static final Logger log = LogManager.getLogger(CreateImageCroppedTask.class);

	private S3Client s3Client;
	private final TaskProcessor taskProcessor;
	private final Parameters parameters;

	public CreateImageCroppedTask() {
		super();

		this.taskProcessor = null;
		this.parameters = null;
	}

	public CreateImageCroppedTask(TaskProcessor taskProcessor, Parameters parameters) {
		super();

		this.taskProcessor = taskProcessor;
		this.parameters = parameters;

		this.s3Client = S3Client.builder()
				.region(Region.of(Environment.get(EnvironmentConstants.AWS_S3_REGION)))
				.build();
	}

	@Override
	public void run() {
		try {

			applyCropAndSave(parameters);

			if(parameters.getNextJobParameters() != null){
				taskProcessor.processLocally(parameters.getNextJobParameters());
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void applyCropAndSave(Parameters parameters) throws IOException {
		try (ResponseInputStream inputStream = this.s3Client.getObject(builder ->
				builder.bucket(parameters.getSourceS3Bucket())
						.key(parameters.getSourceS3Key()))) {

			if (inputStream != null) {
				BufferedImage sourceImage = ImageIO.read(inputStream);

				BufferedImage bufferedImage = applyCrop(sourceImage, parameters.getCropData(), parameters.getContentType());
				if (bufferedImage != null) {
					saveBufferedImageToS3(bufferedImage, parameters.getDestinationS3Bucket(), parameters.getDestinationS3Key());
				}
			} else {
				log.error("Source or mask S3 object is null");
			}
		}
	}

	public void applyCropAndSave(File inputImageFile, CropData cropData, String contentType, String outputPath) throws IOException {
		BufferedImage originalImage = ImageIO.read(inputImageFile);

		BufferedImage bufferedImage = applyCrop(originalImage, cropData, contentType);

		// Save the result as a PNG
		File outputFile = new File(outputPath);

		String formatName = ContentTypeUtils.formatFromContentType(contentType);

		ImageIO.write(bufferedImage, formatName, outputFile);
	}

	private BufferedImage applyCrop(BufferedImage originalImage, CropData cropData, String contentType) {

		int width = originalImage.getWidth();
		int height = originalImage.getHeight();

		int outputHeight = (int)Math.round((double)(cropData.getHeight() * cropData.getScaleE4()) / 10000d);
		int outputWidth = (int)Math.round((double)(cropData.getWidth() * cropData.getScaleE4()) / 10000d);


		BufferedImage result = null;
		if(Utils.uppercaseTrimmed(contentType).contains("JPG") || Utils.uppercaseTrimmed(contentType).contains("JPEG")){
			result = new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_INT_RGB);
		}else{
			result = new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_INT_ARGB);
		}

		// Create graphics context for the new image
		Graphics2D g2d = result.createGraphics();

		// Enable high quality rendering
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		// Draw the original image inside the result image
		int dx1 = 0;
		int dy1 = 0;
		int dx2 = outputWidth;
		int dy2 = outputHeight;
		int sx1 = cropData.getX();
		int sy1 = cropData.getY();
		int sx2 = sx1 + cropData.getWidth();
		int sy2 = sy1 + cropData.getHeight();

		g2d.drawImage(originalImage, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);

		g2d.dispose();

		return result;
	}

	private void saveBufferedImageToS3(BufferedImage bufferedImage, String bucket, String key) throws IOException {
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			String formatName = ContentTypeUtils.formatFromContentType(parameters.getContentType());
			ImageIO.write(bufferedImage, formatName, byteArrayOutputStream);
			byteArrayOutputStream.flush();
			byte[] imageInByte = byteArrayOutputStream.toByteArray();

			this.s3Client.putObject(builder ->
					builder.bucket(bucket)
							.key(key)
							.contentType(parameters.getContentType())
							.contentLength((long)imageInByte.length),
					RequestBody.fromBytes(imageInByte));
		}
	}
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Parameters extends AbstractTaskParameters {
		private String sourceS3Bucket;
		private String sourceS3Key;
		private String destinationS3Bucket;
		private String destinationS3Key;
		private CropData cropData;
		private String contentType;
		private AbstractTaskParameters nextJobParameters;
		private long createTime;

		public Parameters()
		{
			super();
		}

		public Parameters(String sourceS3Bucket, String sourceS3Key, String destinationS3Bucket, String destinationS3Key, CropData cropData, String contentType, AbstractTaskParameters nextJobParameters) {
			this();

			this.sourceS3Bucket = sourceS3Bucket;
			this.sourceS3Key = sourceS3Key;
			this.destinationS3Bucket = destinationS3Bucket;
			this.destinationS3Key = destinationS3Key;
			this.cropData = cropData;
			this.contentType = contentType;
			this.nextJobParameters = nextJobParameters;

			this.createTime = System.currentTimeMillis();
		}

		public String getSourceS3Bucket() {
			return sourceS3Bucket;
		}

		public String getSourceS3Key() {
			return sourceS3Key;
		}

		public String getDestinationS3Bucket() {
			return destinationS3Bucket;
		}

		public String getDestinationS3Key() {
			return destinationS3Key;
		}

		public CropData getCropData() {
			return cropData;
		}

		public String getContentType() {
			return contentType;
		}

		public AbstractTaskParameters getNextJobParameters() {
			return nextJobParameters;
		}

		@Override
		public long getCreateTime() {
			return createTime;
		}
	}
}
