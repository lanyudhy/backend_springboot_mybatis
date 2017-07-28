package com.hebta.plato.service;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Handler;
import java.util.logging.SocketHandler;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang3.StringUtils;
import org.apache.uima.jcas.JCas;
import org.plato.nlp.config.ConfigUtil;
import org.plato.nlp.structure.DocProcessor;
import org.plato.nlp.structure.Document;
import org.plato.nlp.structure.PlatoNamedEntity;
import org.plato.nlp.structure.XmiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.hebta.plato.dao.ComponentMapper;
import com.hebta.plato.dao.CorpusModelMapper;
import com.hebta.plato.dao.PipelineInputOutputMapper;
import com.hebta.plato.dto.ComponentDTO;
import com.hebta.plato.dto.PipelineDTO;
import com.hebta.plato.pojo.Component;
import com.hebta.plato.pojo.ComponentResource;
import com.hebta.plato.pojo.NLPProcessor;
import com.hebta.plato.pojo.Pipeline;
import com.hebta.plato.pojo.PipelineInput;
import com.hebta.plato.pojo.PipelineOutput;
import com.hebta.plato.pojo.User;
import com.hebta.plato.utilities.FileUtil;
import com.hebta.plato.utilities.ReadXmlFiles;
import com.hebta.plato.utilities.SessionUtil;


@Service
public class ComponentService {
	private Logger logger = LoggerFactory.getLogger(ComponentService.class);
	
	@Autowired
	private ComponentMapper componentDao;
	@Autowired
	private PipelineInputOutputMapper inputOutputDao;
	@Autowired
	private CorpusTaggedFileService taggedService;
	
	@Value("${pipeline.xml.file.path}")
	private String pipelineXmlPath;
	@Value("${pipeline.input.file.path}")
	private String pipelineDocInputPath;
	@Value("${pipeline.output.file.path}")
	private String pipelineDocOutputPath;
	@Value("${component.resource.file.path}")
	private String componentResourcePath;
	
	public Map<String, List<ComponentDTO>> getChosenComponentAndLibByPipelineId(Long pipelineId){
		Map<String, List<ComponentDTO>> componentMap = new HashMap<>();
		componentMap.put("chosen", getComponentDtoByPipelineId(pipelineId, false));
		componentMap.put("lib", getComponentLib());
		return componentMap;
	}
	
	public List<ComponentDTO> getComponentDtoByPipelineId(Long pipelineId, Boolean withContent){
		List<ComponentDTO> dtoList = new ArrayList<>();
		List<Component> componentList = componentDao.selectComponentByPipelineId(pipelineId);
		if (componentList != null && componentList.size() > 0) {
			for (Component cpt : componentList) {
				List<ComponentResource> resourceList = componentDao.selectComponentResourceByComponentId(cpt.getComponentId());
				String selectedResource = "";
				if (resourceList != null && resourceList.size() > 0) {
					for (ComponentResource resource : resourceList) {
						if (resource.getType().equals("conf")) {
							String path = componentResourcePath+"/"+resource.getComponentId();
							FileUtil.createFolder(path);
							path = path + "/"+resource.getResourceName()+".conf";
							try {
								FileUtil.writeFileByBytes(path, resource.getContent());
								ReadXmlFiles r = new ReadXmlFiles();
								selectedResource = r.getXmlFileParam(path);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						if (!withContent) {
							resource.setContent(null);
						}
					}
					if (!StringUtils.isEmpty(selectedResource)) {
						for (ComponentResource resource : resourceList) {
							String fileName = selectedResource.substring(0, selectedResource.lastIndexOf("."));
				        	String suffix = selectedResource.substring(selectedResource.lastIndexOf(".")+1, selectedResource.length());
				        	if (fileName.equals(resource.getResourceName()) && suffix.equals(resource.getType())) {
				        		resource.setConfSelected(1);
				        		break;
				        	}
						}
					}
				}
				ComponentDTO dto = buildComponentDTO(cpt, resourceList);
				dtoList.add(dto);
			}
		}
		return dtoList;
	}
	
	private ComponentDTO buildComponentDTO(Component cpt, List<ComponentResource> resourceList){
		ComponentDTO dto = new ComponentDTO();
		dto.setComponentId(cpt.getComponentId());
		dto.setComponentName(cpt.getComponentName());
		dto.setGroupId(cpt.getGroupId());
		dto.setPipelineId(cpt.getPipelineId());
		dto.setResourceList(resourceList);
		dto.setCreatedBy(cpt.getCreatedBy());
		dto.setCreatedTime(cpt.getCreatedTime());
		dto.setSequenceNum(cpt.getSequenceNum());
		dto.setComponentLibId(cpt.getComponentLibId());
		return dto;
	}
	
	public List<ComponentDTO> getComponentLib() {
		List<ComponentDTO> dtoList = new ArrayList<>();
		List<Component> componentList = componentDao.selectAllComponentLib();
		if (componentList != null && componentList.size() > 0) {
			for (Component cpt : componentList) {
				List<ComponentResource> resourceList = componentDao.selectComponentResourceLibByComponentId(cpt.getComponentId());
				if (resourceList != null && resourceList.size() > 0) {
					for (ComponentResource resource : resourceList) {
						resource.setContent(null);
					}
				}
				ComponentDTO dto = buildComponentDTO(cpt, resourceList);
				dtoList.add(dto);
			}
		}
		return dtoList;
	}
	
	public Component addComponent(Long pipelineId, Long componentId){
		Component lib = componentDao.selectComponentLibByPrimaryKey(componentId);
		List<ComponentResource> resourceLibList = componentDao.selectComponentResourceLibByComponentId(lib.getComponentId());
		Integer maxSeqNum = componentDao.selectMaxSeqByPipelineId(pipelineId);
		Component cpt = lib;
		cpt.setComponentId(null);
		cpt.setPipelineId(pipelineId);
		cpt.setSequenceNum(maxSeqNum + 1);
		componentDao.insertComponent(cpt);
		if (resourceLibList != null && resourceLibList.size() > 0) {
			for (ComponentResource resourceLib : resourceLibList) {
				ComponentResource resource = resourceLib;
				resource.setComponentId(cpt.getComponentId());
				resource.setPipelineId(pipelineId);
				resource.setResourceId(null);
			}
			componentDao.batchInsertComponentResource(resourceLibList);
		}
		
		return cpt;
	}
	
	@Transactional
	public PipelineDTO batchAddComponent(PipelineDTO pipelineDto){
		User user = SessionUtil.getSessionUser();
		logger.info("开始删除组件。");
		List<Long> requestComponentIds = new ArrayList<>();
		List<ComponentDTO> requestComponentList = pipelineDto.getComponentDtoList();
		List<ComponentDTO> invalidComponentList = validationComponent(requestComponentList);
		
		if (invalidComponentList != null && invalidComponentList.size() > 0) {
			pipelineDto.setComponentDtoList(invalidComponentList);
			return pipelineDto;
		}
		
		if (requestComponentList != null && requestComponentList.size() > 0) {
			for (ComponentDTO dto : requestComponentList) {
				if (dto.getComponentId() != null) {
					requestComponentIds.add(dto.getComponentId());
				}
			}
		}
		List<Component> oriComponentList = componentDao.selectComponentByPipelineId(pipelineDto.getPipelineId());
		if (oriComponentList != null && oriComponentList.size() > 0) {
			for (Component cpt : oriComponentList) {
				if (!requestComponentIds.contains(cpt.getComponentId())) {
					componentDao.deleteComponentByComponentId(cpt.getComponentId());
					componentDao.deleteComponentResourceByComponentId(cpt.getComponentId());
				}
			}
		}
		logger.info("删除组件结束。");
		
		List<ComponentDTO> componentDtoList = pipelineDto.getComponentDtoList();
		if (componentDtoList != null && componentDtoList.size() > 0) {
			logger.info("开始新增后更新组件，共"+componentDtoList.size()+"个");
			for (int i = 0; i < componentDtoList.size(); i++) {
				Component cpt = componentDtoList.get(i);
				if (cpt.getComponentId() == null) {
					cpt.setComponentId(null);
					cpt.setPipelineId(pipelineDto.getPipelineId());
					cpt.setCreatedTime(new Date());
					cpt.setCreatedBy(String.valueOf(user.getId()));
					componentDao.insertComponent(cpt);
					
					List<ComponentResource> resourceList = componentDao.selectComponentResourceLibByComponentId(cpt.getComponentLibId());
					if (resourceList != null && resourceList.size() > 0) {
						for (ComponentResource resource : resourceList) {
							resource.setComponentId(cpt.getComponentId());
							resource.setPipelineId(pipelineDto.getPipelineId());
							resource.setCreatedTime(new Date());
							resource.setCreatedBy(String.valueOf(user.getId()));
							resource.setResourceId(null);
							componentDao.insertComponentResource(resource);
						}
					}
				} else {
					componentDao.updateComponentByPrimaryKey(cpt);
				}
				
			}
			logger.info("新增或更新组件结束。");
		}
		pipelineDto.setComponentDtoList(null);
		return pipelineDto;
	}
	
	public List<ComponentDTO> validationComponent(List<ComponentDTO> dtoList) {
		List<ComponentDTO> invalidComponentList = new ArrayList<>();
		
		if (dtoList != null && dtoList.size() > 0) {
			for (int i = 0; i < dtoList.size(); i++) {
				ComponentDTO dto = dtoList.get(i);
				List<Long> parentIdList = componentDao.selectParentIdByComponentLibId(dto.getComponentLibId());
				if (parentIdList != null && parentIdList.size() > 0) {
					List<Long> existIdList = new ArrayList<>();
					for (int j = 0; j < i; j++) {
						existIdList.add(dtoList.get(j).getComponentLibId());
					}
					if (existIdList.containsAll(parentIdList)) {
						continue;
					} else {
						invalidComponentList.add(dto);
					}
				}
			}
		}
		
		return invalidComponentList;
	}
	
	public List<ComponentResource> getComponentResourceByComponentId(Long componentId){
		List<ComponentResource> resourceList = componentDao.selectComponentResourceByComponentId(componentId);
		String selectedResource = "";
		for (ComponentResource resource : resourceList) {
			if (resource.getType().equals("conf")) {
				String path = componentResourcePath+"/"+resource.getComponentId();
				FileUtil.createFolder(path);
				path = path + "/"+resource.getResourceName()+".conf";
				try {
					FileUtil.writeFileByBytes(path, resource.getContent());
					ReadXmlFiles r = new ReadXmlFiles();
					selectedResource = r.getXmlFileParam(path);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
		for (ComponentResource resource : resourceList) {
			String fileName = selectedResource.substring(0, selectedResource.lastIndexOf("."));
        	String suffix = selectedResource.substring(selectedResource.lastIndexOf(".")+1, selectedResource.length());
        	if (fileName.equals(resource.getResourceName()) && suffix.equals(resource.getType())) {
        		resource.setConfSelected(1);
        		break;
        	}
		}
		return resourceList;
	}
	
	public List<ComponentResource> uploadFiles(HttpServletRequest request) {
		User user = SessionUtil.getSessionUser();
//		User user = new User();
//		user.setId(1L);
		List<MultipartFile> files = ((MultipartHttpServletRequest)request).getFiles("file");  
		Long id = Long.valueOf(((MultipartHttpServletRequest)request).getParameter("id"));
		Component component = componentDao.selectComponentByPrimaryKey(id);
		for (int i =0; i< files.size(); ++i) {  
            MultipartFile file = files.get(i);  
            String name = file.getOriginalFilename();
            if (!file.isEmpty()) {  
                try {  
                    byte[] bytes = file.getBytes();  

                    if (name.toLowerCase().endsWith(".txt") || name.toLowerCase().endsWith(".bin") || name.toLowerCase().endsWith(".jar")
                    		 || name.toLowerCase().endsWith(".ruta") || name.toLowerCase().endsWith(".dict")) {
                    	String fileName = name.substring(0, name.lastIndexOf("."));
                    	String suffix = name.substring(name.lastIndexOf(".")+1, name.length());

	                    ComponentResource resource = new ComponentResource();
	                    resource.setComponentId(id);
	                    resource.setContent(bytes);
	                    resource.setCreatedBy(String.valueOf(user.getId()));
	                    resource.setCreatedTime(new Date());
	                    if (name.toLowerCase().endsWith(".jar") || name.toLowerCase().endsWith(".bin")) {
	                    	resource.setEditable(0L);
	                    } else {
	                    	resource.setEditable(1L);
	                    }
	                    resource.setPipelineId(component.getPipelineId());
	                    resource.setResourceId(null);
	                    resource.setResourceName(fileName);
	                    resource.setType(suffix);
	                    componentDao.insertComponentResource(resource);
                    } else {
                    	throw new RuntimeException("未知文件类型。");
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
		
		return getComponentResourceByComponentId(id);
	}
	
	public int deleteComponentResourceByResourceId(Long resourceId){
		return componentDao.deleteComponentResourceByResourceId(resourceId);
	}
	
	public String getComponentResourceContent(Long resourceId){
		ComponentResource resource = componentDao.selectComponentResourceByPrimaryKey(resourceId);
		return new String(resource.getContent());
	}
	
	public String updateComponentResourceContent(ComponentResource resource){
		try {
			ComponentResource oriResource = componentDao.selectComponentResourceByPrimaryKey(resource.getResourceId());
			String contentStr = resource.getContentStr();
			
			if (oriResource.getType().equals("conf")) {
				ComponentResource newResource = componentDao.selectComponentResourceByPrimaryKey(Long.valueOf(contentStr));
				contentStr = newResource.getResourceName() + "." + newResource.getType(); 
				String path = componentResourcePath+"/"+oriResource.getComponentId();
				FileUtil.createFolder(path);
				path = path + "/"+oriResource.getResourceName()+".conf";
				FileUtil.writeFileByBytes(path, oriResource.getContent());
				ReadXmlFiles r = new ReadXmlFiles();
				String newContent = r.updateXmlFile(path, contentStr);
				BufferedReader reader = new BufferedReader(new StringReader(newContent));
				StringBuffer buffer = new StringBuffer();
				String temp  = "";
				try {
					for(int i=0;(temp =reader.readLine())!=null;i++){
						//第一行xml头不读
						if (i == 0){
							continue;
						}
		                buffer.append(temp);
		                // 行与行之间的分隔符 相当于“\n”
		                buffer = buffer.append(System.getProperty("line.separator"));
		            }
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				contentStr = buffer.toString();
			} 
			resource.setContent(contentStr.getBytes("UTF-8"));
			componentDao.updateComponentResourceByPrimaryKey(resource);
			return contentStr;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}
	
	public void generateComponentResourceFileByPipelineId(Long pipelineId){
		List<ComponentDTO> dtoList = getComponentDtoByPipelineId(pipelineId,true);
		if (dtoList != null && dtoList.size() > 0) {
			FileUtil.createFolder(pipelineXmlPath + "/" + pipelineId);
			for (ComponentDTO dto : dtoList) {
				String folderPath = pipelineXmlPath + "/"+pipelineId+"/"+dto.getComponentName();
				FileUtil.createFolder(folderPath);
				List<ComponentResource> resourceList = dto.getResourceList();
				if (resourceList != null && resourceList.size() > 0) {
					for (ComponentResource resource : resourceList) {
						try {
							String filePath = folderPath + "/" + resource.getResourceName() + "." + resource.getType();
							File file = new File(filePath);
							file.createNewFile();
							FileUtil.writeFileByBytes(filePath, resource.getContent());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	public Integer processPipeline(Pipeline pipeline){	
		User user = SessionUtil.getSessionUser();
//		User user = new User();
//		user.setId(4L);
		Handler handler = null;
		try {
			handler = new SocketHandler("localhost", 5000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		java.util.logging.Logger logger1 = java.util.logging.Logger.getLogger(CorpusTaggedFileService.class.getName() );
	    logger1.addHandler(handler);
	    logger1.info(user.getId()+"@开始运行自然语言处理项目");

		logger.info("进入 processPipeline()");	
		if (!pipelineXmlPath.endsWith("/")){
			pipelineXmlPath += "/";
		}
		Long pipelineId = pipeline.getProjectId();
		
		generateComponentResourceFileByPipelineId(pipelineId);
		File file = new File(pipelineXmlPath + pipelineId + ".pipeline");
		logger.info("构造的 **.pipeline 文件路径为 {}", file.getAbsolutePath());
		
		List<NLPProcessor> processorList = new ArrayList<>();
		List<Component> componentList = componentDao.selectComponentByPipelineId(pipeline.getProjectId());
		for (Component cpt : componentList) {
			NLPProcessor processor = new NLPProcessor();
			processor.setConf(pipeline.getProjectId()+"/"+cpt.getComponentName());
			processorList.add(processor);
		}
		pipeline.setNLPProcessor(processorList);
		
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Pipeline.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		
			jaxbMarshaller.marshal(pipeline, file);
			logger.info("完成实例 Pipeline 写入到 .pipeline 为后缀的文本文件了");

			file.createNewFile();
			List<DocProcessor> docProcessors = ConfigUtil.importPipelineFromFile(file);
			List<PipelineInput> inputs = inputOutputDao.selectPipelineInputByPipeLineId(pipelineId);
			FileUtil.createFolder(pipelineDocInputPath + "/" + pipelineId);
			FileUtil.createFolder(pipelineDocOutputPath + "/" + pipelineId);
			for (PipelineInput input : inputs){
				logger1.info(user.getId()+"@处理 " + input.getFileName() + "." + "xmi 文件");
				StringBuffer sb = new StringBuffer();
				sb.append(pipelineDocInputPath).append("/").append(pipelineId).append("/")
					.append(input.getFileName()).append(".").append(input.getFileType());
				String filePath = sb.toString();
				logger.info("当前 pipeline 输入文件的路径：：{}", filePath);
				FileUtil.createFile(filePath, input.getFileContent());
				
				JCas aJCas = XmiUtil.createJCas(filePath);
				aJCas = taggedService.processSentenceAndToken(aJCas);
				for (DocProcessor processor : docProcessors){
					processor.process(aJCas);
				}
				String outputPath = pipelineDocOutputPath + "/" + pipelineId + "/" + input.getFileName() + "." + "xmi";
				XmiUtil.writeXmi(aJCas, outputPath);
				
				Document doc = new Document(aJCas);
				
				Vector<PlatoNamedEntity> namedEntityList = doc.getNameEntity();
				StringBuffer namedEntity = new StringBuffer();
				if (namedEntityList != null && namedEntityList.size() > 0) {
					for (PlatoNamedEntity entity : namedEntityList) {
						if (!entity.getSemanticTag().equals("Tk") && !entity.getSemanticTag().equals("S") && !entity.getSemanticTag().equals("E")) {
							namedEntity.append(entity.getBegin()).append("\t")
								.append(entity.getEnd()).append("\t")
								.append(entity.getSemanticTag()).append("\t")
								.append(entity.getAssertion()).append("\t")
								.append(entity.getUmlsCui()).append("\t")
								.append(entity.textStr()).append("\n");
							
						}
					}
				}
				
				PipelineOutput output = new PipelineOutput();
				output.setFileName(input.getFileName());
				output.setCreatedBy(String.valueOf(user.getId()));
				output.setCreatedTime(new Date());
				output.setEncoder("UTF-8");
				output.setFileType("xmi");
				output.setInputFileId(input.getFileId());
				output.setOriContent(input.getFileContent());
				output.setPipelineId(pipelineId);
				output.setXmiContent(FileUtil.readFileByChars(outputPath));
				output.setTxtContent(namedEntity.toString());
				inputOutputDao.insertPipelineOutput(output);
				logger.info("aJCas.getDocumentText() 的值：： {}", aJCas.getDocumentText()); 
			}
		} catch (Exception e) {
			throw new RuntimeException("processPipeline() 方法出错了：：", e);
		}
		return 1;
	}
	
	public void addComponentResource(ComponentResource resource) {
		ComponentResource model = new ComponentResource();
		//resource.getContentStr() 是config.conf文件的路径
		//String content = FileUtil.readFileByChars(resource.getContentStr());
		model.setResourceId(resource.getResourceId());
		model.setContent(FileUtil.readFileByStream(resource.getContentStr()));
		componentDao.updateComponentResourceLibByPrimaryKey(model);
	}
}
