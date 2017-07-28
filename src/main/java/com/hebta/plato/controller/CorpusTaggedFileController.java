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
import com.hebta.plato.dto.MoveCorpusTaggedFiles;
import com.hebta.plato.dto.TaggedFileReturn;
import com.hebta.plato.pojo.CorpusModel;
import com.hebta.plato.pojo.CorpusTaggedFile;
import com.hebta.plato.pojo.Project;
import com.hebta.plato.service.CorpusTaggedFileService;
import com.hebta.plato.service.ProjectService;
import com.hebta.plato.utilities.BaseResponse;
import com.hebta.plato.utilities.BaseResponse.RESPONSE_STATUS;
import com.hebta.plato.utilities.FileUtil;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("tagged")
public class CorpusTaggedFileController {
	private Logger logger = LoggerFactory.getLogger(CorpusTaggedFileController.class);
	
	@Value("${corpus.download.path}")
	private String downloadPath;
	
	@Autowired
	private CorpusTaggedFileService taggedService;
	@Autowired
	private ProjectService projectService;
	
	@ApiOperation(value="添加被标注文件", notes="")
	@RequestMapping(value="add", method=RequestMethod.POST)
	public BaseResponse<CorpusTaggedFile> addCorpusTaggedFile(@RequestBody CorpusTaggedFile file){
		logger.info("进入添加被标注文件方法 CorpusTaggedFileController.addCorpusTaggedFile()");
		BaseResponse<CorpusTaggedFile> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		CorpusTaggedFile reuslt = taggedService.addCorpusTaggedFile(file);
		if (reuslt == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("添加被标注文件失败");
		} else {
			resp.setData(reuslt);
			resp.setMsg("成功添加被标注文件");
		}
		return resp;
	}
	
	@ApiOperation(value="根据Corpus id， 获取所有被标注的文件", notes="")
	@RequestMapping(value="taggedFile/list/{corpusId}/{folder}", method=RequestMethod.GET)
	public BaseResponse<List<CorpusTaggedFile>> getCorpusTaggedFileByCorpusId(@PathVariable Long corpusId, @PathVariable String folder){
		logger.info("进入根据Corpus id， 获取所有被标注的文件方法 CorpusTaggedFileController.getCorpusTaggedFileByCorpusId()");
		BaseResponse<List<CorpusTaggedFile>> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		List<CorpusTaggedFile> result = taggedService.getCorpusTaggedFileByCorpusId(corpusId, folder);
		if (result == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("获取被标注文件失败");
		} else {
			resp.setData(result);
			resp.setMsg("成功获取被标注文件");
		}
		return resp;
	}
	
	@ApiOperation(value="根据File id， 获取被标注的文件", notes="")
	@RequestMapping(value="taggedFile/{fileId}/{folder}", method=RequestMethod.GET)
	public BaseResponse<TaggedFileReturn> getCorpusTaggedFileByFileId(@PathVariable Long fileId, @PathVariable String folder){
		logger.info("进入根据File id， 获取被标注的文件方法 CorpusTaggedFileController.getCorpusTaggedFileByFileId()");
		BaseResponse<TaggedFileReturn> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		TaggedFileReturn result = taggedService.getCorpusTaggedFileByPrimaryKey(fileId);
		if (result == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("获取被标注文件失败");
		} else {
			resp.setData(result);
			resp.setMsg("成功获取被标注文件");
		}
		return resp;
	}
	
	@ApiOperation(value="根据Corpus id，删除所有被标注的文件", notes="")
	@RequestMapping(value="delete/list/{corpusId}/{folder}", method=RequestMethod.GET)
	public BaseResponse deleteCorpusTaggedFileByCorpusId(@PathVariable Long corpusId, @PathVariable String folder){
		logger.info("进入根据Corpus id， 删除所有被标注的文件方法 CorpusTaggedFileController.deleteCorpusTaggedFileByCorpusId()");
		BaseResponse resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		int result = taggedService.deleteCorpusTaggedFileByCorpusId(corpusId, folder);
		if (result < 1) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("删除被标注文件失败");
		} else {
			resp.setMsg("成功删除被标注文件");
		}
		return resp;
	}
	
	@ApiOperation(value="根据File id， 删除被标注的文件", notes="")
	@RequestMapping(value="delete/{fileId}/{folder}", method=RequestMethod.GET)
	public BaseResponse deleteCorpusTaggedFileByFileId(@PathVariable Long fileId, @PathVariable String folder){
		logger.info("进入根据File id，删除被标注的文件方法 CorpusTaggedFileController.deleteCorpusTaggedFileByFileId()");
		BaseResponse resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		int result = taggedService.deleteCorpusTaggedFileByFileId(fileId, folder);
		if (result < 1) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("删除被标注文件失败");
		} else {
			resp.setMsg("成功删除被标注文件");
		}
		return resp;
	}
	
	@ApiOperation(value="分页获取corpus中的被标注文件", notes="")
	@RequestMapping(value="tagged/{corpusId}/{page}/{countPerPage}/{folder}", method=RequestMethod.GET)
	public BaseResponse<FileWithPagination> getTaggedFileWithPagination(@PathVariable Long corpusId, @PathVariable Integer page, @PathVariable Integer countPerPage, @PathVariable String folder){
		logger.info("进入分页获取corpus中的被标注文件方法 CorpusTaggedFileController.getTaggedFileWithPagination()");
		BaseResponse<FileWithPagination> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		FileWithPagination pagination = taggedService.getTaggedFileWithPagination(corpusId, page, countPerPage, folder);
		if (pagination == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("获取输出文件失败");
		} else {
			resp.setData(pagination);
			resp.setMsg("成功获取输出文件");
		}
		return resp;
	}
	
	@ApiOperation(value="在Corpus中，把文件从一个文件夹复制或剪切到另一个文件夹", notes="参数fileIds指的是要移动的文件ID;参数action指的是动作，如copy,cut;参数from指的是从哪个文件夹中复制或剪切;参数to指的是把文件复制或剪切到哪个文件加")
	@RequestMapping(value="move", method=RequestMethod.POST)
	public BaseResponse moveCorpusTaggedFiles(@RequestBody MoveCorpusTaggedFiles moveFiles){
		logger.info("进入在Corpus中，把文件从一个文件夹复制或剪切到另一个文件夹方法 CorpusTaggedFileController.moveCorpusTaggedFiles()");
		BaseResponse resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		int result = taggedService.moveCorpusTaggedFiles(moveFiles);
		if (result < 1) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("获取被标注文件失败");
		} else {
			resp.setMsg("成功获取被标注文件");
		}
		return resp;
	}
	
	@ApiOperation(value="批量上传文件", notes="")
	@RequestMapping(value="upload", method=RequestMethod.POST)
	public BaseResponse<FileWithPagination> uploadFiles(HttpServletRequest request){
		logger.info("进入批量上传文件方法 CorpusTaggedFileController.uploadFiles()");
		BaseResponse<FileWithPagination> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		FileWithPagination result = taggedService.uploadFiles(request);
		if (result == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("上传文件失败");
		} else {
			resp.setMsg("上传文件成功");
		}
		return resp;
	}
	
	@ApiOperation(value="把文件打成压缩包", notes="")
	@RequestMapping(value="zip", method=RequestMethod.POST)
	public BaseResponse<String> zipFiles(@RequestBody List<Long> downloadList){
		logger.info("进入批量上传文件到Input文件夹方法 CorpusTaggedFileController.zipFiles()");
		BaseResponse<String> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		String zipPath = taggedService.generateDownloadFilesZip(downloadList);
		if (zipPath == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("压缩文件失败");
		} else {
			resp.setData(zipPath);
			resp.setMsg("压缩文件成功");
		}
		return resp;
	}
	
	@ApiOperation(value="把文件下的所有文件打成压缩包", notes="")
	@RequestMapping(value="zip/{folder}", method=RequestMethod.GET)
	public BaseResponse<String> zipFiles(@PathVariable String folder){
		logger.info("进入把文件下的所有文件打成压缩包方法 CorpusTaggedFileController.zipFiles()");
		BaseResponse<String> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		String zipPath = taggedService.generateAllFilesZip(folder);
		if (zipPath == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("压缩文件失败");
		} else {
			resp.setData(zipPath);
			resp.setMsg("压缩文件成功");
		}
		return resp;
	}
	
	@ApiOperation(value="下载文件压缩包", notes="")
    @RequestMapping(value="downloadFile/{corpusId}", method=RequestMethod.GET)
    public ResponseEntity<Object> download(HttpServletResponse response, @PathVariable Long corpusId) throws IOException {
        String path = downloadPath + "/zip/"+corpusId+".zip";
        File file = new File(path);
        String fileName = new String((corpusId+".zip").getBytes("UTF-8"), "iso-8859-1");
        long fileTotalsize = file.length();
        System.out.println("fileTotalsize = " + fileTotalsize);
        response.reset();
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-disposition", String.format("attachment; filename=\"%s\"", fileName));
        response.addHeader("Content-Length", String.valueOf(fileTotalsize));
        ServletOutputStream sos = response.getOutputStream();
        FileInputStream in = new FileInputStream(file);
        BufferedOutputStream outputStream = new BufferedOutputStream(sos);
        byte[] b = new byte[1024 * 20];//每次传输2K
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
	
	@ApiOperation(value="根据CorpusId获取所有Models", notes="")
    @RequestMapping(value="list/model/{corpusId}", method=RequestMethod.GET)
	public BaseResponse<List<CorpusModel>> getModelsByCorpusId(@PathVariable Long corpusId){
		logger.info("进入根据CorpusId获取所有Models方法 CorpusTaggedFileController.getModelsByCorpusId()");
		BaseResponse<List<CorpusModel>> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		List<CorpusModel> result = taggedService.getModelsByCorpusId(corpusId);
		if (result == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("获取Models失败");
		} else {
			resp.setData(result);
			resp.setMsg("获取Models成功");
		}
		return resp;
	}
	
	@ApiOperation(value="分页获取model中的文件", notes="")
	@RequestMapping(value="model/{modelId}/{page}/{countPerPage}", method=RequestMethod.GET)
	public BaseResponse<FileWithPagination> getModelFileWithPagination(@PathVariable Long modelId, @PathVariable Integer page, @PathVariable Integer countPerPage){
		logger.info("进入分页获取model中的文件方法 CorpusTaggedFileController.getModelFileWithPagination()");
		BaseResponse<FileWithPagination> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		FileWithPagination pagination = taggedService.getModelOutputWithPagination(modelId, page, countPerPage);
		if (pagination == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("获取输出文件失败");
		} else {
			resp.setData(pagination);
			resp.setMsg("成功获取输出文件");
		}
		return resp;
	}
	
	@ApiOperation(value="删除Model", notes="")
	@RequestMapping(value="model/delete/{modelId}", method=RequestMethod.GET)
	public BaseResponse deleteModel(@PathVariable Long modelId){
		logger.info("进入删除Model方法 CorpusTaggedFileController.deleteModel()");
		BaseResponse resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		int result = taggedService.deleteModel(modelId);
		if (result < 1) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("删除Model失败");
		} else {
			resp.setMsg("成功删除Model");
		}
		return resp;
	}
	
	@ApiOperation(value="删除Model中的Output文件", notes="")
	@RequestMapping(value="output/delete", method=RequestMethod.POST)
	public BaseResponse deleteModelOutputFiles(@RequestBody List<Long> fileIds){
		logger.info("进入删除Model中的Output文件方法 CorpusTaggedFileController.deleteModelOutputFiles()");
		BaseResponse resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		int result = taggedService.deleteModelOutputFiles(fileIds);
		if (result < 1) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("删除Model中Output文件失败");
		} else {
			resp.setMsg("成功删除Model中的Output文件");
		}
		return resp;
	}
	
	@ApiOperation(value="打开Model中的Output文件", notes="")
	@RequestMapping(value="output/{fileId}", method=RequestMethod.GET)
	public BaseResponse<TaggedFileReturn> getModelOutputFileByFileId(@PathVariable Long fileId){
		logger.info("进入打开Model中的Output文件方法 CorpusTaggedFileController.getModelOutputFileByFileId()");
		BaseResponse<TaggedFileReturn> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		TaggedFileReturn result = taggedService.getModelOutputFileByFileId(fileId);
		if (result == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("获取模型输出文件失败");
		} else {
			resp.setData(result);
			resp.setMsg("成功获取模型输出文件");
		}
		return resp;
	}
	
	@ApiOperation(value="新增Entity标注", notes="参数start表示entity的开始位置，end表示entity的结束位置")
	@RequestMapping(value="add/entity/{fileId}/{start}/{end}/{sem}", method=RequestMethod.GET)
	public BaseResponse<TaggedFileReturn> addEntity(@PathVariable Long fileId, @PathVariable Integer start, @PathVariable Integer end, @PathVariable String sem){
		logger.info("进入新增Entity标注方法 CorpusTaggedFileController.addEntity()");
		BaseResponse<TaggedFileReturn> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		TaggedFileReturn result = taggedService.addEntity(fileId, start, end, sem);
		if (result == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("新增Entity标注失败");
		} else {
			resp.setData(result);
			resp.setMsg("成功新增Entity标注");
		}
		return resp;
	}
	
	@ApiOperation(value="新增Relation标注", notes="参数start表示开始Entity的号码，如T0;end表示结束Entity的号码，如T1")
	@RequestMapping(value="add/relation/{fileId}/{start}/{end}/{sem}", method=RequestMethod.GET)
	public BaseResponse<TaggedFileReturn> addRelation(@PathVariable Long fileId, @PathVariable String start, @PathVariable String end, @PathVariable String sem){
		logger.info("进入新增Relation标注方法 CorpusTaggedFileController.addRelation()");
		BaseResponse<TaggedFileReturn> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		TaggedFileReturn result = taggedService.addRelation(fileId, start, end, sem);
		if (result == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("新增Relation标注失败");
		} else {
			resp.setData(result);
			resp.setMsg("成功新增Relation标注");
		}
		return resp;
	}
	
	@ApiOperation(value="删除Entity标注", notes="参数entityId表示entity的号码，如T0")
	@RequestMapping(value="delete/entity/{fileId}/{entityId}", method=RequestMethod.GET)
	public BaseResponse<TaggedFileReturn> deleteEntity(@PathVariable Long fileId, @PathVariable String entityId){
		logger.info("进入删除Entity标注方法 CorpusTaggedFileController.deleteEntity()");
		BaseResponse<TaggedFileReturn> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		TaggedFileReturn result = taggedService.deleteEntity(fileId, entityId);
		if (result == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("删除Entity标注失败");
		} else {
			resp.setData(result);
			resp.setMsg("成功删除Entity标注");
		}
		return resp;
	}
	
	@ApiOperation(value="删除Relation标注", notes="参数relationId表示relation的号码，如R0")
	@RequestMapping(value="delete/relation/{fileId}/{relationId}", method=RequestMethod.GET)
	public BaseResponse<TaggedFileReturn> deleteRelation(@PathVariable Long fileId, @PathVariable String relationId){
		logger.info("进入删除Relation标注方法 CorpusTaggedFileController.deleteRelation()");
		BaseResponse<TaggedFileReturn> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		TaggedFileReturn result = taggedService.deleteRelation(fileId, relationId);
		if (result == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("删除Relation标注失败");
		} else {
			resp.setData(result);
			resp.setMsg("成功删除Relation标注");
		}
		return resp;
	}
	
	@ApiOperation(value="更新Entity标注", notes="参数entityId表示entity的号码，如T0")
	@RequestMapping(value="update/entity/{fileId}/{entityId}/{newSem}", method=RequestMethod.GET)
	public BaseResponse<TaggedFileReturn> updateEntity(@PathVariable Long fileId, @PathVariable String entityId, @PathVariable String newSem){
		logger.info("进入更新Entity标注方法 CorpusTaggedFileController.updateEntity()");
		BaseResponse<TaggedFileReturn> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		TaggedFileReturn result = taggedService.updateEntity(fileId, entityId, newSem);
		if (result == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("更新Entity标注失败");
		} else {
			resp.setData(result);
			resp.setMsg("成功更新Entity标注");
		}
		return resp;
	}
	
	@ApiOperation(value="更新Relation标注", notes="参数relationId表示relation的号码，如R0")
	@RequestMapping(value="update/relation/{fileId}/{relationId}/{newSem}", method=RequestMethod.GET)
	public BaseResponse<TaggedFileReturn> updateRelation(@PathVariable Long fileId, @PathVariable String relationId, @PathVariable String newSem){
		logger.info("进入更新Relation标注方法 CorpusTaggedFileController.updateRelation()");
		BaseResponse<TaggedFileReturn> resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		TaggedFileReturn result = taggedService.updateRelation(fileId, relationId, newSem);
		if (result == null) {
			resp.setStatus(RESPONSE_STATUS.FAIL);
			resp.setMsg("更新Relation标注失败");
		} else {
			resp.setData(result);
			resp.setMsg("成功更新Relation标注");
		}
		return resp;
	}
	
	@ApiOperation(value="下载Jar文件", notes="")
    @RequestMapping(value="downloadJar/{modelId}", method=RequestMethod.GET)
    public ResponseEntity<Object> downloadJar(HttpServletResponse response, @PathVariable Long modelId) throws IOException {
		CorpusModel model = taggedService.getCorpusModelByModelId(modelId);
		String path = downloadPath + "/"+model.getCorpusId();
		FileUtil.createFolder(path);
		path = path+"/"+modelId;
		FileUtil.createFolder(path);
        File file = new File(path+"/model.jar");
        FileUtil.writeFileByBytes(path+"/model.jar", model.getJarFile());
        String fileName = new String(("model.jar").getBytes("UTF-8"), "UTF-8");
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
	
	@ApiOperation(value="训练模型", notes="")
	@RequestMapping(value="train/{corpusId}/{evaluation}", method=RequestMethod.GET)
	public BaseResponse trainModel(@PathVariable Long corpusId, @PathVariable Integer evaluation){
		logger.info("进入训练模型方法 CorpusTaggedFileController.trainModel()");
		BaseResponse resp = new BaseResponse<>(RESPONSE_STATUS.SUCCESS);
		Project project = projectService.getProjectById(corpusId);
		if (project.getStatus() == null || project.getStatus() == 0) {
			project.setStatus(1);
			project = projectService.updateStatus(project);
			taggedService.train(corpusId, evaluation);
			project.setStatus(0);
			project = projectService.updateStatus(project);
			resp.setMsg("成功训练模型");
		} else {
			resp.setMsg("模型正在训练中，请稍后再试。");
		}

		return resp;
	}
}
