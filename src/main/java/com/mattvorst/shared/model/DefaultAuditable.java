package com.mattvorst.shared.model;

import java.util.Date;

import com.mattvorst.shared.dao.convert.DateAttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;

public class DefaultAuditable implements Auditable {
	private Date updatedDate;
	private String updatedBySubject;

	private Date createdDate;
	private String createdBySubject;


	@Override
	@DynamoDbConvertedBy(DateAttributeConverter.class)
	public Date getUpdatedDate() {
		return updatedDate;
	}

	@Override
	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}

	@Override
	public String getUpdatedBySubject() {
		return updatedBySubject;
	}

	@Override
	public void setUpdatedBySubject(String updatedBySubject) {
		this.updatedBySubject = updatedBySubject;
	}

	@Override
	@DynamoDbConvertedBy(DateAttributeConverter.class)
	public Date getCreatedDate() {
		return createdDate;
	}

	@Override
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	@Override
	public String getCreatedBySubject() {
		return createdBySubject;
	}

	@Override
	public void setCreatedBySubject(String createdBySubject) {
		this.createdBySubject = createdBySubject;
	}
}
