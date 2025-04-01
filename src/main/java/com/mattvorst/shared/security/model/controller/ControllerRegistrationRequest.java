package com.mattvorst.shared.security.model.controller;

public class ControllerRegistrationRequest {
	private String controllerType;
	private String hardwareVersion;
	private String softwareVersion;
	private String timeZone;
	private String controllerSecret;

	public String getControllerType() {
		return controllerType;
	}

	public void setControllerType(String controllerType) {
		this.controllerType = controllerType;
	}

	public String getControllerSecret() {
		return controllerSecret;
	}

	public void setControllerSecret(String controllerSecret) {
		this.controllerSecret = controllerSecret;
	}

	public String getHardwareVersion() {
		return hardwareVersion;
	}

	public void setHardwareVersion(String hardwareVersion) {
		this.hardwareVersion = hardwareVersion;
	}

	public String getSoftwareVersion() {
		return softwareVersion;
	}

	public void setSoftwareVersion(String softwareVersion) {
		this.softwareVersion = softwareVersion;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
}
