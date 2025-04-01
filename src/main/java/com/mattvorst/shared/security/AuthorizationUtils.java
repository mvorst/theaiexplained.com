package com.mattvorst.shared.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.mattvorst.shared.constant.EnvironmentConstants;
import com.mattvorst.shared.exception.UnauthorizedException;
import com.mattvorst.shared.model.Auditable;
import com.mattvorst.shared.security.constant.SubjectType;
import com.mattvorst.shared.security.token.AuthToken;
import com.mattvorst.shared.security.token.ControllerToken;
import com.mattvorst.shared.security.token.ServiceAccountToken;
import com.mattvorst.shared.security.token.UserToken;
import com.mattvorst.shared.util.Encryption;
import com.mattvorst.shared.util.Environment;
import com.mattvorst.shared.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationUtils {
	private static final Logger log = LoggerFactory.getLogger(AuthorizationUtils.class);

	public static String encryptToken(String token) {
		String encodedTokenBase64 = null;

		if (token != null) {

			try {
				byte[] bytes = token.getBytes(StandardCharsets.UTF_8);

				List<String> tokenKeyList = Environment.getList(EnvironmentConstants.TOKEN_KEY_256_LIST);
				List<String> tokenIVList = Environment.getList(EnvironmentConstants.TOKEN_IV_256_LIST);

				if (Utils.empty(tokenKeyList) || Utils.empty(tokenIVList)) {
					log.error("TOKEN_KEY_256_LIST or TOKEN_IV_256_LIST has no values");
				} else {
					// Encode the token with the first key in the list
					encodedTokenBase64 = Encryption.base64StringFromBytes(
							Encryption.encryptAES256(bytes, Encryption.bytesFromBase64String(tokenKeyList.get(0)), Encryption.bytesFromBase64String(tokenIVList.get(0))));

					log.debug("TOKEN_AUTH - Length: " + ((encodedTokenBase64 != null) ? encodedTokenBase64.length() : 0)
							+ " ENCODING SUCCESS");
					log.debug("TOKEN_AUTH HASHED ENCRYPTED_TOKEN:" + UUID.nameUUIDFromBytes(encodedTokenBase64.getBytes("UTF-8")));
				}
			} catch (Exception e) {
				log.error("TOKEN_AUTH - encode exception", e);
			}
		} else {
			log.debug("TOKEN_AUTH - no user token");
		}

		return encodedTokenBase64;
	}

	public static String decryptToken(String encryptedToken) {
		String decryptedToken = null;

		try {
			if (encryptedToken != null) {
				encryptedToken = encryptedToken.replaceAll("%2F", "/").replaceAll("%2B", "+").replaceAll("%3D", "=");
			}

			log.debug("TOKEN_AUTH - DECODING TOKEN - Length:" + ((encryptedToken != null) ? encryptedToken.length() : 0));
			log.debug("TOKEN_AUTH HASHED ENCRYPTED_TOKEN:" + UUID.nameUUIDFromBytes(encryptedToken.getBytes("UTF-8")));

			byte[] byteArray = null; // The array of bytes representing the token

			List<String> tokenKeyList = Environment.getList(EnvironmentConstants.TOKEN_KEY_256_LIST);
			List<String> tokenIVList = Environment.getList(EnvironmentConstants.TOKEN_IV_256_LIST);

			if (Utils.empty(tokenKeyList) || Utils.empty(tokenIVList)) {
				log.error("TOKEN_KEY_256_LIST or TOKEN_IV_256_LIST has no values");
			} else {
				int index = 0;
				do {
					byte[] key = Encryption.bytesFromBase64String(tokenKeyList.get(index));
					byte[] iv = Encryption.bytesFromBase64String(tokenIVList.get(index));

					try {
						byteArray = Encryption.decryptAES256(Encryption.bytesFromBase64String(encryptedToken), key, iv);
					} catch (Throwable t) {
					}

					index++;
				} while (byteArray == null && index < tokenKeyList.size() && index < tokenIVList.size());
			}

			// Fallback to AES128 encryption
			if (byteArray == null) {
				try {
					byteArray = Encryption.decryptAES128(Encryption.bytesFromBase64String(encryptedToken), Environment.getTokenPassword(), Environment.getTokenIV());
				} catch (Throwable t) {

				}
			}

			if (byteArray != null) {
				decryptedToken = new String(byteArray);
			}else{
				log.error("TOKEN_NOT_DECRYPTED Code Ref:DJSUI93Ki , Token:" + encryptedToken);
			}
		} catch (Exception e) {
			log.error("TOKEN_AUTH - decode exception Length:" + ((encryptedToken != null) ? encryptedToken.length() : 0), e);
			log.error("TOKEN_AUTH - Token: " + encryptedToken);
			decryptedToken = null;
		}

		return decryptedToken;
	}

	private static AuthToken getAuthToken() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
			return switch(jwtAuthenticationToken.getToken().getClaimAsString(JwtAuthenticationProvider.SUB_TYPE)){
				case "CONTROLLER" -> new ControllerToken(jwtAuthenticationToken.getToken(), jwtAuthenticationToken.isAuthenticated());
				case "USER" -> new UserToken(jwtAuthenticationToken.getToken(), jwtAuthenticationToken.isAuthenticated());
				case "SERVICE_ACCOUNT" -> new ServiceAccountToken(jwtAuthenticationToken.getToken(), jwtAuthenticationToken.isAuthenticated());
				default -> null;
			};
		}

		return null;
	}

	public static UserToken getUserToken() {
		AuthToken authToken = getAuthToken();
		if (authToken instanceof UserToken userToken) {
			return userToken;
		} else {
			throw new UnauthorizedException();
		}
	}

	public static Auditable updateAuditProperties(Auditable auditable) {
		AuthToken authToken = AuthorizationUtils.getAuthToken();

		boolean isCreated = false;

		if (auditable.getUpdatedDate() == null && auditable.getCreatedDate() == null) {
			auditable.setCreatedDate(new Date());
			auditable.setUpdatedDate(auditable.getCreatedDate());
			isCreated = true;
		} else {
			auditable.setUpdatedDate(new Date());
		}

		if (authToken != null) {
			if (isCreated) {
				auditable.setCreatedBySubject(authToken.getSubject());
				auditable.setUpdatedBySubject(authToken.getSubject());
			} else {
				auditable.setUpdatedBySubject(authToken.getSubject());
			}
		}

		return auditable;
	}

	public static SubjectType parseSubjectType(String subject) {
		try {
			if (subject != null) {
				String[] subjectParts = subject.split("/");
				if (subjectParts.length > 1) {
					if (subjectParts.length == 2) {
						return SubjectType.valueOf(subjectParts[0]);
					} else if (subjectParts.length == 3) {
						return SubjectType.valueOf(subjectParts[1]);
					}
				}
			}
		} catch (Exception e) {
			log.error("Unable to parse subject: " + subject);
		}

		return null;
	}
}
