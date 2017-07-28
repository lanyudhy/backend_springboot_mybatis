package com.hebta.plato.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jasypt.util.password.BasicPasswordEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hebta.plato.dao.UserMapper;
import com.hebta.plato.pojo.User;
import com.hebta.plato.utilities.PlatoConstants;

@Service
public class UserService {
	private Logger logger = LoggerFactory.getLogger(UserService.class);
	
	@Autowired
	private UserMapper userDao;
	
	public List<User> getAllUsers(){
		logger.info("进入 getAllUsers() 方法");
		List<User> users = userDao.selectAll();
		User admin = null;
		for (User user : users){
			if (PlatoConstants.USER_ADMIN.equalsIgnoreCase(user.getLoginName())){
				admin = user;
			}
			user.setPassword(null);
		}
		if (admin == null){
			throw new RuntimeException("数据库里没有插入用户名为 admin 的管理员账号");
		}
		users.remove(admin);
		
		return users;
	}
	
	public User getUserById(Long id){
		logger.info("进入 getUserById(), id ::: " + id);
		User user = userDao.selectByPrimaryKey(id);
		user.setPassword(null);
		return user;
	}
	
	public User findDbUser(User user){
		User dbUser = userDao.selectByLoginName(user.getLoginName());
		if (dbUser == null){
			throw new RuntimeException("用户名或密码不正确");
		}
		BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
		Boolean isCorrectPassword = passwordEncryptor.checkPassword(user.getPassword(), dbUser.getPassword());
		if (isCorrectPassword){
			dbUser.setPassword(null);
			return dbUser;
		} else {
			throw new RuntimeException("用户名或密码不正确");
		}
	}
	
	public User upInsertUser(User user){
		logger.info("进入 upInsertUser() 方法");
		
		assert user == null : "参数 user 为空";		
		
		if (user.getId() == null){
			assert StringUtils.isEmpty(user.getPassword()) && user.getPassword().trim().length() == 0 : "密码不可以为空";			
			BasicPasswordEncryptor pwdEncyptor = new BasicPasswordEncryptor();
			user.setPassword(pwdEncyptor.encryptPassword(user.getPassword()));
			
			User dbUser = userDao.selectByLoginName(user.getLoginName());
			if (dbUser != null){
				throw new RuntimeException("用户名重复了，请更换！");
			}
			userDao.insert(user);
		} else {
			if (StringUtils.isNotEmpty(user.getPassword()) && user.getPassword().trim().length() > 0){
				BasicPasswordEncryptor pwdEncyptor = new BasicPasswordEncryptor();
				user.setPassword(pwdEncyptor.encryptPassword(user.getPassword()));
			}
			userDao.update(user);
		}
		User newOne = userDao.selectByLoginName(user.getLoginName());
		newOne.setPassword(null);
		return newOne;
	}
	
	public List<Long> batchDeleteUsers(String idsStr){
		assert idsStr == null || idsStr.length() == 0 : "没有传入要删除的用户ID";
		
		List<Long> ids = new ArrayList<>();
		for (String id : idsStr.split(",")){
			ids.add(Long.valueOf(id));
		}
		int del = userDao.batchDelete(ids);
		if (del > 0) {
			return ids;
		} else {
			throw new RuntimeException("批量删除用户失败");
		}
	}
}
