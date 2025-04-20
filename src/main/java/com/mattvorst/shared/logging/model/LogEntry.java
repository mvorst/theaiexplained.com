package com.mattvorst.shared.logging.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import com.mattvorst.shared.constant.EnvironmentConstants;
import com.mattvorst.shared.constant.EnvironmentType;
import com.mattvorst.shared.util.Environment;

public class LogEntry {

	private String instanceId;
	private String environment;
	private EnvironmentType environmentType;
	private int buildNumber;
	private UUID userUuid;
	private String loggerName;
	private String level;
	private String message;
	private Date entryDate;
	private UUID entryHash;
	private String threadName;
	private String exceptionMessage;
	private StackTraceElement sourceStackTraceElement;
	private List<LogEntryStackTraceElement> stackTraceElementList;
	private String exceptionClassName;
	private String exceptionFileName;
	private String exceptionMethodName;
	private int exceptionLineNumber;
	private String requestId;
	private boolean searchDocument;

	public LogEntry() {
		super();
	}

	public LogEntry(LoggingEvent logEvent) {
		this();

		if(logEvent != null) {

			if(logEvent.getLevel() != null) {
				level = logEvent.getLevel().levelStr;
			}
			loggerName = logEvent.getLoggerName();
			if(logEvent.getMessage() != null) {
				message = logEvent.getMessage();
			}
			if(logEvent.getInstant() != null) {
				entryDate = new Date(logEvent.getInstant().toEpochMilli());
			}
			threadName = logEvent.getThreadName();
			buildNumber = Environment.getInt(EnvironmentConstants.BUILD_NUMBER);
//			sourceStackTraceElement = logEvent.getSource();
			if(logEvent.getThrowableProxy() != null) {
				String thrownMessage = (logEvent.getThrowableProxy().getMessage() != null) ? logEvent.getThrowableProxy().getMessage() : "";
				String thrownClass = logEvent.getThrowableProxy() != null ? logEvent.getThrowableProxy().getClass().getName() : "";
				exceptionMessage = thrownClass + ": " + thrownMessage;
				stackTraceElementList = new ArrayList<>();
				StackTraceElementProxy[] stackTraceElementArray = logEvent.getThrowableProxy().getStackTraceElementProxyArray();
				boolean classFound = false;
				if(stackTraceElementArray != null) {
					for(StackTraceElementProxy stackTraceElement : stackTraceElementArray) {
						stackTraceElementList.add(new LogEntryStackTraceElement(stackTraceElement));
						if(!classFound && stackTraceElement.getStackTraceElement().getClassName().startsWith("com.mattvorst")) {
							exceptionClassName = stackTraceElement.getStackTraceElement().getClassName();
							exceptionFileName = stackTraceElement.getStackTraceElement().getFileName();
							exceptionMethodName = stackTraceElement.getStackTraceElement().getMethodName();
							exceptionLineNumber = stackTraceElement.getStackTraceElement().getLineNumber();

							classFound = true;
						}
					}
				}
			}
			entryHash = UUID.randomUUID();
		}
	}

	public String getLoggerName() {
		return loggerName;
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	public String getExceptionMessage() {
		return exceptionMessage;
	}

	public void setExceptionMessage(String exceptionMessage) {
		this.exceptionMessage = exceptionMessage;
	}

	public StackTraceElement getSourceStackTraceElement() {
		return sourceStackTraceElement;
	}

	public void setSourceStackTraceElement(StackTraceElement sourceStackTraceElement) {
		this.sourceStackTraceElement = sourceStackTraceElement;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public EnvironmentType getEnvironmentType() {
		return environmentType;
	}

	public void setEnvironmentType(EnvironmentType environmentType) {
		this.environmentType = environmentType;
	}

	public Date getEntryDate() {
		return entryDate;
	}

	public void setEntryDate(Date entryDate) {
		this.entryDate = entryDate;
	}

	public UUID getEntryHash() {
		return entryHash;
	}

	public void setEntryHash(UUID entryHash) {
		this.entryHash = entryHash;
	}

	public String getExceptionClassName() {
		return exceptionClassName;
	}

	public void setExceptionClassName(String exceptionClassName) {
		this.exceptionClassName = exceptionClassName;
	}

	public String getExceptionFileName() {
		return exceptionFileName;
	}

	public void setExceptionFileName(String exceptionFileName) {
		this.exceptionFileName = exceptionFileName;
	}

	public String getExceptionMethodName() {
		return exceptionMethodName;
	}

	public void setExceptionMethodName(String exceptionMethodName) {
		this.exceptionMethodName = exceptionMethodName;
	}

	public int getExceptionLineNumber() {
		return exceptionLineNumber;
	}

	public void setExceptionLineNumber(int exceptionLineNumber) {
		this.exceptionLineNumber = exceptionLineNumber;
	}

	public List<LogEntryStackTraceElement> getStackTraceElementList() {
		return stackTraceElementList;
	}

	public void setStackTraceElementList(List<LogEntryStackTraceElement> stackTraceElementList) {
		this.stackTraceElementList = stackTraceElementList;
	}

	public UUID getUserUuid() {
		return userUuid;
	}

	public void setUserUuid(UUID userUuid) {
		this.userUuid = userUuid;
	}

	public int getBuildNumber() {
		return buildNumber;
	}

	public void setBuildNumber(int buildNumber) {
		this.buildNumber = buildNumber;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public boolean isSearchDocument() {
		return searchDocument;
	}

	public void setSearchDocument(boolean searchDocument) {
		this.searchDocument = searchDocument;
	}
}
