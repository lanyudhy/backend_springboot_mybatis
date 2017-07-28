package com.hebta.plato.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.hebta.plato.pojo.Project;

public interface ProjectMapper {
    int insertSelective(Project record);
    
    List<Project> selectMyProjectsByType(@Param("userId") Long userId, @Param("type") Integer type);
    
    List<Project> selectSharedCorpusProjects(Long userId);
    
    List<Project> selectAllCorpusProjects(Long userId);
    
    List<Project> selectByIds(List<Long> ids);
    
    Project selectById(Long id);
    
    int updateName(Project p);
    
    int updateStatus(Project p);
    
    int batchDelete(List<Long> ids);
}