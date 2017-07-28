package com.hebta.plato.utilities;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.hebta.plato.pojo.User;

public class SessionUtil {
	public static User getSessionUser(){
		HttpServletRequest request = getRequest();
		User user = (User)request.getSession().getAttribute("USER");
		if (user == null){
			throw new RuntimeException("会话过期，请重新登录");
		}
		return user;
	}

	public static HttpServletRequest getRequest() {
		HttpServletRequest request = ((ServletRequestAttributes) 
				RequestContextHolder.getRequestAttributes()).getRequest();
		return request;
	}
	
	public static ServletContext getServletContext(){
		return getSession().getServletContext();
	}

	private static HttpSession getSession() {
		return getRequest().getSession();
	}
}
