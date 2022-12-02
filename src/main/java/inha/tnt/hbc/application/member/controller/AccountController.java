package inha.tnt.hbc.application.member.controller;

import static inha.tnt.hbc.model.ResultCode.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import inha.tnt.hbc.application.member.service.AccountService;
import inha.tnt.hbc.domain.member.dto.MemberProfileDto;
import inha.tnt.hbc.domain.member.vo.BirthDate;
import inha.tnt.hbc.model.ResultResponse;
import inha.tnt.hbc.model.member.AccountApi;
import inha.tnt.hbc.model.member.dto.MyInfoResponse;
import inha.tnt.hbc.security.jwt.dto.JwtDto;

@RestController
@RequiredArgsConstructor
public class AccountController implements AccountApi {

	private final AccountService accountService;

	@Override
	public ResponseEntity<ResultResponse> setupBirthday(BirthDate birthDate) {
		final JwtDto jwtDto = accountService.setupMyBirthdayAndGenerateJwt(birthDate);
		return ResponseEntity.ok(ResultResponse.of(BIRTHDAY_SETUP_SUCCESS, jwtDto));
	}

	@Override
	public ResponseEntity<ResultResponse> signout(String fcmToken) {
		return ResponseEntity.ok(accountService.signout(fcmToken));
	}

	@Override
	public ResponseEntity<ResultResponse> getMyInfo() {
		final MyInfoResponse response = accountService.getMyInfo();
		return ResponseEntity.ok(ResultResponse.of(GET_MY_INFO_SUCCESS, response));
	}

	@Override
	public ResponseEntity<ResultResponse> getProfile(Long memberId) {
		final MemberProfileDto response = accountService.getMemberProfile(memberId);
		return ResponseEntity.ok(ResultResponse.of(GET_PROFILE_SUCCESS, response));
	}

}
