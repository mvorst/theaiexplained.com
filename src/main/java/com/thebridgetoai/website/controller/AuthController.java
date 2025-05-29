package com.thebridgetoai.website.controller;

import java.util.Map;

import com.mattvorst.shared.security.model.user.UserLoginRequest;
import com.thebridgetoai.website.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/auth")
public class AuthController {

	private static final Logger log = LoggerFactory.getLogger(AuthController.class);

	@Autowired
	private AuthService authService;

	@PostMapping(value = "/user/login")
	public ResponseEntity<?> userLogin(@RequestBody UserLoginRequest request) {
		try {
			String token = authService.authenticateWithEmailAndPassword(request.getUsername(), request.getPassword());
			return ResponseEntity.ok().body(Map.of("token", token));
		} catch (AuthenticationException e) {
			return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
		}
	}
}
