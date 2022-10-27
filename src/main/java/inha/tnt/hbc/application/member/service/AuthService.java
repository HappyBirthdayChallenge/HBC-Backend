package inha.tnt.hbc.application.member.service;

import static inha.tnt.hbc.domain.member.entity.MemberRoles.*;
import static inha.tnt.hbc.model.ErrorCode.*;
import static inha.tnt.hbc.model.ResultCode.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import inha.tnt.hbc.domain.member.entity.Member;
import inha.tnt.hbc.domain.member.service.MemberService;
import inha.tnt.hbc.exception.EntityNotFoundException;
import inha.tnt.hbc.model.ResultResponse;
import inha.tnt.hbc.security.jwt.dto.JwtDto;
import inha.tnt.hbc.util.JwtUtils;
import inha.tnt.hbc.vo.BirthDate;
import inha.tnt.hbc.vo.Image;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final MemberService memberService;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtils jwtUtils;

	@Transactional
	public Member signup(String username, String password, String name, String phone, BirthDate birthDate,
		Image image) {
		final Member member = Member.builder()
			.username(username)
			.password(passwordEncoder.encode(password))
			.name(name)
			.phone(phone)
			.birthDate(birthDate)
			.image(image)
			.authorities(ROLE_USER.name())
			.build();
		return memberService.save(member);
	}

	public JwtDto signin(String username, String password) {
		final Member member = memberService.findByUsername(username);
		if (!passwordEncoder.matches(password, member.getPassword())) {
			throw new EntityNotFoundException(MEMBER_UNFOUNDED);
		}
		return JwtDto.builder()
			.accessToken(jwtUtils.generateAccessToken(member))
			.refreshToken(jwtUtils.generateRefreshToken(member))
			.build();
	}

	public ResultResponse checkUsername(String username) {
		if (!memberService.isExistingUsername(username)) {
			return ResultResponse.of(USERNAME_AVAILABLE);
		} else {
			return ResultResponse.of(USERNAME_ALREADY_USED);
		}
	}

	public ResultResponse checkPhone(String phone) {
		if (!memberService.isExistingPhone(phone)) {
			return ResultResponse.of(PHONE_AVAILABLE);
		} else {
			return ResultResponse.of(PHONE_ALREADY_USED);
		}
	}

}
