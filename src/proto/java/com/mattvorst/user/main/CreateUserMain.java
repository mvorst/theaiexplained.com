package com.mattvorst.user.main;

import java.util.Date;
import java.util.UUID;

import com.mattvorst.shared.constant.CountryCode;
import com.mattvorst.shared.constant.EnvironmentConstants;
import com.mattvorst.shared.constant.Status;
import com.mattvorst.shared.dao.SecurityDao;
import com.mattvorst.shared.dao.model.security.SourceUser;
import com.mattvorst.shared.dao.model.security.User;
import com.mattvorst.shared.dao.model.security.UserPassword;
import com.mattvorst.shared.security.constant.Source;
import com.mattvorst.shared.util.Environment;
import com.mattvorst.shared.util.Utils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class CreateUserMain {

	public static void main(String[] args) {

		Environment.instance(EnvironmentConstants.ENV_VORST);

		SecurityDao securityDao = new SecurityDao("theaiexplained-ci");

		UUID userUuid = UUID.fromString("3386d6ec-96a7-4ea5-bf3e-c33c00288d89");
		String emailAddress = "mavorst@gmail.com";
		String firstName = "Matt";
		String lastName = "Vorst";


		SourceUser sourceUser = securityDao.getSourceUser(Source.EMAIL, Utils.uppercaseTrimmed(emailAddress)).join();
		if(sourceUser == null){
			sourceUser = new SourceUser();
			sourceUser.setSource(Source.EMAIL);
			sourceUser.setSourceId(Utils.uppercaseTrimmed(emailAddress));
			sourceUser.setSourceUserUuid(UUID.randomUUID());
			sourceUser.setUserUuid(userUuid);
		}

		sourceUser.setDefaultSource(false);
		sourceUser.setCreatedBySubject("mattvorst");
		sourceUser.setCreatedDate(new Date());
		sourceUser.setUpdatedBySubject("mattvorst");
		sourceUser.setUpdatedDate(new Date());

		securityDao.saveSourceUser(sourceUser).join();

		User user = securityDao.getUser(userUuid).join();
		if(user == null){
			user = new User();
			user.setUserUuid(userUuid);
			user.setPasswordUuid(UUID.randomUUID());
			user.setDefaultEmailAddress(emailAddress);
			user.setStatus(Status.ACTIVE);
			user.setFirstName(firstName);
			user.setLastName(lastName);
			user.setRegistered(true);
			user.setTimeZone("America/New_York");
			user.setDefaultPhoneCountry(CountryCode.US);
			user.setDefaultPhoneNumber("+15135828131");
			user.setDefaultLocale("en_US");
			user.setJoinedDate(new Date());
			user.setEncodedPassword(null);
			user.setEmailVerified(true);
			user.setAuthorities(null);
			user.setAccountNonExpired(true);
			user.setAccountNonLocked(true);
			user.setCredentialsNonExpired(true);

			securityDao.saveUser(user).join();

			user = securityDao.getUser(userUuid).join();
		}

		UserPassword userPassword = null;
		UUID passwordUuid = null;
		if(user.getPasswordUuid() != null) {
			passwordUuid = user.getPasswordUuid();
		}else{
			passwordUuid = UUID.randomUUID();
		}
		do {
			UUID userPasswordTableUuid = UUID.nameUUIDFromBytes((user.getUserUuid() + "|" + passwordUuid).getBytes());
			if(securityDao.getUserPassword(userPasswordTableUuid, 0).join() == null) {
				String encodedPassword = new BCryptPasswordEncoder().encode("password");

				userPassword = new UserPassword();
				userPassword.setPasswordUuid(userPasswordTableUuid);
				userPassword.setRevision(0);
				userPassword.setStatus(user.getStatus());
				userPassword.setEncodedPassword(encodedPassword);
				securityDao.saveUserPassword(userPassword).join();
				userPassword.setRevision(System.currentTimeMillis());
				securityDao.saveUserPassword(userPassword).join();

				user.setPasswordUuid(passwordUuid);
				user.setUpdatedBySubject("mattvorst");
				user.setUpdatedDate(new Date());
				securityDao.saveUser(user).join();
			}
		} while(userPassword == null);


		System.out.println("Complete");
		System.exit(0);
	}
}
