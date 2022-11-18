package inha.tnt.hbc.domain.message.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import inha.tnt.hbc.domain.message.entity.Decoration;
import inha.tnt.hbc.domain.message.entity.Message;
import inha.tnt.hbc.domain.message.entity.MessageDecorationTypes;
import inha.tnt.hbc.domain.message.repository.DecorationRepository;

@Service
@RequiredArgsConstructor
public class DecorationService {

	private final DecorationRepository decorationRepository;

	@Transactional
	public void save(Message message, MessageDecorationTypes type) {
		final Decoration decoration = Decoration.builder()
			.message(message)
			.type(type)
			.build();
		decorationRepository.save(decoration);
	}

}