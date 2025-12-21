package com.thebridgetoai.website.config;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

import com.mattvorst.shared.constant.EnvironmentConstants;
import com.mattvorst.shared.security.service.DynamoUserDetailManager;
import com.mattvorst.shared.security.service.JwtService;
import com.mattvorst.shared.util.Environment;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http
				.cors(cors -> cors.disable())
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/*.action").permitAll()
						.requestMatchers("/WEB-INF/jsp/**").permitAll()
						.requestMatchers("/rest/auth/**").permitAll()
						.requestMatchers("/error",
								"/rest/api/**",
								"/rest/auth/**").permitAll()
						.anyRequest().authenticated()
				)
				.oauth2ResourceServer(httpSecurityOAuth2ResourceServerConfigurer -> {
					httpSecurityOAuth2ResourceServerConfigurer.jwt(jwt -> jwt.decoder(jwtDecoder(jwkSource())));
				})
				.formLogin(form ->
						form.loginPage("/login").permitAll())
				.userDetailsService(userDetailsService());
		return http.build();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		return new DynamoUserDetailManager();
	}

	@Bean
	@Primary
	public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
		return createJwtDecoder();
	}

	private NimbusJwtDecoder createJwtDecoder() {
		RSAKey rsaKey = generateRsa();
		try {
			return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
		} catch (JOSEException e) {
			throw new RuntimeException("Failed to create JWT decoder", e);
		}
	}

	@Bean
	@Qualifier("ignoreExpirationJwtDecoder")
	public JwtDecoder ignoreExpirationJwtDecoder(JWKSource<SecurityContext> jwkSource) {
		NimbusJwtDecoder jwtDecoder = createJwtDecoder();
		jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(new ArrayList<>()));

		return jwtDecoder;
	}

	@Bean
	public JWKSource<SecurityContext> jwkSource() {
		RSAKey rsaKey = generateRsa();
		JWKSet jwkSet = new JWKSet(rsaKey);

		return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
	}

	@Bean
	public RSAPublicKey rsaPublicKey() throws JOSEException {
		RSAKey rsaKey = generateRsa();

		return rsaKey.toRSAPublicKey();
	}

	private static RSAKey generateRsa() {
		try {
			String keyId = Environment.get(EnvironmentConstants.JWT_KEY_ID);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");

			PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(Base64.decodeBase64(Environment.get(EnvironmentConstants.JWT_PUBLIC_KEY))));
			PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.decodeBase64(Environment.get(EnvironmentConstants.JWT_PRIVATE_KEY))));

			if (publicKey instanceof RSAPublicKey && privateKey instanceof RSAPrivateKey) {
				return new RSAKey.Builder((RSAPublicKey) publicKey).privateKey((RSAPrivateKey) privateKey).keyID(keyId).build();
			}
		}catch (NoSuchAlgorithmException | InvalidKeySpecException e){
			log.error("Error generating RSA key", e);
		}

		return null;
	}

	@Bean
	public AuthorizationServerSettings authorizationServerSettings() {
		String issuer = Environment.get(EnvironmentConstants.JWT_ISSUER);
		if (issuer == null || issuer.isEmpty()) {
			issuer = "https://thebridgeto.ai";
		}
		return AuthorizationServerSettings.builder()
				.issuer(issuer)
				.build();
	}

	@Bean
	public JwtService jwtService(RSAPublicKey rsaPublicKey, JWKSource<SecurityContext> jwkSource, AuthorizationServerSettings authorizationServerSettings) {
		NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey(rsaPublicKey).build();
		NimbusJwtEncoder jwtEncoder = new NimbusJwtEncoder(jwkSource);
		return new JwtService(jwtDecoder, jwtEncoder, authorizationServerSettings);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
