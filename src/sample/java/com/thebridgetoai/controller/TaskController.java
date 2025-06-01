package com.thebridgetoai.controller;

import java.util.Map;
import java.util.UUID;

import com.mattvorst.shared.controller.BaseRestController;
import com.mattvorst.shared.exception.ValidationException;
import com.mattvorst.shared.model.DynamoResultList;
import com.mattvorst.shared.security.AuthorizationUtils;
import com.mattvorst.shared.security.token.UserToken;
import com.mattvorst.shared.util.CursorUtils;
import com.thebridgetoai.constant.TaskStatus;
import com.thebridgetoai.dao.model.Task;
import com.thebridgetoai.task.ViewTask;
import com.thebridgetoai.service.TaskService;
import org.apache.commons.lang3.stream.Streams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@RestController
@RequestMapping("/rest/api/{version}/task")
public class TaskController extends BaseRestController {

	@Autowired private TaskService taskService;

	@GetMapping("/{taskUuid}")
	public ResponseEntity<ViewTask> getTask(@PathVariable UUID taskUuid) {
		Task task = taskService.getTask(taskUuid);
		if (task == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(new ViewTask(task));
	}

	@PostMapping("/")
	public ResponseEntity<ViewTask> createTask(@RequestBody ViewTask viewTask) throws ValidationException {
		UserToken userToken = AuthorizationUtils.getUserToken();
		UUID userUuid = userToken.getUserUuid();

		Task task = taskService.createTask(viewTask, userUuid);
		return new ResponseEntity<>(new ViewTask(task), HttpStatus.CREATED);
	}

	@PutMapping("/{taskUuid}")
	public ResponseEntity<ViewTask> updateTask(@PathVariable UUID taskUuid, @RequestBody ViewTask viewTask) throws ValidationException {
		Task task = taskService.updateTask(taskUuid, viewTask);
		if (task == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(new ViewTask(task));
	}

	@DeleteMapping("/{taskUuid}")
	public ResponseEntity<ViewTask> deleteTask(@PathVariable UUID taskUuid) {
		Task task = taskService.deleteTask(taskUuid);
		if (task == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(new ViewTask(task));
	}

	@GetMapping("/")
	public ResponseEntity<DynamoResultList<ViewTask>> getTaskList(@RequestParam(required = false) String cursor, @RequestParam(required = false, defaultValue = "10") int count, @RequestParam(required = false) TaskStatus taskSstatus) {

		Map<String, AttributeValue> attributeValueMap = CursorUtils.decodeLastEvaluatedKeyFromCursor(cursor);

		UserToken userToken = AuthorizationUtils.getUserToken();
		UUID userUuid = userToken.getUserUuid();

		DynamoResultList<Task> dynamoResultList;

		if (taskSstatus != null) {
			dynamoResultList = taskService.getTaskListByUserUuidAndStatus(userUuid, taskSstatus, count, attributeValueMap);
		} else {
			dynamoResultList = taskService.getTaskListByUserUuid(userUuid, count, attributeValueMap);
		}

		return ResponseEntity.ok(new DynamoResultList<>(
				Streams.of(dynamoResultList.getList()).map(ViewTask::new).toList(),
				dynamoResultList.getLastEvaluatedKey()));
	}
}