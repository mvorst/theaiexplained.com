package com.mattvorst.shared.logging.model;

import ch.qos.logback.classic.spi.StackTraceElementProxy;

public class LogEntryStackTraceElement {

	private String exceptionClassName;
	private String exceptionFileName;
	private String exceptionMethodName;
	private int exceptionLineNumber;

	public LogEntryStackTraceElement() {
		super();
	}

	public LogEntryStackTraceElement(StackTraceElementProxy stackTraceElement) {
		this();

		exceptionClassName = stackTraceElement.getStackTraceElement().getClassName();
		exceptionFileName = stackTraceElement.getStackTraceElement().getFileName();
		exceptionMethodName = stackTraceElement.getStackTraceElement().getMethodName();
		exceptionLineNumber = stackTraceElement.getStackTraceElement().getLineNumber();
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
}
