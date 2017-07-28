package com.hebta.plato.dao;

import java.util.List;

import com.hebta.plato.pojo.User;

public interface UserMapper {
    int insert(User record);
    
    User selectByPrimaryKey(Long id);
    
    User selectByLoginName(String loginName);
    
    List<User> selectAll();
    
    int batchDelete(List<Long> ids);
    
    int update(User record);
}