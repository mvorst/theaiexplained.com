package com.theaiexplained.website.service;

import java.util.UUID;

import com.mattvorst.shared.async.processor.TaskProcessor;
import com.mattvorst.shared.constant.EnvironmentConstants;
import com.mattvorst.shared.dao.SecurityDao;
import com.mattvorst.shared.dao.model.security.User;
import com.mattvorst.shared.security.constant.Source;
import com.mattvorst.shared.security.model.controller.ControllerRegistrationRequest;
import com.mattvorst.shared.security.service.JwtService;
import com.mattvorst.shared.util.Environment;
import com.mattvorst.shared.util.Utils;
import com.theaiexplained.website.dao.UserDao;
import com.theaiexplained.website.dao.model.user.UserProperties;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
	@Autowired private UserDetailsService userDetailsService;
	@Autowired private JwtService jwtService;
	@Autowired private PasswordEncoder passwordEncoder;

	@Autowired TaskProcessor taskProcessor;

	@Autowired private SecurityDao securityDao;
	@Autowired private UserDao userDao;

	public String authenticateWithEmailAndPassword(String emailAddress, String password) {
		if(userDetailsService.loadUserByUsername(Source.EMAIL + "|" + Utils.uppercaseTrimmed(emailAddress)) instanceof User user) {
			if (passwordEncoder.matches(password, user.getPassword())) {
				Jwt jwt = jwtService.generateUserJwt(user);

				return jwt.getTokenValue();
			}
		}
		throw new BadCredentialsException("Invalid credentials");
	}
}
