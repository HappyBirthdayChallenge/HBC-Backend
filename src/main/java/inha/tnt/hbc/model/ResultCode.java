package inha.tnt.hbc.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

	;

	private final int status;
	private final String code;
	private final String message;

}
