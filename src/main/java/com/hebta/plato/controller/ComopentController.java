package com.hebta.plato.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.hebta.plato.pojo.Component;
import com.hebta.plato.pojo.ComponentResource;
import com.hebta.plato.dto.ComponentDTO;
import com.hebta.plato.dto.FileWithPagination;
import com.hebta.plato.dto.PipelineDTO;
import com.hebta.plato.service.ComponentService;
import com.hebta.plato.utilities.BaseResponse;
import com.hebta.plato.utilities.BaseResponse.RESPONSE_STATUS;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("component")
public class ComopentController {
	private Logger logger = LoggerFactory.getLogger(ComopentController.class);
	
	@Autowired
	private ComponentService componentService;
	
	@ApiOperation(value="获取组件库中的所有组件", notes="")
	@RequestMapping(value="lib", method=RequestMethod.GET)
	public BaseResponse<List<ComponentDTO>> getComponentLib(){
		logger.info("进入获取组件库中的所有组件方法 ComopentController.getComponentLib()");
		BaseResponse<List<ComponentDTO>> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		List<ComponentDTO> list = componentService.getComponentLib();
		if (list == null || list.size() == 0) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("没有找到组件信息");
		} else {
			resp.setData(list);
			resp.setMsg("成功找到组件信息");
		}
		return resp;
	}
	
	@ApiOperation(value="根据pipeline id,获取当前pipeline已选择的组件", notes="")
	@RequestMapping(value="{pipelineId}", method=RequestMethod.GET)
	public BaseResponse<List<ComponentDTO>> getComponentDtoByPipelineId(@PathVariable Long pipelineId){
		logger.info("进入根据pipeline id,获取当前pipeline已选择的组件方法 ComopentController.getComponentDtoByPipelineId()");
		BaseResponse<List<ComponentDTO>> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		List<ComponentDTO> list = componentService.getComponentDtoByPipelineId(pipelineId, false);
		if (list == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("没有找到组件信息");
		} else {
			resp.setData(list);
			resp.setMsg("成功找到组件信息");
		}
		return resp;
	}
	
	@ApiOperation(value="根据pipeline id,获取组件库中的所有组件及当前pipeline已选择的组件", notes="lib为组件库中的组件，chosen为pipeline已选择的组件")
	@RequestMapping(value="chosenAndlib/{pipelineId}", method=RequestMethod.GET)
	public BaseResponse<Map<String, List<ComponentDTO>>> getChosenComponentAndLibByPipelineId(@PathVariable Long pipelineId){
		logger.info("进入根据pipeline id,获取组件库中的所有组件及当前pipeline已选择的组件方法 ComopentController.getChosenComponentAndLibByPipelineId()");
		BaseResponse<Map<String, List<ComponentDTO>>> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		Map<String, List<ComponentDTO>> map = componentService.getChosenComponentAndLibByPipelineId(pipelineId);
		if (map == null || map.size() == 0) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("没有找到组件信息");
		} else {
			resp.setData(map);
			resp.setMsg("成功找到组件信息");
		}
		return resp;
	}
	
	@ApiOperation(value="单个保存组件到pipeline中", notes="目前可能用不到")
	@RequestMapping(value="add/{pipelineId}/{componentId}", method=RequestMethod.GET)
	public BaseResponse<Component> addComponent(@PathVariable Long pipelineId, @PathVariable Long componentId){
		logger.info("进入单个保存组件到pipeline中方法 ComopentController.addComponent()");
		BaseResponse<Component> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		Component component = componentService.addComponent(pipelineId, componentId);
		if (component == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("新增组件失败");
		} else {
			resp.setData(component);
			resp.setMsg("成功新增组件");
		}
		return resp;
	}
	
	@ApiOperation(value="点击保存按钮，把所有的组件保存到pipeline中", notes="")
	@RequestMapping(value="batchAdd", method=RequestMethod.POST)
	public BaseResponse<PipelineDTO> batchAddComponent(@RequestBody PipelineDTO dto){
		logger.info("进入点击保存按钮，把所有的组件保存到pipeline中方法 ComopentController.batchAddComponent()");
		BaseResponse<PipelineDTO> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		PipelineDTO resultDto = componentService.batchAddComponent(dto);
		if (resultDto == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("新增组件失败");
		} else {
			resp.setData(resultDto);
			resp.setMsg("成功新增组件");
		}
		return resp;
	}
	
	@ApiOperation(value="根据ComponentId获取子文件", notes="")
	@RequestMapping(value="resource/{componentId}", method=RequestMethod.GET)
	public BaseResponse<List<ComponentResource>> getComponentResourceByComponentId(@PathVariable Long componentId){
		logger.info("进入根据ComponentId获取子文件方法 ComopentController.getComponentResourceByComponentId()");
		BaseResponse<List<ComponentResource>> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		List<ComponentResource> list = componentService.getComponentResourceByComponentId(componentId);
		if (list == null || list.size() == 0) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("没有找到组件子文件信息");
		} else {
			resp.setData(list);
			resp.setMsg("成功找到组件子文件信息");
		}
		return resp;
	}
	
	@ApiOperation(value="批量上传组件文件", notes="")
	@RequestMapping(value="upload", method=RequestMethod.POST)
	public BaseResponse<List<ComponentResource>> uploadFiles(HttpServletRequest request){
		logger.info("进入批量上传组件文件方法 ComopentController.uploadFiles()");
		BaseResponse<List<ComponentResource>> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		List<ComponentResource> result = componentService.uploadFiles(request);
		if (result == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("上传组件文件失败");
		} else {
			resp.setData(result);
			resp.setMsg("上传组件文件成功");
		}
		return resp;
	}
	
	@ApiOperation(value="删除组件文件", notes="")
	@RequestMapping(value="delete/resource/{resourceId}", method=RequestMethod.GET)
	public BaseResponse deleteComponentResourceByResourceId(@PathVariable Long resourceId){
		logger.info("进入删除组件文件方法 ComopentController.deleteComponentResourceByResourceId()");
		BaseResponse resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		int result = componentService.deleteComponentResourceByResourceId(resourceId);
		if (result <= 0) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("删除组件文件失败");
		} else {
			resp.setMsg("删除组件文件成功");
		}
		return resp;
	}
	
	@ApiOperation(value="获取组件文件内容", notes="")
	@RequestMapping(value="content/resource/{resourceId}", method=RequestMethod.GET)
	public BaseResponse<String> getComponentResourceContent(@PathVariable Long resourceId){
		logger.info("进入获取组件文件内容方法 ComopentController.getComponentResourceContent()");
		BaseResponse<String> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		String result = componentService.getComponentResourceContent(resourceId);
		if (result == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("获取组件文件内容失败");
		} else {
			resp.setData(result);
			resp.setMsg("获取组件文件内容成功");
		}
		return resp;
	}
	
	@ApiOperation(value="更新组件文件内容", notes="POST方法，只需要resourceId和contentStr两个参数")
	@RequestMapping(value="update/content/resource", method=RequestMethod.POST)
	public BaseResponse<String> updateComponentResourceContent(@RequestBody ComponentResource resource){
		logger.info("进入更新组件文件内容方法 ComopentController.updateComponentResourceContent()");
		BaseResponse<String> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		String result = componentService.updateComponentResourceContent(resource);
		if (result == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("更新组件文件内容失败");
		} else {
			resp.setData(result);
			resp.setMsg("更新组件文件内容成功");
		}
		return resp;
	}
	
	@ApiOperation(value="插入组件库文件内容", notes="")
	@RequestMapping(value="add/lib/resource", method=RequestMethod.POST)
	public BaseResponse<String> addComponentResource(@RequestBody ComponentResource resource){
		BaseResponse<String> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		componentService.addComponentResource(resource);
//		if (result == null) {
//			resp.setStatus(RESPONSE_STATUS.FAIL);
//			resp.setMsg("更新组件文件内容失败");
//		} else {
//			resp.setData(result);
//			resp.setMsg("更新组件文件内容成功");
//		}
		return resp;
	}
}
