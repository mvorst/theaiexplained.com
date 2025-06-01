package com.thebridgetoai.util;

import java.util.Date;

import com.mattvorst.shared.constant.EnvironmentConstants;
import com.mattvorst.shared.model.DynamoResultList;
import com.mattvorst.shared.util.Environment;
import com.mattvorst.shared.util.Streams;
import com.thebridgetoai.website.constant.ContentCategoryType;
import com.thebridgetoai.website.dao.ContentDao;
import com.thebridgetoai.website.dao.model.Content;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ContentDataPublishMain {

	public static void main(String[] args) {

		Environment.instance(EnvironmentConstants.ENV_VORST);

		ContentDao contentDao = new ContentDao("thebridgetoai");

		DynamoResultList<Content> dynamoResultList = contentDao.getAllContent(20, null).join();
		Streams.of(dynamoResultList.getList()).forEach(content -> {
			content.setContentCategoryType(ContentCategoryType.BLOG_POST);
			content.setPublishedDate(new Date());
			contentDao.saveContent(content).join();
		});

		System.out.println("Complete");
		System.exit(0);
	}

}