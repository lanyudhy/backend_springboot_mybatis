package com.hebta.plato.dao;

import java.util.List;

import com.hebta.plato.pojo.TypeDefEntity;

public interface TypeDefEntityMapper {
    int insertSelective(TypeDefEntity record);

    int updateName(TypeDefEntity record);
    
    int delete(Long id);
    
    List<TypeDefEntity> selectByProjectId(Long projectId);
    
    TypeDefEntity selectById(Long id);
}