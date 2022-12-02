package inha.tnt.hbc.application.member.service;

import static inha.tnt.hbc.model.ResultCode.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import inha.tnt.hbc.domain.member.dto.MemberProfileDto;
import inha.tnt.hbc.domain.member.entity.Member;
import inha.tnt.hbc.domain.member.service.FCMTokenService;
import inha.tnt.hbc.domain.member.service.MemberService;
import inha.tnt.hbc.domain.member.service.RefreshTokenService;
import inha.tnt.hbc.domain.member.vo.BirthDate;
import inha.tnt.hbc.domain.room.entity.Room;
import inha.tnt.hbc.domain.room.service.RoomService;
import inha.tnt.hbc.model.ResultResponse;
import inha.tnt.hbc.model.member.dto.MyInfoResponse;
import inha.tnt.hbc.security.jwt.dto.JwtDto;
import inha.tnt.hbc.util.JwtUtils;
import inha.tnt.hbc.util.SecurityContextUtils;

@Service
@RequiredArgsConstructor
public class AccountService {

	private final SecurityContextUtils securityContextUtils;
	private final JwtUtils jwtUtils;
	private final RefreshTokenService refreshTokenService;
	private final FCMTokenService fcmTokenService;
	private final RoomService roomService;
	private final MemberService memberService;

	@Transactional
	public JwtDto setupMyBirthdayAndGenerateJwt(BirthDate birthDate) {
		final Member member = securityContextUtils.takeoutMember();
		member.setupBirthDate(birthDate);
		roomService.createRandomly(member);
		final String accessToken = jwtUtils.generateAccessToken(member);
		final String refreshToken = jwtUtils.generateRefreshToken(member);
		return JwtDto.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.build();
	}

	public ResultResponse signout(String fcmToken) {
		final Long memberId = securityContextUtils.takeoutMemberId();
		refreshTokenService.deleteRefreshToken(memberId);
		fcmTokenService.deleteFCMToken(memberId, fcmToken);
		return ResultResponse.of(SIGNOUT_SUCCESS);
	}

	public MyInfoResponse getMyInfo() {
		return MyInfoResponse.of(securityContextUtils.takeoutMember());
	}

	public MemberProfileDto getMemberProfile(Long targetMemberId) {
		final Long memberId = securityContextUtils.takeoutMemberId();
		final MemberProfileDto memberProfileDto = memberService.findMemberProfileDto(memberId, targetMemberId);
		final List<Room> rooms = roomService.findAllByMemberId(targetMemberId);
		memberProfileDto.setRooms(rooms);
		return memberProfileDto;
	}

}
