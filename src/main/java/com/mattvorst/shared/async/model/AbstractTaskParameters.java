package com.mattvorst.shared.async.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Map;

import com.mattvorst.shared.util.Utils;
import org.springframework.core.annotation.AnnotationUtils;

public abstract class AbstractTaskParameters {
	protected String taskName;
	private String origin;
	private String description;
	private String messageId;

	public String getOrigin() {
		return origin;
	}

	public AbstractTaskParameters() {
		Class enclosingClazz = getClass().getEnclosingClass();
		if (enclosingClazz != null) {
			AsyncTask asyncTask = AnnotationUtils.findAnnotation(enclosingClazz, AsyncTask.class);
			if (asyncTask != null) {
				this.taskName = asyncTask.value();
			} else {
				this.taskName = enclosingClazz.getSimpleName();
			}
		}
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getTaskName() {
		if (!Utils.empty(this.taskName)) {
			return this.taskName;
		}

		return taskName;
	}

	public abstract long getCreateTime();

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public QueueRunnable instantiateTask(Map<String, Object> dependencies)
			throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
		return instantiateTask(dependencies, QueueRunnable.class);
	}

	/**
	 * Create an instance of task given its parameters. The dependency map from the processor will be used for
	 * constructor injection when creating the task instance.
	 *
	 * There are a couple assumptions made about any task that will be instantiated.
	 * 1. It must extend {@code QueueRunnable}
	 * 2. It can only have one constructor that accepts arguments. It is fine if it also has
	 * a no argument constructor.
	 * 3. For the argument constructor, the last argument must be of type Parameter.
	 * @param dependencies
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private <T> T instantiateTask(Map<String, Object> dependencies, Class<T> taskType)
			throws InvocationTargetException, InstantiationException, IllegalAccessException {
		Class taskClazz = this.getClass().getEnclosingClass();
		if (!taskType.isAssignableFrom(taskClazz)) {
			throw new RuntimeException("All task classes must be a " + taskType);
		}

		Constructor<?>[] constructors = taskClazz.getConstructors();
		Constructor matchedConstructor = null;

		for (Constructor constructor : constructors) {
			if (constructor.getParameterCount() > 0) {
				if (matchedConstructor != null) {
					throw new RuntimeException("Found multiple constructor candidates in " + taskClazz.getName());
				}

				matchedConstructor = constructor;
			}
		}

		if (matchedConstructor == null) {
			return taskType.cast(matchedConstructor.newInstance());
		} else {
			Parameter[] parameters = matchedConstructor.getParameters();
			Object[] args = new Object[parameters.length];

			args[args.length - 1] = this;

			for (int i=0; i< parameters.length - 1; i++) {
				Parameter parameter = parameters[i];
				Object dependency = dependencies.get(parameter.getType().getName());
				if (dependency == null) {
					throw new RuntimeException("Unable to bind processor dependency to task." + parameter.getType().getName() + ":" + taskClazz.getName());
				} else {
					args[i] = dependency;
				}
			}

			return taskType.cast(matchedConstructor.newInstance(args));
		}
	}
}
