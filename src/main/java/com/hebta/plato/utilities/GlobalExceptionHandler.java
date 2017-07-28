package com.hebta.plato.utilities;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.druid.util.StringUtils;
import com.hebta.plato.utilities.BaseResponse.RESPONSE_STATUS;

/**
 * 这个是全局的错误拦截器，包括ApplicationException都会被拦截到，由于Controller是程序的入口，
 * 所以用@ControllerAdvice注册。
 *
 */
@ControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
	@ResponseBody
    @ExceptionHandler(value = Throwable.class)
    public BaseResponse<String> defaultErrorHandler(HttpServletResponse response, Throwable ex) throws Exception {
		BaseResponse<String> resp = new BaseResponse<>(RESPONSE_STATUS.FAIL);
		String message = ex.getMessage();
		if (StringUtils.isEmpty(message)){
			message = "Unchecked Exception occurs like NullpointerException.";
		}
		resp.setMsg(message);
		logger.info("error occurs: ", ex);
		return resp;
    }
}