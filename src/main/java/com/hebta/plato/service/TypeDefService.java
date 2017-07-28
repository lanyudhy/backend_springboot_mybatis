package com.hebta.plato.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hebta.plato.dao.TypeDefEntityMapper;
import com.hebta.plato.dao.TypeDefRelationMapper;
import com.hebta.plato.dto.TypeDefRelationArg;
import com.hebta.plato.dto.TypeDefXml;
import com.hebta.plato.pojo.TypeDefEntity;
import com.hebta.plato.pojo.TypeDefRelation;

@Service
public class TypeDefService {
	private Logger logger = LoggerFactory.getLogger(TypeDefService.class);
	
	@Autowired
	private TypeDefEntityMapper entityDao;
	@Autowired
	private TypeDefRelationMapper relationDao;
	
	@Transactional
	public TypeDefXml updateEntity(TypeDefEntity entity){
		logger.info("首先检索老的实体备用，然后更新实体");
		TypeDefEntity old = entityDao.selectById(entity.getId());
		entityDao.updateName(entity);
		
		Long projectId = entity.getProjectId();
		TypeDefXml xml = getTypeDefXml(projectId);
		for (TypeDefRelation relation : xml.getRelation_types()){
			boolean upFlag = false;			
			for (TypeDefRelationArg arg : relation.getArgs()){
				logger.info("如果从关系里删除这个实体为真，则说明关系里是包含这个实体的，需要更新名字");
				boolean drop = arg.getTargets().remove(old.getType());
				if (drop){
					upFlag = true;
					arg.getTargets().add(entity.getType());
				}	
			}	
			if (upFlag){
				relation.setArgs(relation.getArgs()); // 只有Set才会触发数据库字段的更新
				relationDao.update(relation);
			}
		}
		
		return getTypeDefXml(projectId);
	}
	
	@Transactional
	public TypeDefRelation updateRelation(TypeDefRelation relation){
		relationDao.update(relation);
		return relationDao.selectById(relation.getId());
	}
	
	@Transactional
	public TypeDefXml deleteEntity(Long id){
		logger.info("首先检索老的实体备用，然后删除实体");
		TypeDefEntity entity = entityDao.selectById(id);
		
		Long projectId = entity.getProjectId();
		TypeDefXml xml = getTypeDefXml(projectId);
		List<Long> relationIds = new ArrayList<>();
		
		for (TypeDefRelation relation : xml.getRelation_types()){
			boolean upFlag = false;
			boolean delFlag = false;
			for (TypeDefRelationArg arg : relation.getArgs()){
				logger.info("直接从关系里删除实体，即便没有也不影响结果");
				boolean drop = arg.getTargets().remove(entity.getType());
				if (drop){
					upFlag = true;
				}
				if (arg.getTargets().size() == 0){
					delFlag = true;
					break;
				}
			}
			if (upFlag && !delFlag){
				logger.info("删除一个实体后，关系里仍然有实体的需要更新");
				relation.setArgs(relation.getArgs()); // 只有Set才会触发数据库字段的更新
				relationDao.update(relation);
			}
			if (delFlag){
				Long relationId = relation.getId();
				logger.info("ID 为 {} 的关系的 from 或者 to 里面没有实体了，则整个关系就要被删除", relationId);
				relationIds.add(relationId);
			}
		}
		if (relationIds.size() > 0)
			logger.info("删除当前实体，被影响到的关系的ID有 {}", relationIds);
		
		entityDao.delete(id);
		if (relationIds.size() > 0){
			relationDao.batchDelete(relationIds);		
		}
		
		return getTypeDefXml(projectId);
	}
	
	public int deleteRelation(Long id){
		return relationDao.delete(id);
	}
	
	public TypeDefRelation getRelationById(Long id){
		return relationDao.selectById(id);
	}
	
	public TypeDefEntity getEntityById(Long id){
		return entityDao.selectById(id);
	}
	
	@Transactional
	public TypeDefEntity saveEntity(TypeDefEntity entity){
		entityDao.insertSelective(entity);
		return entityDao.selectById(entity.getId());
	}
	
	@Transactional
	public TypeDefRelation saveRelation(TypeDefRelation relation){
		relationDao.insert(relation);
		return relationDao.selectById(relation.getId());
	}
	
	@Transactional
	public TypeDefXml getTypeDefXml(Long projectId){
		TypeDefXml xml = new TypeDefXml();
		xml.setEntity_types(entityDao.selectByProjectId(projectId));
		xml.setRelation_types(relationDao.selectByProjectId(projectId));
		return xml;		
	}
}
