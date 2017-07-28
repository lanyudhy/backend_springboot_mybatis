package com.hebta.plato.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.hebta.plato.dto.TypeDefXml;
import com.hebta.plato.pojo.TypeDefEntity;
import com.hebta.plato.pojo.TypeDefRelation;
import com.hebta.plato.service.TypeDefService;
import com.hebta.plato.utilities.BaseResponse;
import com.hebta.plato.utilities.BaseResponse.RESPONSE_STATUS;

import io.swagger.annotations.ApiOperation;

/**
 * TypeDef.xml 文件的操作
 * @author 雷兆金
 *
 */
@RestController
public class TypeDefController {
	private Logger logger = LoggerFactory.getLogger(TypeDefController.class);
	
	@Autowired
	private TypeDefService defService;

	@ApiOperation(value="更新一个实体", notes="")
	@RequestMapping(value = "entity/update", method = RequestMethod.POST)
	public BaseResponse<TypeDefXml> updateEntity(@RequestBody TypeDefEntity obj) {
		logger.info("进入更新实体方法 TypeDefController.updateEntity()");
		BaseResponse<TypeDefXml> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		TypeDefXml xml = defService.updateEntity(obj);
		if (xml == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("更新一个实体失败");
		} else {
			resp.setData(xml);
			resp.setMsg("成功更新一个实体");
		}
		return resp;
	}
	
	@ApiOperation(value="更新一个关系", notes="")
	@RequestMapping(value = "relation/update", method = RequestMethod.POST)
	public BaseResponse<TypeDefRelation> updateRelation(@RequestBody TypeDefRelation obj) {
		logger.info("进入更新关系方法 TypeDefController.updateRelation()");
		BaseResponse<TypeDefRelation> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		TypeDefRelation entity = defService.updateRelation(obj);
		if (entity == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("更新一个关系失败");
		} else {
			resp.setData(entity);
			resp.setMsg("成功更新一个关系");
		}
		return resp;
	}
	
	@ApiOperation(value="删除一个关系", notes="")
	@RequestMapping(value = "relation/{id}/deletion", method = RequestMethod.GET)
	public BaseResponse deleteRelation(@PathVariable Long id) {
		logger.info("进入删除关系方法 TypeDefController.deleteRelation()");
		BaseResponse resp = new BaseResponse(RESPONSE_STATUS.SUCCESS);
		defService.deleteRelation(id);		
		resp.setMsg("成功删除一个关系");
		
		return resp;
	}

	@ApiOperation(value="删除一个实体", notes="")
	@RequestMapping(value = "entity/{id}/deletion", method = RequestMethod.GET)
	public BaseResponse<TypeDefXml> deleteEntity(@PathVariable Long id) {
		logger.info("进入删除实体方法 TypeDefController.deleteEntity()");
		BaseResponse<TypeDefXml> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		TypeDefXml xml = defService.deleteEntity(id);	
		if (xml == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("删除实体失败");
		} else {
			resp.setData(xml);
			resp.setMsg("成功删除一个实体");
		}
		
		return resp;
	}

	@ApiOperation(value="查询一个实体", notes="")
	@RequestMapping(value = "entity/{id}", method = RequestMethod.GET)
	public BaseResponse<TypeDefEntity> getEntity(@PathVariable Long id) {
		logger.info("进入查询实体方法 TypeDefController.getEntity()");
		BaseResponse<TypeDefEntity> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		TypeDefEntity entity = defService.getEntityById(id);
		if (entity == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("没有找到实体");
		} else {
			resp.setData(entity);
			resp.setMsg("成功找到实体");
		}
		return resp;
	}

	@ApiOperation(value="查询一个关系", notes="")
	@RequestMapping(value = "relation/{id}", method = RequestMethod.GET)
	public BaseResponse<TypeDefRelation> getRelation(@PathVariable Long id) {
		logger.info("进入查询关系方法 TypeDefController.getRelation()");
		BaseResponse<TypeDefRelation> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		TypeDefRelation relation = defService.getRelationById(id);
		if (relation == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("没有找到关系");
		} else {
			resp.setData(relation);
			resp.setMsg("成功找到关系");
		}
		return resp;
	}

	@ApiOperation(value="保存一个实体", notes="")
	@RequestMapping(value = "entity", method = RequestMethod.POST)
	public BaseResponse<TypeDefEntity> saveEntity(@RequestBody TypeDefEntity obj) {
		logger.info("进入实体保存方法 TypeDefController.saveEntity()");
		BaseResponse<TypeDefEntity> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		TypeDefEntity entity = defService.saveEntity(obj);
		if (entity == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("保存实体失败");
		} else {
			resp.setData(entity);
			resp.setMsg("成功保存实体");
		}
		return resp;
	}

	@ApiOperation(value="保存一个关系", notes="")
	@RequestMapping(value = "relation", method = RequestMethod.POST)
	public BaseResponse<TypeDefRelation> saveRelation(@RequestBody TypeDefRelation obj) {
		logger.info("进入查询关系方法 TypeDefController.saveRelation()");
		BaseResponse<TypeDefRelation> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		TypeDefRelation relation = defService.saveRelation(obj);
		if (relation == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("保存关系失败");
		} else {
			resp.setData(relation);
			resp.setMsg("成功保存关系");
		}
		return resp;
	}
}
