package com.theaiexplained.website.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

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
						.requestMatchers(new AntPathRequestMatcher("/*.action")).permitAll()
						.requestMatchers(new AntPathRequestMatcher("/WEB-INF/jsp/**")).permitAll()
						.requestMatchers(new AntPathRequestMatcher("/rest/auth/**")).permitAll()
						.requestMatchers("/error",
								"/rest/info",
								"/rest/api/**",
								"/rest/auth/**").permitAll()
						.anyRequest().authenticated()
				);
//				.oauth2ResourceServer(httpSecurityOAuth2ResourceServerConfigurer -> {
//					httpSecurityOAuth2ResourceServerConfigurer.jwt(jwt -> jwt.decoder(jwtDecoder(jwkSource())));
//				})
//				.formLogin(form ->
//						form.loginPage("/login").permitAll())
//				.userDetailsService(userDetailsService());
		return http.build();
	}

//	@Bean
//	public UserDetailsService userDetailsService() {
//		return new DynamoUserDetailManager();
//	}

//	@Bean
//	@Primary
//	public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
//		return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
//	}
//
//	@Bean
//	@Qualifier("ignoreExpirationJwtDecoder")
//	public JwtDecoder ignoreExpirationJwtDecoder(JWKSource<SecurityContext> jwkSource) {
//		NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
//		jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(new ArrayList<>()));
//
//		return jwtDecoder;
//	}

//	@Bean
//	public JWKSource<SecurityContext> jwkSource() {
//		RSAKey rsaKey = generateRsa();
//		JWKSet jwkSet = new JWKSet(rsaKey);
//
//		return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
//	}
//
//	@Bean
//	public RSAPublicKey rsaPublicKey() throws JOSEException {
//		RSAKey rsaKey = generateRsa();
//		JWKSet jwkSet = new JWKSet(rsaKey);
//
//		return rsaKey.toRSAPublicKey();
//	}
//
//	private static RSAKey generateRsa() {
//		try {
//			String keyId = Environment.get(EnvironmentConstants.JWT_KEY_ID);
//			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//
//			PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(Base64.decodeBase64(Environment.get(EnvironmentConstants.JWT_PUBLIC_KEY))));
//			PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.decodeBase64(Environment.get(EnvironmentConstants.JWT_PRIVATE_KEY))));
//
//			if (publicKey instanceof RSAPublicKey && privateKey instanceof RSAPrivateKey) {
//				return new RSAKey.Builder((RSAPublicKey) publicKey).privateKey((RSAPrivateKey) privateKey).keyID(keyId).build();
//			}
//		}catch (NoSuchAlgorithmException | InvalidKeySpecException e){
//			log.error("Error generating RSA key", e);
//		}
//
//		return null;
//	}
//
//	@Bean
//	public JwtService jwtService(RSAPublicKey rsaPublicKey, JWKSource<SecurityContext> jwkSource, AuthorizationServerSettings authorizationServerSettings) {
//		NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey(rsaPublicKey).build();
//		NimbusJwtEncoder jwtEncoder = new NimbusJwtEncoder(jwkSource);
//		return new JwtService(jwtDecoder, jwtEncoder, authorizationServerSettings);
//	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
