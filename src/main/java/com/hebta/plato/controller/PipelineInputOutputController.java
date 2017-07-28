package com.hebta.plato.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.hebta.plato.dto.FileNameToType;
import com.hebta.plato.dto.FileWithPagination;
import com.hebta.plato.dto.TaggedFileReturn;
import com.hebta.plato.pojo.Pipeline;
import com.hebta.plato.pojo.PipelineInput;
import com.hebta.plato.pojo.PipelineOutput;
import com.hebta.plato.pojo.Project;
import com.hebta.plato.service.ComponentService;
import com.hebta.plato.service.PipelineInputOutputService;
import com.hebta.plato.service.ProjectService;
import com.hebta.plato.utilities.BaseResponse;
import com.hebta.plato.utilities.BaseResponse.RESPONSE_STATUS;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("inputOutput")
public class PipelineInputOutputController {
	private Logger logger = LoggerFactory.getLogger(PipelineInputOutputController.class);
	
	@Value("${pipeline.output.file.path}")
	private String outputPath;
	
	@Autowired
	private PipelineInputOutputService inputOutputService;
	@Autowired
	private ComponentService compService;
	@Autowired
	private ProjectService projectService;
	
	@ApiOperation(value="获取pipeline中的输入文件", notes="")
	@RequestMapping(value="input/{pipelineId}/{page}/{countPerPage}", method=RequestMethod.GET)
	public BaseResponse<FileWithPagination> getPipelineInputWithPagination(@PathVariable Long pipelineId, @PathVariable Integer page, @PathVariable Integer countPerPage){
		logger.info("进入获取pipeline中的输入文件方法 PipelineInputOutputController.getPipelineInputWithPagination()");
		BaseResponse<FileWithPagination> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		FileWithPagination pagination = inputOutputService.getPipelineInputWithPagination(pipelineId, page, countPerPage);
		if (pagination == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("获取输入文件失败");
		} else {
			resp.setData(pagination);
			resp.setMsg("成功获取输入文件");
		}
		return resp;
	}
	
	@ApiOperation(value="获取pipeline中的输出文件", notes="")
	@RequestMapping(value="output/{pipelineId}/{page}/{countPerPage}", method=RequestMethod.GET)
	public BaseResponse<FileWithPagination> getPipelineOutputWithPagination(@PathVariable Long pipelineId, @PathVariable Integer page, @PathVariable Integer countPerPage){
		logger.info("进入获取pipeline中的输出文件方法 PipelineInputOutputController.getPipelineOutputWithPagination()");
		BaseResponse<FileWithPagination> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		FileWithPagination pagination = inputOutputService.getPipelineOutputWithPagination(pipelineId, page, countPerPage);
		if (pagination == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("获取输出文件失败");
		} else {
			resp.setData(pagination);
			resp.setMsg("成功获取输出文件");
		}
		return resp;
	}
	
	@ApiOperation(value="根据file id,获取该输入文件的内容及信息", notes="")
	@RequestMapping(value="input/{fileId}", method=RequestMethod.GET)
	public BaseResponse<PipelineInput> getPipelineInputByFileId(@PathVariable Long fileId){
		logger.info("进入根据file id,获取该输入文件的内容及信息方法 PipelineInputOutputController.getPipelineInputByFileId()");
		BaseResponse<PipelineInput> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		PipelineInput input = inputOutputService.getPipelineInputByFileId(fileId);
		if (input == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("获取输出文件失败");
		} else {
			resp.setData(input);
			resp.setMsg("成功获取输出文件");
		}
		return resp;
	}
	
	@ApiOperation(value="根据file id,获取该输出文件的内容及信息", notes="")
	@RequestMapping(value="output/{fileId}", method=RequestMethod.GET)
	public BaseResponse<TaggedFileReturn> getPipelineOutputByFileId(@PathVariable Long fileId){
		logger.info("进入根据file id,获取该输出文件的内容及信息方法 PipelineInputOutputController.getPipelineOutputByFileId()");
		BaseResponse<TaggedFileReturn> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		TaggedFileReturn output = inputOutputService.getPipelineOutputByFileId(fileId);
		if (output == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("获取输出文件失败");
		} else {
			resp.setData(output);
			resp.setMsg("成功获取输出文件");
		}
		return resp;
	}
	
	@ApiOperation(value="批量上传文件到Input文件夹", notes="")
	@RequestMapping(value="input/upload", method=RequestMethod.POST)
	public BaseResponse<FileWithPagination> uploadPipelineInputFiles(HttpServletRequest request){
		logger.info("进入批量上传文件到Input文件夹方法 PipelineInputOutputController.uploadPipelineInputFiles()");
		BaseResponse<FileWithPagination> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		FileWithPagination result = inputOutputService.uploadFiles(request);
		if (result == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("上传文件失败");
		} else {
			resp.setData(result);
			resp.setMsg("上传文件成功");
		}
		return resp;
	}
	
	@ApiOperation(value="把Output文件打成压缩包", notes="")
	@RequestMapping(value="output/zip", method=RequestMethod.POST)
	public BaseResponse<String> zipPipelineutputFiles(@RequestBody List<FileNameToType> downloadList){
		logger.info("进入把Output文件打成压缩包方法 PipelineInputOutputController.zipPipelineutputFiles()");
		BaseResponse<String> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		String zipPath = inputOutputService.generateDownloadPipelineoutputZip(downloadList);
		if (zipPath == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("压缩文件失败");
		} else {
			resp.setData(zipPath);
			resp.setMsg("压缩文件成功");
		}
		return resp;
	}
	
	@ApiOperation(value="把Output中所有文件打成压缩包", notes="")
	@RequestMapping(value="output/zip/{pipelineId}", method=RequestMethod.GET)
	public BaseResponse<String> zipPipelineutputFiles(@PathVariable Long pipelineId){
		logger.info("进入把Output中所有文件打成压缩包方法 PipelineInputOutputController.zipPipelineutputFiles()");
		BaseResponse<String> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		String zipPath = inputOutputService.generateAllPipelineoutputZip(pipelineId);
		if (zipPath == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("压缩文件失败");
		} else {
			resp.setData(zipPath);
			resp.setMsg("压缩文件成功");
		}
		return resp;
	}
	
	@ApiOperation(value="下载Output文件压缩包", notes="")
    @RequestMapping(value="downloadFiletest/{pipelineId}", method=RequestMethod.GET)
    public ResponseEntity<Object> download(HttpServletResponse response, @PathVariable Long pipelineId) throws IOException {
        String path = outputPath + "/zip/"+pipelineId+".zip";
        File file = new File(path);
        String fileName = new String((pipelineId+".zip").getBytes("UTF-8"), "iso-8859-1");
        long fileTotalsize = file.length();
        System.out.println("fileTotalsize = " + fileTotalsize);
        response.reset();
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-disposition", String.format("attachment; filename=\"%s\"", fileName));
        response.addHeader("Content-Length", String.valueOf(fileTotalsize));
        ServletOutputStream sos = response.getOutputStream();
        FileInputStream in = new FileInputStream(file);
        BufferedOutputStream outputStream = new BufferedOutputStream(sos);
        byte[] b = new byte[1024 * 2];//每次传输2K
        long filecomplateSize = 0L;
        double rateSpeed = 0;//速度
        long rateProcess = 0L;//进度
        int readSize = 0;
        int i = 0;
        int nbytes = 0;
        long startTime = System.nanoTime();
        //这里是下载进度和速度的简单实现 没有使用单独的线程进行速度计算
        while ((readSize = in.read(b)) > 0) {
            if (i % 20 == 0) {
                startTime = System.nanoTime();
            }
            filecomplateSize = filecomplateSize + readSize;
            outputStream.write(b, 0, readSize);
            nbytes = readSize + nbytes;
            if (i % 20 == 19) {
                rateProcess = filecomplateSize / fileTotalsize;
                //时间差  System.nanoTime()的返回值精确度是毫微秒
                double currentTime = (System.nanoTime() - startTime);
                //将速度转化成比较通用的MB/S
                rateSpeed = ((nbytes / currentTime) * 1000 * 1000 * 1000) / 1024 / 1024;
                String strrateSpeed = String.format("%.2f", rateSpeed);
                rateSpeed = Double.valueOf(strrateSpeed);
                System.out.println(rateSpeed);
                System.out.println(rateSpeed);
                nbytes = 0;
            }
            i++;
        }
        outputStream.flush();
        outputStream.close();
        sos.close();
        in.close();
        return new ResponseEntity<Object>(HttpStatus.OK);
    }
	
	@ApiOperation(value="删除Input文件", notes="")
	@RequestMapping(value="delete/input/{fileId}", method=RequestMethod.GET)
	public BaseResponse deletePipelineInputByFileId(@PathVariable Long fileId){
		logger.info("进入删除Input文件方法 PipelineInputOutputController.deletePipelineInputByFileId()");
		BaseResponse resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		int result = inputOutputService.deletePipelineInputByFileId(fileId);
		if (result <= 0) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("删除输入文件失败");
		} else {
			resp.setMsg("成功删除输入文件");
		}
		return resp;
	}
	
	@ApiOperation(value="删除Output文件", notes="")
	@RequestMapping(value="delete/output/{fileId}", method=RequestMethod.GET)
	public BaseResponse deletePipelineOutputByFileId(@PathVariable Long fileId){
		logger.info("进入删除Output文件方法 PipelineInputOutputController.deletePipelineOutputByFileId()");
		BaseResponse resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		int result = inputOutputService.deletePipelineOutputByFileId(fileId);
		if (result <= 0) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("删除输出文件失败");
		} else {
			resp.setMsg("成功删除输出文件");
		}
		return resp;
	}
	
	@ApiOperation(value="根据用户拖拽的处理器对训练文本进行处理", notes="")
	@RequestMapping(method = RequestMethod.POST)
	public BaseResponse<Integer> processPipeline(@RequestBody Pipeline pipeline) {
		logger.info("进入文本处理方法 PipelineInputOutputController.processPipeline()");
		BaseResponse<Integer> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		Project project = projectService.getProjectById(pipeline.getProjectId());
		if (project.getStatus() == null || project.getStatus() == 0) {
			project.setStatus(1);
			project = projectService.updateStatus(project);
			Integer result = compService.processPipeline(pipeline);
			if (result <= 0) {
				resp.setStatus(RESPONSE_STATUS.FAIL);
				resp.setMsg("处理训练文本失败");
			} else {
				resp.setMsg("成功处理训练文本");
			}
			project.setStatus(0);
			project = projectService.updateStatus(project);
		} else {
			resp.setMsg("训练文本正在处理中，请稍等");
		}
		return resp;
	}
}
