package com.theaiexplained.website.task;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mattvorst.shared.async.model.AbstractTaskParameters;
import com.mattvorst.shared.async.model.AsyncTask;
import com.mattvorst.shared.async.model.QueueRunnable;
import com.theaiexplained.website.dao.ContentDao;
import com.theaiexplained.website.dao.model.Content;
import com.theaiexplained.website.dao.model.FeaturedContent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;

@AsyncTask("ContentUpdateIndexTask")
public class ContentUpdateIndexTask extends QueueRunnable {

	private static final Logger log = LogManager.getLogger(ContentUpdateIndexTask.class);

	private final ContentDao contentDao;
	private final Parameters parameters;

	public ContentUpdateIndexTask() {
		super();

		this.contentDao = null;
		this.parameters = null;
	}

	public ContentUpdateIndexTask(ContentDao contentDao, Parameters parameters) {
		super();

		this.contentDao = contentDao;
		this.parameters = parameters;
	}

	@Override
	public void run() {
		try {
			Content content = contentDao.getContent(parameters.getContentUuid()).get();
			if(content != null){
				FeaturedContent featuredContent = contentDao.getFeaturedContent(content.getContentCategoryType(), content.getContentUuid()).get();
//				CompletableFuture<DynamoResultList<ContentAssociation>> contentAssociationFuture = contentDao.getContentAssociationListByContentUuid(content.getContentUuid(), 100, null);

				if(content.isFeatured()){
					if(featuredContent == null){
						// Create a new verison
						featuredContent = new FeaturedContent();
					}

					BeanUtils.copyProperties(content, featuredContent);

					contentDao.saveFeaturedContent(featuredContent).join();
				}else{
					if(featuredContent != null) {
						contentDao.deleteFeaturedContent(featuredContent);
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Parameters extends AbstractTaskParameters {
		private UUID contentUuid;
		private long createTime;

		public Parameters()
		{
			super();
		}

		public Parameters(UUID contentUuid) {
			this();

			this.contentUuid = contentUuid;
			this.createTime = System.currentTimeMillis();
		}

		public UUID getContentUuid() {
			return contentUuid;
		}

		@Override
		public long getCreateTime() {
			return createTime;
		}
	}
}
