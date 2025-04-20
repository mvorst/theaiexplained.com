package com.mattvorst.shared.logging.appender;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.mattvorst.shared.constant.EnvironmentConstants;
import com.mattvorst.shared.logging.model.LogEntry;
import com.mattvorst.shared.service.AmazonServiceFactory;
import com.mattvorst.shared.util.Environment;
import com.mattvorst.shared.util.Utils;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

public class SQSAppender extends AppenderBase {


	protected SqsAsyncClient sqsAsyncClient;
	protected String queueUrl;

	@Override
	public void start() {
		super.start();

		queueUrl = Environment.get(EnvironmentConstants.AWS_SQS_LOG_QUEUE);

		sqsAsyncClient = AmazonServiceFactory.getSqsAsyncClient(Environment.get(EnvironmentConstants.AWS_DEFAULT_PROFILE));
	}

	@Override
	public void stop() {
		if (sqsAsyncClient != null) {
			sqsAsyncClient.close();
		}

		super.stop();
	}

	@Override
	protected void append(Object eventObject) {
		if(eventObject instanceof LoggingEvent loggingEvent) {
			String messageBody = Utils.gson().toJson(new LogEntry(loggingEvent));

			SendMessageRequest sendMsgRequest = SendMessageRequest
					.builder()
					.queueUrl(queueUrl)
					.messageBody(messageBody)
					.build();

			sqsAsyncClient.sendMessage(sendMsgRequest).join();
		}
	}
}
