package com.hebta.plato.dao;

import java.util.List;

import com.hebta.plato.pojo.TypeDefRelation;

public interface TypeDefRelationMapper {
    int insert(TypeDefRelation record);

    int update(TypeDefRelation record);
    
    int delete(Long id);
    
    int batchDelete(List<Long> ids);
    
    List<TypeDefRelation> selectByProjectId(Long projectId);
    
    TypeDefRelation selectById(Long id);
}