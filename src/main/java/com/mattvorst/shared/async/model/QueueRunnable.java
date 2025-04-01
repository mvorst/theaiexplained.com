package com.mattvorst.shared.async.model;

public abstract class QueueRunnable implements Runnable {
	private String className;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
}

