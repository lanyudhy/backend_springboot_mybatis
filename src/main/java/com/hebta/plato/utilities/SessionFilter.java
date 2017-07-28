package com.hebta.plato.utilities;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.hebta.plato.utilities.BaseResponse.RESPONSE_STATUS;

//@Component
public class SessionFilter implements Filter {
	private Logger logger = LoggerFactory.getLogger(SessionFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		logger.info("启动会话过滤器");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse resp = (HttpServletResponse)response;
		String requestURI = req.getRequestURI();
		HttpSession session = req.getSession(false); // check exist Session associated
		
		String[] excludedUris = { "logout", "login", "user/addition", ".ico", ".gif", ".jpg", ".bmp",
				// 下面这行是 Swagger-UI 需要加载的资源！
				".html", ".js", ".css", ".png", "swagger", "v2/api-docs",
				"tagged","component","inputOutput","platoEndpoint"};
		List<String> exUriList = Arrays.asList(excludedUris);
		
		if ((session != null && session.getAttribute("USER") != null)
				|| exUriList.stream().anyMatch(requestURI::contains)) {
			chain.doFilter(req, resp);
		} else {
			logger.warn(requestURI + " 没有通过过滤 @ SessionFilter");
			
			BaseResponse result = new BaseResponse(RESPONSE_STATUS.FAIL);
			result.setMsg("No session or session time out, please login. @ SessionFilter");
			result.setCode("NO_SESSION");
			
			resp.getWriter().write(new Gson().toJson(result));
		}
	}

	@Override
	public void destroy() {
		logger.info("退出会话过滤器");
	}

}
