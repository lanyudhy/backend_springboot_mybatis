package com.hebta.plato.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.hebta.plato.dto.TypeDefXml;
import com.hebta.plato.pojo.Project;
import com.hebta.plato.service.ProjectService;
import com.hebta.plato.service.TypeDefService;
import com.hebta.plato.utilities.BaseResponse;
import com.hebta.plato.utilities.BaseResponse.RESPONSE_STATUS;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("project")
public class ProjectController {
	private Logger logger = LoggerFactory.getLogger(ProjectController.class);

	@Autowired
	private ProjectService projectService;
	@Autowired
	private TypeDefService typeDefService;
	
	@ApiOperation(value="获取指定类型的项目", notes="不包括被分享的项目, type的取值:1, Corpus; 2, Pipeline")
	@RequestMapping(value = "{type}/owned", method = RequestMethod.GET)
	public BaseResponse<List<Project>> getOwnedProjects(@PathVariable Integer type) {
		logger.info("进入获取指定类型的项目方法 ProjectController.getOwnedProjects()");
		BaseResponse<List<Project>> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		List<Project> projects = projectService.getMyProjectsByType(type);
		if (projects == null || projects.size() == 0) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("没有找到指定类型的项目");
		} else {
			resp.setData(projects);
			resp.setMsg("成功找到指定类型的项目");
		}
		return resp;
	}

	@ApiOperation(value="获取被分享的项目", notes="只查找被分享的Corpus项目")
	@RequestMapping(value = "shared", method = RequestMethod.GET)
	public BaseResponse<List<Project>> getSharedProjects() {
		logger.info("进入获取被分享项目的方法 ProjectController.getSharedProjects()");
		BaseResponse<List<Project>> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		List<Project> projects = projectService.getSharedCorpusProjects();
		if (projects == null || projects.size() == 0) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("没有找到被分享项目");
		} else {
			resp.setData(projects);
			resp.setMsg("成功找到被分享项目");
		}
		return resp;
	}
	
	@ApiOperation(value="获取所有的Corpus项目", notes="查找自己创建的和被分享的Corpus项目")
	@RequestMapping(value = "corpuses", method = RequestMethod.GET)
	public BaseResponse<List<Project>> getAllCorpusProjects() {
		logger.info("进入获取被分享项目的方法 ProjectController.getAllCorpusProjects()");
		BaseResponse<List<Project>> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		List<Project> projects = projectService.getAllCorpusProjects();
		if (projects == null || projects.size() == 0) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("没有找到所有的Corpus项目");
		} else {
			resp.setData(projects);
			resp.setMsg("成功找到所有的Corpus项目");
		}
		return resp;
	}
	
	@ApiOperation(value="获取typeDef.xml内容", notes="根据提供的项目ID查询文件内容")
	@RequestMapping(value = "{id}/typeDef", method = RequestMethod.GET)
	public BaseResponse<TypeDefXml> getTypeDefXml(@PathVariable Long id) {
		logger.info("进入登录方法 ProjectController.getTypeDefXml()");
		BaseResponse<TypeDefXml> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		TypeDefXml content = typeDefService.getTypeDefXml(id);
		if (content == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("根据ID获取typeDef.xml内容失败");
		} else {
			resp.setData(content);
			resp.setMsg("成功根据ID获取typeDef.xml内容");
		}
		return resp;
	}

	@ApiOperation(value="创建项目", notes="pairType的值：[1] Corpus; [2], Pipeline; [1,2], Both;")
	@RequestMapping(method = RequestMethod.POST)
	public BaseResponse<List<Project>> saveProject(@RequestBody Project project) {
		logger.info("进入项目保存方法 ProjectController.saveProject()");
		BaseResponse<List<Project>> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		List<Project> projects = projectService.saveProject(project);
		if (projects == null || projects.size() == 0) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("保存项目失败");
		} else {
			resp.setData(projects);
			resp.setMsg("成功保存项目");
		}
		return resp;
	}

	@ApiOperation(value="更新项目的名字", notes="")
	@RequestMapping(value = "update", method = RequestMethod.POST)
	public BaseResponse<Project> updateName(@RequestBody Project project) {
		logger.info("进入更新项目名字的方法ProjectController.updateName()");
		BaseResponse<Project> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		Project update = projectService.updateName(project);
		if (update == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("更新项目的名字失败");
		} else {
			resp.setData(update);
			resp.setMsg("成功更新项目的名字");
		}
		return resp;
	}

	@ApiOperation(value="批量删除项目", notes="根据提供的项目ID列表批量删除")
	@RequestMapping(value = "{idsStr}/deletion", method = RequestMethod.GET)
	public BaseResponse<List<Long>> deleteProjects(@PathVariable String idsStr) {
		logger.info("进入批量删除方法 ProjectController.deleteProjects()");
		BaseResponse<List<Long>> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		List<Long> ids = projectService.batchDelete(idsStr);
		if (ids == null || ids.size() == 0) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("批量删除项目失败");
		} else {
			resp.setData(ids);
			resp.setMsg("成功批量删除项目");
		}
		return resp;
	}
	
	@ApiOperation(value="分享项目", notes="只能分享Corpus项目")
	@RequestMapping(value = "{projectId}/user/{userIds}", method = RequestMethod.GET)
	public BaseResponse shareProject(@PathVariable Long projectId, @PathVariable String userIds) {
		logger.info("进入分享项目方法 ProjectController.shareProject()");
		BaseResponse resp = new BaseResponse(RESPONSE_STATUS.SUCCESS);
		int share = projectService.shareProject(projectId, userIds); 
		if (share <= 0) { 
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("分享项目失败");
		} else {
			resp.setMsg("成功分享项目");
		}
		return resp;
	}
}
