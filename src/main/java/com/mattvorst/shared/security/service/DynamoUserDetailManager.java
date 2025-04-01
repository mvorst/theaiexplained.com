package com.mattvorst.shared.security.service;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.mattvorst.shared.dao.SecurityDao;
import com.mattvorst.shared.dao.model.security.SourceUser;
import com.mattvorst.shared.security.constant.Source;
import com.mattvorst.shared.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

@Service
public class DynamoUserDetailManager implements UserDetailsManager, UserDetailsPasswordService {

	@Autowired private SecurityDao securityDao;


	@Override
	public void createUser(UserDetails userDetails) {
		// TODO Auto-generated method stub
	}

	@Override
	public void updateUser(UserDetails userDetails) {
		// TODO Auto-generated method stub
	}

	@Override
	public void deleteUser(String username) {
		// TODO Auto-generated method stub
	}

	@Override
	public void changePassword(String oldPassword, String newPassword) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean userExists(String username) {
		String[] sourceUserParts = username.split("\\|");
		if(sourceUserParts.length != 2) {
			throw new IllegalArgumentException("Invalid username format");
		}

		Source source = Utils.safeToEnum(sourceUserParts[0], Source.class);
		if(source == null) {
			throw new IllegalArgumentException("Invalid source");
		}

		try {
			SourceUser sourceUser = securityDao.getSourceUser(source, sourceUserParts[1]).get();
			if(sourceUser != null) {
				return true;
			}
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}

		return false;
	}

	@Override
	public UserDetails loadUserByUsername(String username) {
		UserDetails userDetails = null;

		String[] sourceUserParts = username.split("\\|");
		if(sourceUserParts.length != 2) {
			throw new IllegalArgumentException("Invalid username format");
		}

		Source source = Utils.safeToEnum(sourceUserParts[0], Source.class);
		if(source == null) {
			throw new IllegalArgumentException("Invalid source");
		}

		try {
			userDetails = securityDao.getSourceUser(source, sourceUserParts[1]).thenApply((sourceUser) -> {
				if(sourceUser != null) {
					try {
						return securityDao.getUser(sourceUser.getUserUuid()).thenApply((user) -> {
							if(user != null) {
								UUID userPasswordTableUuid = UUID.nameUUIDFromBytes((user.getUserUuid() + "|" + user.getPasswordUuid()).getBytes());
								securityDao.getUserPassword(userPasswordTableUuid, 0).thenAccept((userPassword) -> {
									if(userPassword != null) {
										user.setEncodedPassword(userPassword.getEncodedPassword());
									}
								}).join();
							}
							return user;
						}).get();
					} catch (InterruptedException | ExecutionException e) {
						throw new RuntimeException(e);
					}
				}
				return null;
			}).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}

		return userDetails;
	}

	@Override
	public UserDetails updatePassword(UserDetails user, String newPassword) {
		// TODO Auto-generated method stub
		return null;
	}
}
