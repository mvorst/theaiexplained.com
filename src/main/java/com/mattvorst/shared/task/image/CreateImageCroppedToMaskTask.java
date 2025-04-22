package com.mattvorst.shared.task.image;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mattvorst.shared.async.model.AbstractTaskParameters;
import com.mattvorst.shared.async.model.AsyncTask;
import com.mattvorst.shared.async.model.QueueRunnable;
import com.mattvorst.shared.async.processor.TaskProcessor;
import com.mattvorst.shared.constant.EnvironmentConstants;
import com.mattvorst.shared.util.Environment;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@AsyncTask("CreateImageCroppedToMaskTask")
public class CreateImageCroppedToMaskTask extends QueueRunnable {

	private static final Logger log = LogManager.getLogger(CreateImageCroppedToMaskTask.class);

	private S3Client s3Client;
	private final TaskProcessor taskProcessor;
	private final Parameters parameters;

	public CreateImageCroppedToMaskTask() {
		super();

		this.taskProcessor = null;
		this.parameters = null;
	}

	public CreateImageCroppedToMaskTask(TaskProcessor taskProcessor, Parameters parameters) {
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

			applyMaskAndSave(parameters);

			if(parameters.getNextJobParameters() != null){
				taskProcessor.processLocally(parameters.getNextJobParameters());
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void applyMaskAndSave(Parameters parameters) throws IOException {
		try (ResponseInputStream sourceInputStream = this.s3Client.getObject(builder ->
				builder.bucket(parameters.getSourceS3Bucket())
						.key(parameters.getSourceS3Key()));

				ResponseInputStream svgInputStream = this.s3Client.getObject(builder ->
				builder.bucket(parameters.getMaskS3Bucket())
						.key(parameters.getMaskS3Key()));) {

			if (sourceInputStream != null && svgInputStream != null) {
				BufferedImage sourceImage = ImageIO.read(sourceInputStream);
				String svgMaskContent = readSvgContent(svgInputStream);

				BufferedImage bufferedImage = applyMask(sourceImage, svgMaskContent);
				if (bufferedImage != null) {
					saveBufferedImageToS3(bufferedImage, parameters.getDestinationS3Bucket(), parameters.getDestinationS3Key());
				}
			} else {
				log.error("Source or mask S3 object is null");
			}
		}
	}

	public void applyMaskAndSave(File inputImageFile, String svgMaskContent, String outputPath) throws IOException {
		BufferedImage originalImage = ImageIO.read(inputImageFile);

		BufferedImage bufferedImage = applyMask(originalImage, svgMaskContent);

		// Save the result as a PNG
		File outputFile = new File(outputPath);
		ImageIO.write(bufferedImage, "PNG", outputFile);
	}

	private BufferedImage applyMask(BufferedImage originalImage, String svgMaskContent) throws IOException {

		// Parse the SVG mask from string
		String parser = XMLResourceDescriptor.getXMLParserClassName();
		SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
		SVGDocument svgDoc = factory.createSVGDocument(null, new StringReader(svgMaskContent));

		// Get the root SVG element to determine dimensions
		SVGSVGElement svgRoot = svgDoc.getRootElement();

		// Assume the SVG has width and height attributes as a percentage of the image
		float svgWidth = 1;
		float svgHeight = 1;

		// Create a new transparent image with the same dimensions as the original
		int width = originalImage.getWidth();
		int height = originalImage.getHeight();
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		// Create graphics context for the new image
		Graphics2D g2d = result.createGraphics();

		// Enable high quality rendering
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		// Scale the SVG if needed to match the image dimensions
		float scaleX = width / svgWidth;
		float scaleY = height / svgHeight;

		AffineTransform transform = new AffineTransform();
		transform.scale(scaleX, scaleY);

		// Create a shape from the SVG to use as a clip
		Shape svgShape = createShapeFromSVG(svgDoc);
		if (svgShape != null) {
			// Apply the transformation to the shape
			svgShape = transform.createTransformedShape(svgShape);

			// Set the SVG shape as a clip
			g2d.setClip(svgShape);

			// Draw the original image
			g2d.drawImage(originalImage, 0, 0, null);
		}

		g2d.dispose();

		return result;
	}

	private Shape createShapeFromSVG(SVGDocument svgDoc) {
		// Note: This is a simplified implementation and may not work for all SVG files.
		// For production use, you might want to use a more robust SVG shape extraction
		// method or a dedicated library like Apache Batik's GVT (Graphic Vector Toolkit).

		try {
			// Using Batik's TranscoderInput and GraphicsNodeRenderContext would be more robust
			BridgeContext bridgeContext = new BridgeContext(new UserAgentAdapter());
			GVTBuilder builder = new GVTBuilder();
			DocumentLoader loader = new DocumentLoader(new UserAgentAdapter());
			bridgeContext.setDynamicState(BridgeContext.DYNAMIC);

			GraphicsNode rootGN = builder.build(bridgeContext, svgDoc);

			return rootGN.getOutline();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private String readSvgContent(InputStream inputStream) throws IOException {
		try (ByteArrayOutputStream maskOutputStream = new ByteArrayOutputStream()) {
			IOUtils.copy(inputStream, maskOutputStream);
			return maskOutputStream.toString();
		}
	}

	private void saveBufferedImageToS3(BufferedImage bufferedImage, String bucket, String key) throws IOException {
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			ImageIO.write(bufferedImage, "PNG", byteArrayOutputStream);
			byteArrayOutputStream.flush();
			byte[] imageInByte = byteArrayOutputStream.toByteArray();

			this.s3Client.putObject(builder ->
							builder.bucket(bucket)
									.key(key)
									.contentType("image/png")
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
		private String maskS3Bucket;
		private String maskS3Key;

		private AbstractTaskParameters nextJobParameters;
		private long createTime;

		public Parameters()
		{
			super();
		}

		public Parameters(String sourceS3Bucket, String sourceS3Key, String destinationS3Bucket, String destinationS3Key, String maskS3Bucket, String maskS3Key, AbstractTaskParameters nextJobParameters) {
			this();

			this.sourceS3Bucket = sourceS3Bucket;
			this.sourceS3Key = sourceS3Key;
			this.destinationS3Bucket = destinationS3Bucket;
			this.destinationS3Key = destinationS3Key;
			this.maskS3Bucket = maskS3Bucket;
			this.maskS3Key = maskS3Key;
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

		public String getMaskS3Bucket() {
			return maskS3Bucket;
		}

		public String getMaskS3Key() {
			return maskS3Key;
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
