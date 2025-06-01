package com.thebridgetoai.service;

import java.util.Map;
import java.util.UUID;

import com.mattvorst.shared.exception.ValidationException;
import com.mattvorst.shared.model.DynamoResultList;
import com.mattvorst.shared.util.FieldValidator;
import com.thebridgetoai.constant.TaskStatus;
import com.thebridgetoai.dao.TaskDao;
import com.thebridgetoai.dao.model.Task;
import com.thebridgetoai.task.ViewTask;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * Service class for managing tasks.
 * This class provides methods for creating, retrieving, updating, and deleting tasks.
 */
@Service
public class TaskService {

	@Autowired private MessageSource messageSource;
	@Autowired private TaskDao taskDao;

	public Task getTask(UUID taskUuid) {
		return taskDao.getTask(taskUuid).join();
	}

	public Task createTask(ViewTask viewTask, UUID userUuid) throws ValidationException {
		validateTask(viewTask);

		// Create a new Task entity
		int count = 0;
		do{
			UUID taskUuid = UUID.randomUUID();
			if(taskDao.getTask(taskUuid) == null){
				viewTask.setTaskUuid(taskUuid);
				viewTask.setTaskStatus(TaskStatus.NOT_STARTED);
			}
		}while(viewTask.getTaskUuid() == null && count++ < 10);

		Task task = new Task();

		BeanUtils.copyProperties(viewTask, task);

		taskDao.saveTask(task).join();

		return task;
	}

	public Task updateTask(UUID taskUuid, ViewTask viewTask) throws ValidationException {
		validateTask(viewTask);

		// Get existing task
		Task task = getTask(taskUuid);
		if (task == null) {
			return null;
		}

		// Update fields
		BeanUtils.copyProperties(viewTask, task, "taskUuid", "createdDate", "createdBySubject");

		// Save changes
		taskDao.saveTask(task).join();

		return task;
	}

	public Task deleteTask(UUID taskUuid) {
		Task task = getTask(taskUuid);
		if (task != null) {
			taskDao.deleteTask(task).join();
		}
		return task;
	}

	public DynamoResultList<Task> getTaskListByUserUuid(UUID userUuid, int count, Map<String, AttributeValue> attributeValueMap) {
		return taskDao.getTaskListByUserUuid(userUuid, count, attributeValueMap).join();
	}

	public DynamoResultList<Task> getTaskListByUserUuidAndStatus(UUID userUuid, TaskStatus status, int count, Map<String, AttributeValue> attributeValueMap) {
		return taskDao.getTaskListByUserUuidAndStatus(userUuid, status, count, attributeValueMap).join();
	}

	private void validateTask(ViewTask viewTask) throws ValidationException {
		FieldValidator.get(messageSource, LocaleContextHolder.getLocale())
				.validateNotEmpty("title", viewTask.getTitle())
				.validateNotNull("status", viewTask.getTaskStatus())
				.apply();
	}
}