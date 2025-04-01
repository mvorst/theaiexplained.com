package com.mattvorst.shared.async.processor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.mattvorst.shared.async.model.AbstractTaskParameters;
import com.mattvorst.shared.async.model.QueueRunnable;
import com.mattvorst.shared.async.model.RunnableResultMap;
import com.mattvorst.shared.dao.SecurityDao;
import com.mattvorst.shared.dao.SystemDao;
import com.mattvorst.shared.util.Utils;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class TaskProcessor {

	private final Logger log = LogManager.getLogger(TaskProcessor.class);

	@Autowired private SecurityDao securityDao;
	@Autowired private SystemDao systemDao;

	private Map<String, Object> dependencyMap;

	@Autowired(required = false)
	@Qualifier("asyncExecutor")
	private ThreadPoolTaskExecutor actionTaskExecutor;

	@PostConstruct
	public void init() {
		try {
			Field[] autowiredFields = FieldUtils.getFieldsWithAnnotation(this.getClass(), Autowired.class);
			this.dependencyMap = new HashMap<>();
			for (Field f : autowiredFields) {
				f.setAccessible(true);
				this.dependencyMap.put(f.getType().getName(), f.get(this));
				f.setAccessible(false);
			}
			this.dependencyMap.put(TaskProcessor.class.getName(), this);
		} catch (Throwable e) {
			log.error("Unable to initialize dependency map.", e);
			throw new RuntimeException(e);
		}
	}

	public CompletableFuture<Map<String, Object>> invokeTask(AbstractTaskParameters parameters) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
		QueueRunnable runnable = parameters.instantiateTask(dependencyMap);
		if(actionTaskExecutor != null) {
			if(runnable instanceof RunnableResultMap runnableResultMap){
				return CompletableFuture.supplyAsync(()-> { runnable.run(); return runnableResultMap.getResultMap(); }, actionTaskExecutor);
			}else{
				return CompletableFuture.supplyAsync(()-> { runnable.run(); return null; }, actionTaskExecutor);
			}
		}else{
			if(runnable instanceof RunnableResultMap runnableResultMap) {
				return CompletableFuture.supplyAsync(() -> { runnable.run(); return runnableResultMap.getResultMap(); });
			}else {
				return CompletableFuture.supplyAsync(() -> { runnable.run(); return null; });
			}
		}
	}

	public void processLocally(AbstractTaskParameters parameters){
			try {
				invokeTask(parameters)
						.thenAccept(result -> {
							log.info("SYNCHRONOUS_JOB_PROCESSED Body:" + Utils.gson().toJson(parameters));
						})
						.exceptionally(e -> {
							log.warn("SYNCHRONOUS_JOB_FAILED_LOCALLY Body:" + Utils.gson().toJson(parameters), e);
							//							amazonSQS.sendMessageAsync(awsSqsJobQueue, Utils.gson().toJson(parameters));
							return null;
						});
			} catch (Exception e) {
				log.warn("SYNCHRONOUS_JOB_NOT_PROCESSED_LOCALLY Body:" + Utils.gson().toJson(parameters), e);
				//				amazonSQS.sendMessageAsync(awsSqsJobQueue, Utils.gson().toJson(parameters));
			}
	}
}
