package com.mattvorst.shared.security.model.controller;

import java.util.UUID;

public class ControllerLoginRequest {
	private UUID controllerUuid;
	private String controllerSecret;
	private String serverSecret;

	public UUID getControllerUuid() {
		return controllerUuid;
	}

	public void setControllerUuid(UUID controllerUuid) {
		this.controllerUuid = controllerUuid;
	}

	public String getControllerSecret() {
		return controllerSecret;
	}

	public void setControllerSecret(String controllerSecret) {
		this.controllerSecret = controllerSecret;
	}

	public String getServerSecret() {
		return serverSecret;
	}

	public void setServerSecret(String serverSecret) {
		this.serverSecret = serverSecret;
	}
}
