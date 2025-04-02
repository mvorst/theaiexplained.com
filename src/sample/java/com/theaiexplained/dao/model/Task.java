package com.theaiexplained.dao.model;

import java.util.UUID;

import com.mattvorst.shared.model.DefaultAuditable;
import com.mattvorst.shared.util.Utils;
import com.theaiexplained.constant.TaskStatus;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

@DynamoDbBean
public class Task extends DefaultAuditable {
	public static final String TABLE_NAME = "task";

	private UUID taskUuid;
	private String title;
	private String summary;
	private String detail;
	private String reference;
	private long dateCreated;
	private long dateDue;
	private TaskStatus taskStatus;
	private UUID userUuid;

	// Constructors
	public Task() {}

	public Task(UUID taskUuid) {
		this.taskUuid = taskUuid;
	}

	// Getters and Setters
	@DynamoDbPartitionKey
	@DynamoDbSecondarySortKey(indexNames = {"userUuid-taskUuid-index"})
	public UUID getTaskUuid() {
		return taskUuid;
	}

	public void setTaskUuid(UUID taskUuid) {
		this.taskUuid = taskUuid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public long getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(long dateCreated) {
		this.dateCreated = dateCreated;
	}

	public long getDateDue() {
		return dateDue;
	}

	public void setDateDue(long dateDue) {
		this.dateDue = dateDue;
	}

	public TaskStatus getTaskStatus() {
		return taskStatus;
	}

	public void setTaskStatus(TaskStatus taskStatus) {
		this.taskStatus = taskStatus;
	}

	@DynamoDbSecondaryPartitionKey(indexNames = {"userUuid-taskUuid-index", "userUuid-statusCreatedDateAndTaskUuid-index"})
	public UUID getUserUuid() {
		return userUuid;
	}

	public void setUserUuid(UUID userUuid) {
		this.userUuid = userUuid;
	}

	@DynamoDbSecondarySortKey(indexNames = {"userUuid-taskStatusCreatedDateAndTaskUuid-index"})
	public String getTaskStatusCreatedDateAndTaskUuid() {
		return taskStatus + "|" + Utils.gmtDateFormat().format(getCreatedDate()) + "|" + taskUuid;
	}

	public void setTaskStatusCreatedDateAndTaskUuid(String taskStatusCreatedDateAndTaskUuid){}
}