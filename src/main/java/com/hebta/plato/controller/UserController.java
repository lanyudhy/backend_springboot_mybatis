package com.hebta.plato.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.hebta.plato.pojo.User;
import com.hebta.plato.service.UserService;
import com.hebta.plato.utilities.BaseResponse;
import com.hebta.plato.utilities.BaseResponse.RESPONSE_STATUS;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("user")
public class UserController {
	private Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserService userService;

	@ApiOperation(value="获取用户信息列表", notes="")
	@RequestMapping(value = "list", method = RequestMethod.GET)
	public BaseResponse<List<User>> getAllUsers() {
		logger.info("进入获取所有用户方法 UserController.getAllUsers()");
		BaseResponse<List<User>> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		List<User> users = userService.getAllUsers();
		if (users == null || users.size() == 0) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("没有找到用户信息");
		} else {
			resp.setData(users);
			resp.setMsg("成功找到用户信息");
		}
		return resp;
	}

	@ApiOperation(value="创建用户", notes="用户创建请求不会被拦截")
	@RequestMapping(value = "addition", method = RequestMethod.POST)
	public BaseResponse<User> saveUser(@RequestBody User user) {
		logger.info("进入用户信息保存方法 UserController.saveUser()");
		BaseResponse<User> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		User insert = userService.upInsertUser(user);
		if (insert == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("保存用户信息失败");
		} else {
			resp.setData(insert);
			resp.setMsg("成功保存用户信息");
		}
		return resp;
	}

	@ApiOperation(value="更新用户信息", notes="")
	@RequestMapping(value = "update", method = RequestMethod.POST)
	public BaseResponse<User> updateUser(@RequestBody User user) {
		logger.info("进入更新用户方法UserController.updateUser()");
		BaseResponse<User> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		User update = userService.upInsertUser(user);
		if (update == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("更新用户信息失败");
		} else {
			resp.setData(update);
			resp.setMsg("成功更新用户信息");
		}
		return resp;
	}

	@ApiOperation(value="删除用户", notes="根据提供的用户ID列表批量删除用户")
	@RequestMapping(value = "{idsStr}/deletion", method = RequestMethod.GET)
	public BaseResponse<List<Long>> deleteUsers(@PathVariable String idsStr) {
		logger.info("进入批量删除方法 UserController.deleteUsers()");
		BaseResponse<List<Long>> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		List<Long> ids = userService.batchDeleteUsers(idsStr);
		if (ids == null || ids.size() == 0) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("批量删除用户信息失败");
		} else {
			resp.setData(ids);
			resp.setMsg("成功批量删除用户信息");
		}
		return resp;
	}

	@ApiOperation(value="查询用户", notes="根据提供的用户ID查询单个用户")
	@RequestMapping(value = "{id}/profile", method = RequestMethod.GET)
	public BaseResponse<User> getUser(@PathVariable Long id) {
		logger.info("进入登录方法 UserController.getUser()");
		BaseResponse<User> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		User user = userService.getUserById(id);
		if (user == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("根据ID获取用户信息失败");
		} else {
			resp.setData(user);
			resp.setMsg("成功根据ID获取用户信息");
		}
		return resp;
	}

	@ApiOperation(value="登录", notes="")
	@RequestMapping(value = "login", method = RequestMethod.POST)
	public BaseResponse<User> login(@RequestBody User user, HttpServletRequest request) {
		logger.info("进入登录方法 UserController.login()");
		BaseResponse<User> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		User dbUser = userService.findDbUser(user);

		if (dbUser == null) {
			resp.setCode("E00001");
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("用户名或密码错误.");
		} else {
			dbUser.setPassword(null);
			resp.setData(dbUser);
			resp.setMsg("用户存在.");

			request.getSession().setAttribute("USER", dbUser);

		}
		return resp;
	}

	@ApiOperation(value="退出系统", notes="")
	@RequestMapping(value = "logout", method = RequestMethod.GET)
	public BaseResponse<User> logout(HttpServletRequest request) {
		logger.info("进入 UserController.logout()，退出系统");
		BaseResponse<User> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		request.getSession().invalidate();
		resp.setMsg("成功登出系统");
		return resp;
	}
}
