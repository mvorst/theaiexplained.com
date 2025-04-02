package com.theaiexplained.model.task;

import java.util.UUID;

import com.theaiexplained.constant.TaskStatus;
import com.theaiexplained.dao.model.Task;
import org.springframework.beans.BeanUtils;

public class ViewTask {
	private UUID taskUuid;
	private String title;
	private String summary;
	private String detail;
	private String reference;
	private long dateCreated;
	private long dateDue;
	private TaskStatus taskStatus;

	// Default constructor
	public ViewTask() {}

	// Constructor from Task entity
	public ViewTask(Task task) {
		if (task != null) {
			BeanUtils.copyProperties(task, this);
		}
	}

	// Getters and Setters
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
}