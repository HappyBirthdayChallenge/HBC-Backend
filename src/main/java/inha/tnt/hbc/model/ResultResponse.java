package inha.tnt.hbc.model;

import static inha.tnt.hbc.util.Constants.*;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
public class ResultResponse {

	@ApiModelProperty(value = "HTTP 상태 코드", example = "200")
	private final int status;
	@ApiModelProperty(value = "Business 상태 코드", example = "R-M001")
	private final String code;
	@ApiModelProperty(value = "응답 메세지", example = "사용 가능한 아이디입니다.")
	private final String message;
	@ApiModelProperty(value = "응답 데이터")
	private final Object data;

	public ResultResponse(ResultCode resultCode, Object data) {
		this.status = resultCode.getStatus();
		this.code = resultCode.getCode();
		this.message = resultCode.getMessage();
		this.data = data;
	}

	public static ResultResponse of(ResultCode resultCode, Object data) {
		return new ResultResponse(resultCode, data);
	}

	public static ResultResponse of(ResultCode resultCode) {
		return new ResultResponse(resultCode, EMPTY);
	}

}
