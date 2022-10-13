package inha.tnt.hbc.util;

import static inha.tnt.hbc.model.ErrorCode.*;
import static inha.tnt.hbc.security.oauth2.OAuth2Attributes.*;
import static inha.tnt.hbc.util.Constants.*;
import static inha.tnt.hbc.util.JwtUtils.JwtType.*;

import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import inha.tnt.hbc.domain.member.entity.Member;
import inha.tnt.hbc.exception.InvalidRequestHeaderException;
import inha.tnt.hbc.model.ErrorResponse.FieldError;
import inha.tnt.hbc.security.jwt.JwtAuthenticationToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

// TODO: static 메소드 전환 고려
//  https://okky.kr/articles/698002
@Component
public class JwtUtils {

	public static final String CLAIM_AUTHORITIES = "authorities";
	public static final String TOKEN_TYPE = "Bearer"; // RFC 6750: JWT, OAuth 2.0 token are bearer tokens
	private static final String CLAIM_ISSUER = "issuer";
	private static final String CLAIM_PRIMARY_KEY = "pk";
	private static final String HEADER_TYPE = "typ";
	private static final String TOKEN_NAME = "JWT";
	private static final String TOKEN_ISSUER = "hbc";
	private static final int TOKEN_PREFIX_LENGTH = 7;

	@Value("${jwt.valid.access}")
	private long ACCESS_TOKEN_VALIDITY;
	@Value("${jwt.valid.refresh}")
	private long REFRESH_TOKEN_VALIDITY;
	@Value("${jwt.key}")
	private String SECRET_KEY;

	public String extractJwt(String authorizationHeader) {
		validateAuthorizationHeader(authorizationHeader);

		return authorizationHeader.substring(TOKEN_PREFIX_LENGTH);
	}

	public Authentication getAuthentication(String token) {
		final Claims claims = getAllClaims(token);
		final List<SimpleGrantedAuthority> authorities = getAuthorities(claims);
		final User principal = new User(claims.getSubject(), EMPTY, authorities);

		return JwtAuthenticationToken.of(principal, token, authorities);
	}

	public String generateJwt(long validity, Map<String, Object> claims, String subject) {
		return generateJwt(validity, new HashMap<>(), claims, subject);
	}

	public String generateAccessToken(OAuth2User oAuth2User) {
		final Map<String, Object> claims = new HashMap<>();
		claims.put(CLAIM_AUTHORITIES, combineAuthorities(oAuth2User));
		claims.put(CLAIM_PRIMARY_KEY, oAuth2User.getAttributes().get(PRIMARY_KEY));

		return generateJwt(ACCESS_TOKEN_VALIDITY, claims, ACCESS_TOKEN.name());
	}

	public String generateRefreshToken(OAuth2User oAuth2User) {
		final Map<String, Object> claims = new HashMap<>();
		claims.put(CLAIM_AUTHORITIES, combineAuthorities(oAuth2User));
		claims.put(CLAIM_PRIMARY_KEY, oAuth2User.getAttributes().get(PRIMARY_KEY));

		return generateJwt(REFRESH_TOKEN_VALIDITY, claims, REFRESH_TOKEN.name());
	}

	public String generateAccessToken(Member member) {
		final Map<String, Object> claims = new HashMap<>();
		claims.put(CLAIM_AUTHORITIES, member.getAuthorities());
		claims.put(CLAIM_PRIMARY_KEY, member.getId());

		return generateJwt(ACCESS_TOKEN_VALIDITY, claims, ACCESS_TOKEN.name());
	}

	public String generateRefreshToken(Member member) {
		final Map<String, Object> claims = new HashMap<>();
		claims.put(CLAIM_AUTHORITIES, member.getAuthorities());
		claims.put(CLAIM_PRIMARY_KEY, member.getId());

		return generateJwt(REFRESH_TOKEN_VALIDITY, claims, REFRESH_TOKEN.name());
	}

	public String generateJwt(long validity, Map<String, Object> headers, Map<String, Object> claims, String subject) {
		final Date now = new Date(System.currentTimeMillis());
		final Date expiration = new Date(now.getTime() + validity);
		headers.put(HEADER_TYPE, TOKEN_NAME);
		claims.put(CLAIM_ISSUER, TOKEN_ISSUER);
		return Jwts.builder()
			.setHeader(headers)
			.setClaims(claims)
			.setSubject(subject)
			.setIssuedAt(now)
			.setExpiration(expiration)
			.signWith(SignatureAlgorithm.HS512, SECRET_KEY)
			.compact();
	}

	public Long getPrimaryKey(String token) {
		return getClaim(token, claims -> (Long)claims.get(CLAIM_PRIMARY_KEY));
	}

	public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaims(token);
		return claimsResolver.apply(claims);
	}

	@PostConstruct
	protected void init() {
		SECRET_KEY = Base64.getEncoder().encodeToString(SECRET_KEY.getBytes());
	}

	private String combineAuthorities(OAuth2User oAuth2User) {
		return oAuth2User.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining(COMMA));
	}

	private List<SimpleGrantedAuthority> getAuthorities(Claims claims) {
		return Arrays.stream(
				claims.get(CLAIM_AUTHORITIES).toString().split(COMMA))
			.map(SimpleGrantedAuthority::new)
			.collect(Collectors.toList());
	}

	private void validateAuthorizationHeader(String authorizationHeader) {
		if (authorizationHeader == null) {
			throw new InvalidRequestHeaderException(
				FieldError.of(AUTHORIZATION_HEADER, EMPTY, AUTHORIZATION_HEADER_MISSING));
		} else if (!authorizationHeader.startsWith(TOKEN_TYPE + SPACE)) {
			throw new InvalidRequestHeaderException(
				FieldError.of(AUTHORIZATION_HEADER, authorizationHeader, BEARER_PREFIX_MISSING));
		}
	}

	private Claims getAllClaims(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY))
			.build()
			.parseClaimsJws(token)
			.getBody();
	}

	public enum JwtType {

		ACCESS_TOKEN, REFRESH_TOKEN

	}

}