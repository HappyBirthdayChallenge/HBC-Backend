package inha.tnt.hbc.domain.member.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import inha.tnt.hbc.domain.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findByUsername(String username);

	Optional<Member> findByPhone(String phone);

	Optional<Member> findByNameAndPhone(String name, String phone);

	Optional<Member> findByNameAndPhoneAndUsername(String name, String phone, String username);

	@Query("select distinct m from Member m left join fetch m.oAuth2Accounts")
	List<Member> findAllFetchOAuth2Accounts();

	@Query("select m from Member m where m.birthDate.month = :month and m.birthDate.date = :date")
	List<Member> findAllByBirthDay(int month, int date);

}
