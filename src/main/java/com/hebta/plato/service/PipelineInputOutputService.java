package com.hebta.plato.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.uima.UIMAException;
import org.apache.uima.jcas.JCas;
import org.plato.nlp.structure.BratFileJson;
import org.plato.nlp.structure.BratSemJson;
import org.plato.nlp.structure.Document;
import org.plato.nlp.structure.PlatoNamedEntity;
import org.plato.nlp.structure.PlatoRelation;
import org.plato.nlp.structure.XmiUtil;
import org.plato.nlp.util.HtmlColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.alibaba.druid.util.StringUtils;
import com.google.gson.Gson;
import com.hebta.plato.dao.PipelineInputOutputMapper;
import com.hebta.plato.dto.ComponentDTO;
import com.hebta.plato.dto.FileNameToType;
import com.hebta.plato.dto.FileWithPagination;
import com.hebta.plato.dto.PieChart;
import com.hebta.plato.dto.PieChartElement;
import com.hebta.plato.dto.TaggedFileReturn;
import com.hebta.plato.dto.TypeDefXml;
import com.hebta.plato.pojo.CorpusTaggedFile;
import com.hebta.plato.pojo.PipelineInput;
import com.hebta.plato.pojo.PipelineOutput;
import com.hebta.plato.pojo.User;
import com.hebta.plato.utilities.FileUtil;
import com.hebta.plato.utilities.SessionUtil;
import com.hebta.plato.utilities.ZipUtil;

@Service
public class PipelineInputOutputService {
	private Logger logger = LoggerFactory.getLogger(PipelineInputOutputService.class);
	
	@Value("${pipeline.output.file.path}")
	private String outputPath;
	
	@Value("${pipeline.input.file.path}")
	private String inputPath;
	
	@Autowired
	private PipelineInputOutputMapper inputOutputDao;
	
	public FileWithPagination getPipelineInputWithPagination(Long pipelineId, Integer page, Integer countPerPage) {
		FileWithPagination pagination = new FileWithPagination();
		List<FileNameToType> fileFullNameList =  inputOutputDao.selectPipelineInputWithPagination(pipelineId, page, countPerPage);
		Integer totalCount = inputOutputDao.selectTotalCountInputByPipelineId(pipelineId);
		Integer totalPage = totalCount/countPerPage;
		if (totalCount%countPerPage != 0) {
			totalPage = totalPage + 1;
		}
		pagination.setFileFullNameList(fileFullNameList);
		pagination.setCurrentPage(page);
		pagination.setTotalPage(totalPage);
		
		return pagination;
	}
	
	public FileWithPagination getPipelineOutputWithPagination(Long pipelineId, Integer page, Integer countPerPage) {
		countPerPage = countPerPage/2;
		FileWithPagination pagination = new FileWithPagination();
		List<FileNameToType> fileFullNameList =  inputOutputDao.selectPipelineOutputWithPagination(pipelineId, page, countPerPage);
		Integer totalCount = inputOutputDao.selectTotalCountOutputByPipelineId(pipelineId);
		Integer totalPage = totalCount/countPerPage;
		if (totalCount%countPerPage != 0) {
			totalPage = totalPage + 1;
		}
		pagination.setFileFullNameList(fileFullNameList);
		pagination.setCurrentPage(page);
		pagination.setTotalPage(totalPage);
		
		return pagination;
	}
	
	public PipelineInput getPipelineInputByFileId(Long fileId) {
		return inputOutputDao.selectPipelineInputByPrimaryKey(fileId);
	}
	
	public TaggedFileReturn getPipelineOutputByFileId(Long fileId) {
		JCas aJCas = getJCas(fileId);
		return loadXmi(fileId, aJCas);
	}
	
	public void uploadPipelineInputFiles(Long pipelineId, HttpServletRequest request){
		
	}
	
	public String generateDownloadPipelineoutputZip(List<FileNameToType> downloadList){
		List<Long> txtFileIds = new ArrayList<>();
		List<Long> xmiFileIds = new ArrayList<>();
		List<PipelineOutput> txtFileList = new ArrayList<>();
		List<PipelineOutput> xmiFileList = new ArrayList<>();
		if (downloadList != null && downloadList.size() > 0) {
			for (FileNameToType file : downloadList) {
				if ("txt".equals(file.getType())) {
					txtFileIds.add(file.getFileId());
				} else if ("xmi".equals(file.getType())) {
					xmiFileIds.add(file.getFileId());
				}
			}
			if (txtFileIds != null && txtFileIds.size() > 0) {
				txtFileList = getOutputFilesByFileIds(txtFileIds);
			}
			if (xmiFileIds != null && xmiFileIds.size() > 0) {
				xmiFileList = getOutputFilesByFileIds(xmiFileIds);
			}
			String filePath = null;
			String zipPath = null;
			if (txtFileList != null && txtFileList.size() > 0) {
				filePath = outputPath + "/" + txtFileList.get(0).getPipelineId();
				zipPath = outputPath + "/zip";
				FileUtil.createFolder(filePath);
				FileUtil.createFolder(zipPath);
				zipPath = zipPath + "/" + txtFileList.get(0).getPipelineId() + ".zip";
				for (PipelineOutput output : txtFileList) {
					Boolean bool = FileUtil.createFile(filePath + "/" + output.getFileName() + ".txt", output.getTxtContent());
					if (!bool) {
						logger.info(output.getFileName() + ".txt 导出失败。");
					}
				}
			}
			if (xmiFileList != null && xmiFileList.size() > 0) {
				filePath = outputPath + "/" + xmiFileList.get(0).getPipelineId();
				zipPath = outputPath + "/zip";
				FileUtil.createFolder(filePath);
				FileUtil.createFolder(zipPath);
				zipPath = zipPath + "/" + xmiFileList.get(0).getPipelineId() + ".zip";
				for (PipelineOutput output : xmiFileList) {
					Boolean bool = FileUtil.createFile(filePath + "/" + output.getFileName() + ".xmi", output.getXmiContent());
					if (!bool) {
						logger.info(output.getFileName() + ".xmi 导出失败。");
					}
				}
			}
			
			if (!StringUtils.isEmpty(filePath)){
				ZipUtil.ZipMultiFile(filePath,zipPath);
			}
			
			return zipPath;
		} else {
			throw new RuntimeException("请至少选择一个文件。");
		}
	}
	
	public String generateAllPipelineoutputZip(Long pipelineId){
		List<PipelineOutput> fileList = inputOutputDao.selectPipelineOutputByPipeLineId(pipelineId);

		String filePath = null;
		String zipPath = null;
		if (fileList != null && fileList.size() > 0) {
			filePath = outputPath + "/" + fileList.get(0).getPipelineId();
			zipPath = outputPath + "/zip";
			FileUtil.createFolder(filePath);
			FileUtil.createFolder(zipPath);
			zipPath = zipPath + "/" + fileList.get(0).getPipelineId() + ".zip";
			for (PipelineOutput output : fileList) {
				if (output.getTxtContent() != null) {
					Boolean bool = FileUtil.createFile(filePath + "/" + output.getFileName() + ".txt", output.getTxtContent());
					if (!bool) {
						logger.info(output.getFileName() + ".txt 导出失败。");
					}
				}
				if (output.getXmiContent() != null) {
					Boolean bool = FileUtil.createFile(filePath + "/" + output.getFileName() + ".xmi", output.getXmiContent());
					if (!bool) {
						logger.info(output.getFileName() + ".xmi 导出失败。");
					}
				}
			}
		}
		
		if (!StringUtils.isEmpty(filePath)){
			ZipUtil.ZipMultiFile(filePath,zipPath);
		}
		
		return zipPath;
	}
	public List<PipelineOutput> getOutputFilesByFileIds(List<Long> fileIds) {
		return inputOutputDao.selectOutputFileByFileIds(fileIds);
	}
	
	public FileWithPagination uploadFiles(HttpServletRequest request) {
		User user = SessionUtil.getSessionUser();
//		User user = new User();
//		user.setId(1L);
		List<MultipartFile> files = ((MultipartHttpServletRequest)request).getFiles("file");  
		String type = ((MultipartHttpServletRequest)request).getParameter("type");
		Long id = Long.valueOf(((MultipartHttpServletRequest)request).getParameter("id"));
		for (int i =0; i< files.size(); ++i) {  
            MultipartFile file = files.get(i);  
            String name = file.getOriginalFilename();
            if (!file.isEmpty()) {  
                try {  
                    byte[] bytes = file.getBytes();  
                    if (name.toLowerCase().endsWith("zip")) {
                    	String path = inputPath + "/" + id + "/" + name;
                    	FileUtil.createFolder(inputPath + "/" + id);
                    	BufferedOutputStream stream =  
                                new BufferedOutputStream(new FileOutputStream(new File(path)));  
                        stream.write(bytes);  
                        stream.close(); 
                    	Map<String, String> map = ZipUtil.ZipContraMultiFile(path);
                    	FileUtil.delFile(path);
                    	for (Map.Entry<String, String> entry : map.entrySet()) {
                    		String fileName = entry.getKey().substring(0, entry.getKey().lastIndexOf("."));
                    		String content = entry.getValue();
    	                    PipelineInput input = new PipelineInput();
    	                    input.setPipelineId(id);
    	                    input.setFileType("txt");
    	                    input.setFileName(fileName);
    	                    input.setFileContent(content);
    	                    input.setEncoder("UTF-8");
    	                    input.setCreatedTime(new Date());
    	                    input.setCreatedBy(String.valueOf(user.getId()));
    	                    
    	                    inputOutputDao.insertPipelineInput(input);
                		}
                    	
                    } else if (name.toLowerCase().endsWith("txt")) {
                    	String fileName = name.substring(0, name.lastIndexOf("."));
	                    String content = new String(bytes);
	                    PipelineInput input = new PipelineInput();
	                    input.setPipelineId(id);
	                    input.setFileType("txt");
	                    input.setFileName(fileName);
	                    input.setFileContent(content);
	                    input.setEncoder("UTF-8");
	                    input.setCreatedTime(new Date());
	                    input.setCreatedBy(String.valueOf(user.getId()));
	                    
	                    inputOutputDao.insertPipelineInput(input);
                    }
                } catch (Exception e) {  
                    logger.info("You failed to upload " + name + " => " + e.getMessage());  
                    throw new RuntimeException("上传失败。");
                }  
            } else {  
            	logger.info("You failed to upload " + name + " because the file was empty.");  
            	throw new RuntimeException("上传失败，因为文件为空。");
            }  
        }  
		
		
		return getPipelineOutputWithPagination(id, 1, 20);
	}
	
	private File multipartToFile(MultipartFile multfile) throws IOException {  
        CommonsMultipartFile cf = (CommonsMultipartFile)multfile;   
        //这个myfile是MultipartFile的  
        DiskFileItem fi = (DiskFileItem) cf.getFileItem();  
        File file = fi.getStoreLocation();  
        //手动创建临时文件  
        if(file.length() < 2048){  
            File tmpFile = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") +   
                    file.getName());  
            multfile.transferTo(tmpFile);  
            return tmpFile;  
        }  
        return file;  
    }

	public int deletePipelineInputByFileId(Long fileId){
		return inputOutputDao.deletePipelineInputByFileId(fileId);
	}
	
	public int deletePipelineOutputByFileId(Long fileId){
		return inputOutputDao.deletePipelineOutputByFileId(fileId);
	}
	
	public JCas getJCas(Long fileId) {
		PipelineOutput file = inputOutputDao.selectPipelineOutputByPrimaryKey(fileId);
		FileUtil.createFolder(outputPath+"/"+file.getPipelineId());
		String filePath = outputPath+"/"+file.getPipelineId()+"/"+file.getFileName()+".xmi";
		FileUtil.createFile(filePath, file.getXmiContent());
		try {
			return XmiUtil.loadXmi(filePath);
		} catch (UIMAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public TaggedFileReturn loadXmi(Long fileId, JCas aJCas) {
		PipelineOutput output = inputOutputDao.selectPipelineOutputByPrimaryKey(fileId);
		TaggedFileReturn result = new TaggedFileReturn();
			
		BratFileJson bratFile = new BratFileJson();
		bratFile.setContent(aJCas.getDocumentText());
		BratSemJson bratSem = new BratSemJson();
		
		BratFileJson tokenFileJson = new BratFileJson();
		bratFile.setContent(aJCas.getDocumentText());
		BratSemJson tokenSemJson = new BratSemJson();
		
		BratFileJson sentFileJson = new BratFileJson();
		bratFile.setContent(aJCas.getDocumentText());
		BratSemJson sentSemJson = new BratSemJson();
		Document document = new Document(aJCas);

		Vector<PlatoNamedEntity> neList = document.getNameEntity();
		Map<String, String> map = new HashMap<>();
		Map<String, String> colorMap = new HashMap<>();
		Map<String, String> pieCharts = new HashMap<>();
		if (neList != null && neList.size() > 0) {
			Map<String, Integer> neMap = new HashMap<>();
			for (int i = 0; i < neList.size(); i++) {
				PlatoNamedEntity namedEntity = neList.get(i);
				String color = HtmlColor.getRandomCorlor();
				if (colorMap.containsKey(namedEntity.getSemanticTag())) {
					color = colorMap.get(namedEntity.getSemanticTag());
				} else {
					if (!namedEntity.getSemanticTag().equals("S") && !namedEntity.getSemanticTag().equals("E")) {
						color = HtmlColor.getCorlor(colorMap.size());
						colorMap.put(namedEntity.getSemanticTag(), color);
					} else {
						if (!colorMap.containsKey("S") && !colorMap.containsKey("E")) {
							color = HtmlColor.getCorlor(colorMap.size());
							colorMap.put("S", color);
							colorMap.put("E", color);
						} else {
							color = colorMap.get(namedEntity.getSemanticTag());
						}
					}
				}
				
				if (namedEntity.getSemanticTag().equals("Tk")) {
					tokenFileJson.addEntity( "T"+i, namedEntity.getBegin(), namedEntity.getEnd(), namedEntity.textStr(), namedEntity.getSemanticTag());
					
					map.put(namedEntity.getKey(),"T"+i);
					Vector<String> alias = new Vector<String>();
					alias.add(namedEntity.getSemanticTag());
	
					tokenSemJson.addEntity(namedEntity.getSemanticTag(), alias, color );
				} else if (namedEntity.getSemanticTag().equals("S") || namedEntity.getSemanticTag().equals("E")) {
					sentFileJson.addEntity( "T"+i, namedEntity.getBegin(), namedEntity.getEnd(), namedEntity.textStr(), namedEntity.getSemanticTag());
					
					map.put(namedEntity.getKey(),"T"+i);
					Vector<String> alias = new Vector<String>();
					alias.add(namedEntity.getSemanticTag());
	
					sentSemJson.addEntity(namedEntity.getSemanticTag(), alias, color );
				} else {
					if (neMap.containsKey(namedEntity.getSemanticTag())) {
						neMap.put(namedEntity.getSemanticTag(), neMap.get(namedEntity.getSemanticTag()).intValue()+1);
					} else {
						neMap.put(namedEntity.getSemanticTag(), 1);
					}
					bratFile.addEntity( "T"+i, namedEntity.getBegin(), namedEntity.getEnd(), namedEntity.textStr(), namedEntity.getSemanticTag());
					
					map.put(namedEntity.getKey(),"T"+i);
					Vector<String> alias = new Vector<String>();
					alias.add(namedEntity.getSemanticTag());
	
					bratSem.addEntity(namedEntity.getSemanticTag(), alias, color );
				}
	
			}
			PieChart chart = new PieChart();
			List<String> legend = new ArrayList<>();
			List<PieChartElement> data = new ArrayList<>();
			for (Map.Entry<String, Integer> entry : neMap.entrySet()) {
				PieChartElement element = new PieChartElement();
				element.setName(entry.getKey());
				element.setValue(entry.getValue());
				data.add(element);
				legend.add(entry.getKey());
			}
			chart.setData(data);
			chart.setLegend(legend);
			pieCharts.put("entity", new Gson().toJson(chart));
		}
		Vector<PlatoRelation> relationList = document.getRelations();
		if (relationList != null && relationList.size() > 0) {
			Map<String, Integer> reMap = new HashMap<>();
			for (int i = 0; i < relationList.size(); i++) {
				PlatoRelation relation = relationList.get(i);
				if (relation.getSemanticTag().equals("Sentence")) {
					sentFileJson.addRelation("R"+i, map.get(relation.getEntFrom().getKey()), map.get(relation.getEntTo().getKey()), relation.getSemanticTag());
					Vector<String> alias = new Vector<String>();
					alias.add(relation.getSemanticTag());
					sentSemJson.addRelation(relation.getSemanticTag(), alias, relation.getEntFrom().getSemanticTag(), relation.getEntTo().getSemanticTag());
			
				} else {
					if (reMap.containsKey(relation.getSemanticTag())) {
						reMap.put(relation.getSemanticTag(), reMap.get(relation.getSemanticTag()).intValue()+1);
					} else {
						reMap.put(relation.getSemanticTag(), 1);
					}
					bratFile.addRelation("R"+i, map.get(relation.getEntFrom().getKey()), map.get(relation.getEntTo().getKey()), relation.getSemanticTag());
					Vector<String> alias = new Vector<String>();
					alias.add(relation.getSemanticTag());
					bratSem.addRelation(relation.getSemanticTag(), alias, relation.getEntFrom().getSemanticTag(), relation.getEntTo().getSemanticTag());
				}
			}
			PieChart chart = new PieChart();
			List<String> legend = new ArrayList<>();
			List<PieChartElement> data = new ArrayList<>();
			for (Map.Entry<String, Integer> entry : reMap.entrySet()) {
				PieChartElement element = new PieChartElement();
				element.setName(entry.getKey());
				element.setValue(entry.getValue());
				data.add(element);
				legend.add(entry.getKey());
			}
			chart.setData(data);
			chart.setLegend(legend);
			pieCharts.put("relation", new Gson().toJson(chart));
		}
		result.setBratFile(bratFile.getJson(null));
		result.setBratSem(bratSem.getJson(null, null));
		result.setTokenBratFile(tokenFileJson.getJson(null));
		result.setTokenBratSem(tokenSemJson.getJson(null, null));
		result.setSentBratFile(sentFileJson.getJson(null));
		result.setSentBratSem(sentSemJson.getJson(null, null));
		result.setTypeDefXml(new TypeDefXml());
		result.setOutputTxt(output.getTxtContent());
		result.setPieCharts(pieCharts);
		return result;
	}
	
}
