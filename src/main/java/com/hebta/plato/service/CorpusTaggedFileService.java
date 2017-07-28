package com.hebta.plato.service;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Handler;
import java.util.logging.SocketHandler;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.plato.nlp.config.ConfigUtil;
import org.plato.nlp.config.ConfigurationException;
import org.plato.nlp.config.NERFeatureConf;
import org.plato.nlp.config.NLPProcessorConf;
import org.plato.nlp.io.DocumentIOException;
import org.plato.nlp.ner.ChiCharNgramFeature;
import org.plato.nlp.ner.DictionaryFeature;
import org.plato.nlp.ner.NERFeatureExtractor;
import org.plato.nlp.ner.NERTrainer;
import org.plato.nlp.sent.SentDetectorUIMA;
import org.plato.nlp.structure.BratFileJson;
import org.plato.nlp.structure.BratSemJson;
import org.plato.nlp.structure.DocProcessor;
import org.plato.nlp.structure.Document;
import org.plato.nlp.structure.PlatoNamedEntity;
import org.plato.nlp.structure.PlatoRelation;
import org.plato.nlp.structure.PlatoSentence;
import org.plato.nlp.structure.PlatoToken;
import org.plato.nlp.structure.XmiUtil;
import org.plato.nlp.token.TokenizerUIMA;
import org.plato.nlp.util.HtmlColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.hebta.plato.dao.CorpusModelMapper;
import com.hebta.plato.dao.CorpusTaggedFileMapper;
import com.hebta.plato.dao.ProjectMapper;
import com.hebta.plato.dto.FileNameToType;
import com.hebta.plato.dto.FileWithPagination;
import com.hebta.plato.dto.MoveCorpusTaggedFiles;
import com.hebta.plato.dto.PieChart;
import com.hebta.plato.dto.PieChartElement;
import com.hebta.plato.dto.TaggedFileReturn;
import com.hebta.plato.dto.TypeDefXml;
import com.hebta.plato.pojo.CorpusModel;
import com.hebta.plato.pojo.CorpusTaggedFile;
import com.hebta.plato.pojo.ModelOutput;
import com.hebta.plato.pojo.User;
import com.hebta.plato.utilities.FileUtil;
import com.hebta.plato.utilities.SessionUtil;
import com.hebta.plato.utilities.ZipUtil;

import opennlp.tools.util.StringUtil;

@Service
public class CorpusTaggedFileService {

	private Logger logger = LoggerFactory.getLogger(CorpusTaggedFileService.class);
	
	@Value("${corpus.upload.path}")
	private String uploadPath;
	@Value("${corpus.download.path}")
	private String downloadPath;
	@Value("${corpus.train.path}")
	private String trainPath;
	@Value("${corpus.test.path}")
	private String testPath;
	@Value("${corpus.modol}")
	private String modelPath;
	@Value("${corpus.read}")
	private String readPath;
	
	@Autowired
	private CorpusTaggedFileMapper corpusTaggedFileDao;
	@Autowired
	private CorpusModelMapper corpusModelDao;
	@Autowired
	private TypeDefService typeDefService;
	@Autowired
	private JdbcTemplate jdbcTemp;
	
	public CorpusTaggedFile addCorpusTaggedFile(CorpusTaggedFile file){
		int flag = corpusTaggedFileDao.insertCorpusTaggedFile(file);
		if (flag < 1) {
			throw new RuntimeException("保存标准文件失败");
		}
		return file;
	}
	
	public List<CorpusTaggedFile> getCorpusTaggedFileByCorpusId(Long corpusId, String folder){
		return corpusTaggedFileDao.selectCorpusTaggedFileByCorpusId(corpusId, folder);
	}
	
	public TaggedFileReturn getCorpusTaggedFileByPrimaryKey(Long fileId){
		JCas aJCas = getJCas(fileId);
		return loadXmi(fileId, aJCas);
	}
	
	public TaggedFileReturn getModelOutputFileByFileId(Long fileId){
		ModelOutput file = corpusModelDao.selectModelOutputByPrimaryKey(fileId);
		CorpusModel model = corpusModelDao.selectCorpusModelByPrimaryKey(file.getModelId());
		FileUtil.createFolder(modelPath+"/"+file.getCorpusId());
		FileUtil.createFolder(modelPath+"/"+file.getCorpusId()+"/"+model.getModelName());
		FileUtil.createFolder(modelPath+"/"+file.getCorpusId()+"/"+model.getModelName()+"/output");
		String filePath = modelPath+"/"+file.getCorpusId()+"/"+model.getModelName()+"/output/"+file.getFileName()+".xmi";
		FileUtil.createFile(filePath, file.getXmiContent());
		try {
			JCas aJCas =  XmiUtil.loadXmi(filePath);
			return loadXmi(null, aJCas);
		} catch (UIMAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public int deleteCorpusTaggedFileByCorpusId(Long corpusId, String folder){
		User user = SessionUtil.getSessionUser();
		return corpusTaggedFileDao.deleteCorpusTaggedFileByCorpusId(corpusId, folder, String.valueOf(user.getId()));
	}
	
	public int deleteCorpusTaggedFileByFileId(Long fileId, String folder){
		return corpusTaggedFileDao.deleteCorpusTaggedFileByFileId(fileId, folder);
	}
	
	public FileWithPagination getTaggedFileWithPagination(Long corpusId, Integer page, Integer countPerPage, String folder){
		User user = SessionUtil.getSessionUser();
		String sql = "SELECT UI.LOGIN_NAME,UP.PROJECT_OWNER FROM USER_PROJECT UP JOIN USER_INFO UI ON UP.USER_ID = UI.ID WHERE UP.PROJECT_ID = " + corpusId;
		FileWithPagination pagination = new FileWithPagination();
		List<FileNameToType> fileFullNameList = new ArrayList<>();
		Integer totalCount = 0;
		Map<String, String> ownerMap = new HashMap<>();
		jdbcTemp.query(sql, (rs) -> {
			ownerMap.put(rs.getString("LOGIN_NAME"), rs.getString("PROJECT_OWNER"));
		});
		if (ownerMap.get(user.getLoginName()) != null) {
			if ("1".equals(ownerMap.get(user.getLoginName()))) {
				fileFullNameList =  corpusTaggedFileDao.selectTaggedFiletWithPaginationByOwner(corpusId, page, countPerPage, folder, user.getLoginName());
				totalCount = corpusTaggedFileDao.selectTotalCountTaggedFileByCorpusOwner(corpusId, folder, user.getLoginName());
			} else {
				fileFullNameList =  corpusTaggedFileDao.selectTaggedFiletWithPagination(corpusId, page, countPerPage, folder, String.valueOf(user.getLoginName()));
				totalCount = corpusTaggedFileDao.selectTotalCountTaggedFileByCorpusId(corpusId, folder, String.valueOf(user.getLoginName()));
			}
		} else {
			throw new RuntimeException("没有权限。");
		}
		
		Integer totalPage = totalCount/countPerPage;
		if (totalCount%countPerPage != 0) {
			totalPage = totalPage + 1;
		}
		pagination.setFileFullNameList(fileFullNameList);
		pagination.setCurrentPage(page);
		pagination.setTotalPage(totalPage);
		
		return pagination;
	}
	
	public int moveCorpusTaggedFiles(MoveCorpusTaggedFiles moveFiles) {
		User user = SessionUtil.getSessionUser();
		List<CorpusTaggedFile> fromFileList = corpusTaggedFileDao.selectCorpusTaggedFileByFileIds(moveFiles.getFileIds(), moveFiles.getFrom());
		if ("copy".equals(moveFiles.getAction())) {
			if (fromFileList != null && fromFileList.size() > 0) {
				for (CorpusTaggedFile file : fromFileList) {
					file.setFileId(null);
					file.setFileName(file.getFileName()+"_"+file.getModifiedBy());
					file.setCreatedTime(new Date());
					file.setFolder(moveFiles.getTo());
					file.setModifiedBy(user.getLoginName());
					file.setModifiedTime(null);
					corpusTaggedFileDao.insertCorpusTaggedFile(file);
				}
			}
		} else if ("cut".equals(moveFiles.getAction())) {
			if (fromFileList != null && fromFileList.size() > 0) {
				for (CorpusTaggedFile file : fromFileList) {
					file.setFileId(null);
					file.setFileName(file.getFileName()+"_"+file.getModifiedBy());
					file.setCreatedTime(new Date());
					file.setFolder(moveFiles.getTo());
					file.setModifiedBy(user.getLoginName());
					file.setModifiedTime(null);
					corpusTaggedFileDao.insertCorpusTaggedFile(file);
				}
			}
			corpusTaggedFileDao.deleteCorpusTaggedFileByFileIds(moveFiles.getFileIds(), moveFiles.getFrom());
		} else {
			throw new RuntimeException("未知操作。");
		}
		return 1;
	}
	
	public FileWithPagination uploadFiles(HttpServletRequest request) {
		User user = SessionUtil.getSessionUser();
//		User user = new User();
//		user.setId(1L);
		List<MultipartFile> files = ((MultipartHttpServletRequest)request).getFiles("file");  
		String type = ((MultipartHttpServletRequest)request).getParameter("type");
		Long id = Long.valueOf(((MultipartHttpServletRequest)request).getParameter("id"));
		List<String> userNameList = jdbcTemp.queryForList("SELECT UI.LOGIN_NAME FROM USER_PROJECT UP JOIN USER_INFO UI ON UP.USER_ID = UI.ID WHERE UP.PROJECT_ID = " + id, String.class);
		for (int i =0; i< files.size(); ++i) {  
            MultipartFile file = files.get(i);  
            String name = file.getOriginalFilename();
            if (!file.isEmpty()) {  
                try {  
                    byte[] bytes = file.getBytes();
//                    String[] namePart = name.split("\\.");
                    String path = uploadPath + "/" + id + "/" + name;
                    FileUtil.createFolder(uploadPath + "/" + id);
                    if (name.toLowerCase().endsWith("zip")) {
                    	
                    	BufferedOutputStream stream =  
                                new BufferedOutputStream(new FileOutputStream(new File(path)));  
                        stream.write(bytes);  
                        stream.close(); 
                    	Map<String, String> map = ZipUtil.ZipContraMultiFileWithReturn(path, uploadPath);
                    	for (Map.Entry<String, String> entry : map.entrySet()) {
                    		String content = entry.getValue();
//                    		String[] subFileName = entry.getKey().split("\\.");
    	                    CorpusTaggedFile taggedFile = new CorpusTaggedFile();
    	                    taggedFile.setCorpusId(id);
    	                    taggedFile.setEncoder("UTF-8");
    	                    taggedFile.setCreatedTime(new Date());
    	                    taggedFile.setCreatedBy(String.valueOf(user.getId()));
    	                    taggedFile.setFolder(type);
    	                    String fileName = entry.getKey().substring(0, entry.getKey().lastIndexOf("."));
    	                    if (entry.getKey().toLowerCase().endsWith(".txt")) {
    	                    	taggedFile.setFileType("xmi");
    	                    	taggedFile.setFileContent(content);
    	                    	taggedFile.setFileName(fileName);
    	                    	JCas aJCas = XmiUtil.createJCas(entry.getKey()); 
    	                    	aJCas = processSentenceAndToken(aJCas);
    		                    String xmiFileName = uploadPath + "/" + id + "/"  + fileName + ".xmi";
    		                    XmiUtil.writeXmi(aJCas, xmiFileName);
    		                    taggedFile.setXmiContent(FileUtil.readFileByChars(xmiFileName));

    	                    } else if (entry.getKey().toLowerCase().endsWith(".xmi")) {
    	                    	
    	                    	taggedFile.setFileName(fileName);
    	                    	taggedFile.setFileType("xmi");
    	                    	taggedFile.setXmiContent(content);
    	                    } else {
    	                    	throw new RuntimeException(entry.getKey() + "未知文件类型。");
    	                    }
    	                    if (type.equals("train")) {
    	                    	corpusTaggedFileDao.insertCorpusTaggedFile(taggedFile);
    	                    	for(String userName : userNameList) {
    	                    		taggedFile.setModifiedBy(String.valueOf(userName));
    	                    		corpusTaggedFileDao.insertCorpusTaggedFile(taggedFile);
    	                    	}
    	                    } else {
    	                    	//taggedFile.setModifiedTime(new Date());
    	                    	taggedFile.setModifiedBy(user.getLoginName());
	                    		corpusTaggedFileDao.insertCorpusTaggedFile(taggedFile);
    	                    }
                		}
                    	
                    } else if (name.toLowerCase().endsWith(".txt")) {
                    	String fileName = name.substring(0, name.lastIndexOf("."));
	                    String content = new String(bytes,"UTF-8");
                    	BufferedOutputStream stream =  
                                new BufferedOutputStream(new FileOutputStream(new File(path)));  
                        stream.write(bytes);  
                        stream.close(); 
	                    CorpusTaggedFile taggedFile = new CorpusTaggedFile();
	                    taggedFile.setCorpusId(id);
	                    taggedFile.setFileType("xmi");
	                    taggedFile.setFileName(fileName);
	                    taggedFile.setFileContent(content);
	                    taggedFile.setEncoder("UTF-8");
	                    taggedFile.setCreatedTime(new Date());
	                    taggedFile.setCreatedBy(String.valueOf(user.getId()));
	                    taggedFile.setFolder(type);
	                    JCas aJCas = XmiUtil.createJCas(path); 
	                    aJCas = processSentenceAndToken(aJCas);
	                    String xmiFileName = uploadPath + "/" + id + "/" + fileName + ".xmi";
	                    XmiUtil.writeXmi(aJCas, xmiFileName);
	                    taggedFile.setXmiContent(FileUtil.readFileByChars(xmiFileName));
	                    
	                    if (type.equals("train")) {
	                    	corpusTaggedFileDao.insertCorpusTaggedFile(taggedFile);
	                    	for(String userName : userNameList) {
	                    		taggedFile.setModifiedBy(userName);
	                    		corpusTaggedFileDao.insertCorpusTaggedFile(taggedFile);
	                    	}
	                    } else {
	                    	//taggedFile.setModifiedTime(new Date());
	                    	taggedFile.setModifiedBy(user.getLoginName());
                    		corpusTaggedFileDao.insertCorpusTaggedFile(taggedFile);
	                    }
                    } else if (name.toLowerCase().endsWith("xmi")) {
                    	String fileName = name.substring(0, name.lastIndexOf("."));
                    	String content = new String(bytes);
	                    CorpusTaggedFile taggedFile = new CorpusTaggedFile();
	                    taggedFile.setCorpusId(id);
	                    taggedFile.setFileType("xmi");
	                    taggedFile.setFileName(fileName);
	                    taggedFile.setXmiContent(content);
	                    taggedFile.setEncoder("UTF-8");
	                    taggedFile.setCreatedTime(new Date());
	                    taggedFile.setCreatedBy(String.valueOf(user.getId()));
	                    taggedFile.setFolder(type);
	                    
	                    if (type.equals("train")) {
	                    	corpusTaggedFileDao.insertCorpusTaggedFile(taggedFile);
	                    	for(String userName : userNameList) {
	                    		taggedFile.setModifiedBy(userName);
	                    		corpusTaggedFileDao.insertCorpusTaggedFile(taggedFile);
	                    	}
	                    } else {
	                    	//taggedFile.setModifiedTime(new Date());
	                    	taggedFile.setModifiedBy(user.getLoginName());
                    		corpusTaggedFileDao.insertCorpusTaggedFile(taggedFile);
	                    }  
                    } else {
                    	throw new RuntimeException("未知文件类型。");
                    }
                    //FileUtil.delFolder(uploadPath + "/" + id);
                } catch (Exception e) {  
                    logger.info("You failed to upload " + name + " => " + e.getMessage());  
                    throw new RuntimeException("上传失败。");
                }  
            } else {  
            	logger.info("You failed to upload " + name + " because the file was empty.");  
            	throw new RuntimeException("上传失败，因为文件为空。");
            }  
        } 
		
		return getTaggedFileWithPagination(id, 1, 20, type);
	}
	
	public String generateDownloadFilesZip(List<Long> downloadList){
		List<Long> xmiFileIds = new ArrayList<>();
		List<CorpusTaggedFile> xmiFileList = new ArrayList<>();
		if (downloadList != null && downloadList.size() > 0) {
			for (Long fileId : downloadList) {
				xmiFileIds.add(fileId);	
			}
			if (xmiFileIds != null && xmiFileIds.size() > 0) {
				xmiFileList = corpusTaggedFileDao.selectCorpusTaggedFileByFileIds(xmiFileIds, null);
			}
			String filePath = null;
			String zipPath = null;
			if (xmiFileList != null && xmiFileList.size() > 0) {
				filePath = downloadPath + "/" + xmiFileList.get(0).getCorpusId();
				zipPath = downloadPath + "/zip";
				FileUtil.createFolder(filePath);
				FileUtil.createFolder(zipPath);
				zipPath = zipPath + "/" + xmiFileList.get(0).getCorpusId() + ".zip";
				String sql = "SELECT UI.ID, UI.LOGIN_NAME FROM USER_PROJECT UP JOIN USER_INFO UI ON UP.USER_ID = UI.ID WHERE UP.PROJECT_ID = " + xmiFileList.get(0).getCorpusId();
				Map<String, String> nameMap = new HashMap<>();
				jdbcTemp.query(sql, (rs) -> {
					nameMap.put(rs.getString("ID"), rs.getString("LOGIN_NAME"));
				});
				for (CorpusTaggedFile file : xmiFileList) {
					
					Boolean bool = FileUtil.createFile(filePath + "/" + nameMap.get(file.getModifiedBy())+"_"+file.getFileName() + ".xmi", file.getXmiContent());
					if (!bool) {
						logger.info(nameMap.get(file.getModifiedBy())+"的"+file.getFileName() + ".xmi 导出失败。");
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
	
	public String generateAllFilesZip(String folder){
		List<CorpusTaggedFile> xmiFileList = corpusTaggedFileDao.selectCorpusTaggedFileByFolder(folder);

		String filePath = null;
		String zipPath = null;
		if (xmiFileList != null && xmiFileList.size() > 0) {
			filePath = downloadPath + "/" + xmiFileList.get(0).getCorpusId();
			zipPath = downloadPath + "/zip";
			FileUtil.createFolder(filePath);
			FileUtil.createFolder(zipPath);
			zipPath = zipPath + "/" + xmiFileList.get(0).getCorpusId() + ".zip";
			String sql = "SELECT UI.ID, UI.LOGIN_NAME FROM USER_PROJECT UP JOIN USER_INFO UI ON UP.USER_ID = UI.ID WHERE UP.PROJECT_ID = " + xmiFileList.get(0).getCorpusId();
			Map<String, String> nameMap = new HashMap<>();
			jdbcTemp.query(sql, (rs) -> {
				nameMap.put(rs.getString("ID"), rs.getString("LOGIN_NAME"));
			});
			for (CorpusTaggedFile file : xmiFileList) {
				Boolean bool = FileUtil.createFile(filePath + "/" + nameMap.get(file.getModifiedBy())+"_"+file.getFileName() + ".xmi", file.getXmiContent());
				if (!bool) {
					logger.info(nameMap.get(file.getModifiedBy())+"的"+file.getFileName() + ".xmi 导出失败。");
				}
			}
		}
		
		if (!StringUtils.isEmpty(filePath)){
			ZipUtil.ZipMultiFile(filePath,zipPath);
		}
		
		return zipPath;
	}
	
	public List<CorpusModel> getModelsByCorpusId(Long corpusId){
		
		return corpusModelDao.selectCorpusModelByCorpusId(corpusId);
	}
	
	public FileWithPagination getModelOutputWithPagination(Long modelId, Integer page, Integer countPerPage){
		FileWithPagination pagination = new FileWithPagination();
		List<FileNameToType> fileFullNameList =  corpusModelDao.selectModelOutputWithPagination(modelId, page, countPerPage);
		Integer totalCount = corpusModelDao.selectTotalCountModelOutputByModelId(modelId);
		Integer totalPage = totalCount/countPerPage;
		if (totalCount%countPerPage != 0) {
			totalPage = totalPage + 1;
		}
		pagination.setFileFullNameList(fileFullNameList);
		pagination.setCurrentPage(page);
		pagination.setTotalPage(totalPage);
		
		return pagination;
	}
	
	public CorpusModel getCorpusModelByModelId(Long modelId) {
		return corpusModelDao.selectCorpusModelByPrimaryKey(modelId);
	}
	
	public int deleteModel(Long modelId){
		int deleteCount = corpusModelDao.deleteCorpusModelByModelId(modelId);
		corpusModelDao.deleteModelOutputByModelId(modelId);
		return deleteCount;
	}
	
	public int deleteModelOutputFiles(List<Long> fileIds){
		return corpusModelDao.deleteModelOutputFiles(fileIds);
	}
	
	public JCas getJCas(Long fileId) {
		User user = SessionUtil.getSessionUser();
		CorpusTaggedFile file = corpusTaggedFileDao.selectCorpusTaggedFileByPrimaryKey(fileId);
		FileUtil.createFolder(readPath+"/"+file.getCorpusId());
		FileUtil.createFolder(readPath+"/"+file.getCorpusId()+"/"+user.getLoginName());
		String filePath = readPath + "/"+file.getCorpusId()+"/"+user.getLoginName()+"/"+file.getFileName()+".xmi";
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
		TaggedFileReturn result = new TaggedFileReturn();
		CorpusTaggedFile file = new CorpusTaggedFile();
		if (fileId != null) {
			file = corpusTaggedFileDao.selectCorpusTaggedFileByPrimaryKey(fileId);
			result.setTypeDefXml(typeDefService.getTypeDefXml(file.getCorpusId()));
		} else {
			result.setTypeDefXml(new TypeDefXml());
		}

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

		Vector<PlatoToken> tokens = document.getTokens();
		Vector<PlatoSentence> sentences = document.getSentences();
		if (tokens != null && tokens.size() > 0) {
			for (PlatoToken token : tokens) {
				PlatoNamedEntity ne = new PlatoNamedEntity(aJCas, token.getBegin(), token.getEnd(), "Tk");
				ne.addToIndexes();
			}
		}
		if (sentences != null && sentences.size() > 0) {
			for (PlatoSentence sentence : sentences) {
				
				PlatoNamedEntity beginNE = new PlatoNamedEntity(aJCas, sentence.getBegin(), sentence.getBegin()+1, "S");
				beginNE.addToIndexes();
				PlatoNamedEntity endNE = new PlatoNamedEntity(aJCas, sentence.getEnd()-1, sentence.getEnd(), "E");
				endNE.addToIndexes();
				PlatoRelation relation = new PlatoRelation(beginNE, endNE, "Sentence");
				relation.addToIndexes();
			}
		}
		
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
		result.setPieCharts(pieCharts);
		return result;
	}
	
	public void saveXmi(Long fileId, JCas aJCas) {
		User user = SessionUtil.getSessionUser();
		CorpusTaggedFile file = corpusTaggedFileDao.selectCorpusTaggedFileByPrimaryKey(fileId);
		FileUtil.createFolder(readPath+"/"+file.getCorpusId());
		FileUtil.createFolder(readPath+"/"+file.getCorpusId()+"/"+user.getLoginName());
		String filePath = readPath+"/"+file.getCorpusId()+"/"+user.getLoginName()+"/"+file.getFileName()+".xmi";
		FileUtil.createFile(filePath, file.getXmiContent());
		try {
			XmiUtil.writeXmi(aJCas, filePath);
			String content = FileUtil.readFileByChars(filePath);
			file.setXmiContent(content);
			file.setModifiedTime(new Date());
			corpusTaggedFileDao.updateCorpusTaggedFile(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public JCas processSentenceAndToken(JCas aJCas){
		try {
			NLPProcessorConf conf1 = NLPProcessorConf.fromFile( new File( "workspace/ComponentLibrary/NLP_components/断句/基于中文标点/config.conf" ) );
			DocProcessor proc1 = (SentDetectorUIMA)conf1.createDocProc();
			NLPProcessorConf conf2 = NLPProcessorConf.fromFile( new File( "workspace/ComponentLibrary/NLP_components/分词/后向最大匹配/config.conf" ) );
			DocProcessor proc2 = (TokenizerUIMA)conf2.createDocProc();
			
			proc1.process( aJCas );
			proc2.process( aJCas );
		
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AnalysisEngineProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return aJCas;
	}
	
	public Map<String, PlatoNamedEntity> getNameEntityMap(JCas aJCas) {
		Document document = new Document(aJCas);
		
		Vector<PlatoToken> tokens = document.getTokens();
		Vector<PlatoSentence> sentences = document.getSentences();
		if (tokens != null && tokens.size() > 0) {
			for (PlatoToken token : tokens) {
				PlatoNamedEntity ne = new PlatoNamedEntity(aJCas, token.getBegin(), token.getEnd(), "Tk");
				ne.addToIndexes();
			}
		}
		if (sentences != null && sentences.size() > 0) {
			for (PlatoSentence sentence : sentences) {
				
				PlatoNamedEntity beginNE = new PlatoNamedEntity(aJCas, sentence.getBegin(), sentence.getBegin()+1, "S");
				beginNE.addToIndexes();
				PlatoNamedEntity endNE = new PlatoNamedEntity(aJCas, sentence.getEnd()-1, sentence.getEnd(), "E");
				endNE.addToIndexes();
			}
		}
		
		Vector<PlatoNamedEntity> neList = document.getNameEntity();
		Map<String, PlatoNamedEntity> map = new HashMap<>();
		if (neList != null && neList.size() > 0) {
			for (int i = 0; i < neList.size(); i++) {
				PlatoNamedEntity entity = neList.get(i);
				map.put("T"+i, entity);
				if (entity.getSemanticTag().equals("Tk") || entity.getSemanticTag().equals("S") || entity.getSemanticTag().equals("E")) {
					document.deleteEntity(entity);
				}
			}
		}
		return map;
	}
	
	public Map<String, PlatoRelation> getRelationMap(JCas aJCas) {
		Document document = new Document(aJCas);

		Vector<PlatoSentence> sentences = document.getSentences();
		if (sentences != null && sentences.size() > 0) {
			for (PlatoSentence sentence : sentences) {
				
				PlatoNamedEntity beginNE = new PlatoNamedEntity(aJCas, sentence.getBegin(), sentence.getBegin()+1, "S");
				beginNE.addToIndexes();
				PlatoNamedEntity endNE = new PlatoNamedEntity(aJCas, sentence.getEnd()-1, sentence.getEnd(), "E");
				endNE.addToIndexes();
				PlatoRelation relation = new PlatoRelation(beginNE, endNE, "Sentence");
				relation.addToIndexes();
			}
		}
		
		Vector<PlatoRelation> reList = document.getRelations();
		Map<String, PlatoRelation> map = new HashMap<>();
		if (reList != null && reList.size() > 0) {
			for (int i = 0; i < reList.size(); i++) {
				PlatoRelation relation = reList.get(i);
				map.put("R"+i, relation);
				if (relation.getSemanticTag().equals("Sentence")) {
					document.deleteRelation(relation);
					document.deleteEntity(relation.getEntFrom());
					document.deleteEntity(relation.getEntTo());
				}
			}
		}
		return map;
	}
	
	public TaggedFileReturn addEntity(Long fileId, Integer start, Integer end, String sem) {
		JCas aJCas = getJCas(fileId);
		PlatoNamedEntity ne = new PlatoNamedEntity(aJCas, start, end, sem);
		ne.addToIndexes();
		saveXmi(fileId, aJCas);
		return loadXmi(fileId, aJCas);
	}
	
	public TaggedFileReturn addRelation(Long fileId, String start, String end, String sem) {
		JCas aJCas = getJCas(fileId);
		Map<String, PlatoNamedEntity> nameEntityMap = getNameEntityMap(aJCas);
		PlatoRelation relation = new PlatoRelation(nameEntityMap.get(start), nameEntityMap.get(end), sem);
		relation.addToIndexes();
		saveXmi(fileId, aJCas);
		return loadXmi(fileId, aJCas);
	}
	
	public TaggedFileReturn deleteEntity(Long fileId, String entityId) {
		JCas aJCas = getJCas(fileId);
		Document document = new Document(aJCas);
		Map<String, PlatoNamedEntity> nameEntityMap = getNameEntityMap(aJCas);
		document.deleteEntity(nameEntityMap.get(entityId));
		saveXmi(fileId, document.getJCas());
		return loadXmi(fileId, document.getJCas());
	}
	
	public TaggedFileReturn deleteRelation(Long fileId, String relationId) {
		JCas aJCas = getJCas(fileId);
		Document document = new Document(aJCas);
		Map<String, PlatoRelation> relationyMap = getRelationMap(aJCas);
		document.deleteRelation(relationyMap.get(relationId));
		saveXmi(fileId, document.getJCas());
		return loadXmi(fileId, document.getJCas());
	}
	
	public TaggedFileReturn updateEntity(Long fileId, String entityId, String newSem) {
		JCas aJCas = getJCas(fileId);
		Document document = new Document(aJCas);
		Map<String, PlatoNamedEntity> nameEntityMap = getNameEntityMap(aJCas);
		String entityKey = nameEntityMap.get(entityId).getKey();
		Vector<PlatoNamedEntity> nameEntities = document.getNameEntity();
		if (nameEntities != null && nameEntities.size() > 0) {
			for (PlatoNamedEntity ne : nameEntities) {
				if (entityKey.equals(ne.getKey())) {
					ne.setSemanticTag(newSem);
				}
			}
		}
		saveXmi(fileId, aJCas);
		return loadXmi(fileId, aJCas);
	}
	
	public TaggedFileReturn updateRelation(Long fileId, String relationId, String newSem) {
		JCas aJCas = getJCas(fileId);
		Document document = new Document(aJCas);
		Map<String, PlatoRelation> relationMap = getRelationMap(aJCas);
		String relationKey = relationMap.get(relationId).getKey();
		Vector<PlatoRelation> relations = document.getRelations();
		if (relations != null && relations.size() > 0) {
			for (PlatoRelation relation : relations) {
				if (relationKey.equals(relation.getKey())) {
					relation.setSemanticTag(newSem);
				}
			}
		}
		saveXmi(fileId, aJCas);
		return loadXmi(fileId, aJCas);
	}
	
	public void train(Long corpusId, Integer evaluation){
		logger.info("==========test");
		User user = SessionUtil.getSessionUser();
//		User user = new User();
//		user.setId(4L);
		Handler handler = null;
		try {
			handler = new SocketHandler("localhost", 80);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		java.util.logging.Logger logger1 = java.util.logging.Logger.getLogger(CorpusTaggedFileService.class.getName() );
	    logger1.addHandler(handler);
	    logger1.info(user.getId()+"@开始训练模型");

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");

		//先创建model
		CorpusModel model = new CorpusModel();
		model.setCorpusId(corpusId);
		model.setCreatedBy(String.valueOf(user.getId()));
		model.setCreatedTime(new Date());
		model.setModelName("model" + sdf.format(new Date()));
		corpusModelDao.insertCorpusModel(model);
		
		List<File> trainFiles = new ArrayList<File>();
        List<File> testFiles = new ArrayList<File>();

    	List<CorpusTaggedFile> trainTaggedFiles = corpusTaggedFileDao.selectCorpusTaggedFileByCorpusId(corpusId, "train");
    	
    	//evaluation 为1 代表使用测试集， 为2 代表交叉验证
        if (evaluation.equals(1)) {
        	if (trainTaggedFiles != null && trainTaggedFiles.size() > 0) {
            	String path = trainPath + "/" + corpusId;
            	FileUtil.createFolder(path);
            	for (CorpusTaggedFile taggedFile : trainTaggedFiles) {
            		String fileName = path + "/" + taggedFile.getFileName() + "_" + taggedFile.getModifiedBy() + ".xmi";
            		FileUtil.createFile(fileName, taggedFile.getXmiContent());
            		File file = new File(fileName);
            		trainFiles.add(file);
            	}
            }
        	
	        List<CorpusTaggedFile> testTaggedFiles = corpusTaggedFileDao.selectCorpusTaggedFileByCorpusId(corpusId, "test");
	        if (testTaggedFiles != null && testTaggedFiles.size() > 0) {
	        	String path = testPath + "/" + corpusId;
	        	FileUtil.createFolder(path);
	        	for (CorpusTaggedFile taggedFile : testTaggedFiles) {
	        		String fileName = path + "/" + taggedFile.getFileName() + "_" + taggedFile.getModifiedBy() + ".xmi";
	        		FileUtil.createFile(fileName, taggedFile.getXmiContent());
	        		File file = new File(fileName);
	        		testFiles.add(file);
	        	}
	        }
	        
	        process(model, user.getId(), trainFiles, testFiles, "");
	        trainFiles.clear();
	        testFiles.clear();
	        FileUtil.delAllFile(trainPath + "/" + corpusId);
        	FileUtil.delAllFile(testPath + "/" + corpusId);
        } else if (evaluation.equals(2)) {
        	Map<String, int[] > totalMap = new HashMap<>();
        	for (int i = 0; i < 5; i++) {
        		logger1.info(user.getId()+"@第"+(i+1)+"组开始");
        		if (trainTaggedFiles != null && trainTaggedFiles.size() > 0) {
                	String trainFolder = trainPath + "/" + corpusId;
                	FileUtil.createFolder(trainFolder);
                	String testFolder = testPath + "/" + corpusId;
    	        	FileUtil.createFolder(testFolder);
                	for (int j = 0; j < trainTaggedFiles.size(); j++) {
                		CorpusTaggedFile taggedFile = trainTaggedFiles.get(j);
                		if (j%5 == i) {
                			String fileName = trainFolder + "/" + taggedFile.getFileName() + "_" + taggedFile.getModifiedBy() + ".xmi";
                    		FileUtil.createFile(fileName, taggedFile.getXmiContent());
                    		File file = new File(fileName);
                    		trainFiles.add(file);
                		} else {
                			String fileName = testFolder + "/" + taggedFile.getFileName() + "_" + taggedFile.getModifiedBy() + ".xmi";
                    		FileUtil.createFile(fileName, taggedFile.getXmiContent());
                    		File file = new File(fileName);
                    		testFiles.add(file);
                		}              		
                	}
                	Map<String, int[] > evaluateMap = process(model, user.getId(), trainFiles, testFiles, String.valueOf(i));
                	for (Map.Entry<String, int[]> entry : evaluateMap.entrySet()) {
                		if (totalMap.containsKey(entry.getKey())) {
                			int[] value = entry.getValue();
                			int[] totalValue = totalMap.get(entry.getKey());
                			for (int j = 0; j < value.length; j++) {
                				totalValue[j] = (totalMap.get(entry.getKey())[j]+value[j]);
							}
                			totalMap.put(entry.getKey(), totalValue);
                		} else {
                			totalMap.put(entry.getKey(), entry.getValue());
                		}
                	}
                	trainFiles.clear();
                	testFiles.clear();
                	FileUtil.delAllFile(trainFolder);
                	FileUtil.delAllFile(testFolder);
                }
			}
        	
			
        	try {
        		File trainingLogFile = new File( modelPath + "/" + corpusId + "/" + model.getModelName() + "/" + "training.log" );
        		FileWriter writerLog = new FileWriter( trainingLogFile , true);
				for( String key : totalMap.keySet() ) {
		            String log = totalMap.get( key )[0]/5 + "\t" + totalMap.get( key )[1]/5 + "\t" + totalMap.get( key )[2]/5 
		                    + "\t" + String.format( "%.3f", ((float) totalMap.get(key)[0]/5)/(totalMap.get(key)[1]/5) )
		                    + "\t" + String.format( "%.3f", ((float) totalMap.get(key)[0]/5)/(totalMap.get(key)[2]/5) )
		                    + "\t" + String.format( "%.3f", 2.0 * (totalMap.get(key)[0]/5) / ( (totalMap.get(key)[2]/5) + (totalMap.get(key)[1]/5) ) ) + "\t" + key;
		            logger1.info(user.getId()+"@"+log);
		            writerLog.write(log);
		        }
				if (writerLog != null)
	    			writerLog.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	if (trainTaggedFiles != null && trainTaggedFiles.size() > 0) {
            	String trainFolder = trainPath + "/" + corpusId;
            	FileUtil.createFolder(trainFolder);

            	for (int j = 0; j < trainTaggedFiles.size(); j++) {
            		CorpusTaggedFile taggedFile = trainTaggedFiles.get(j);
        			String fileName = trainFolder + "/" + taggedFile.getFileName() + "_" + taggedFile.getModifiedBy() + ".xmi";
            		FileUtil.createFile(fileName, taggedFile.getXmiContent());
            		File file = new File(fileName);
            		trainFiles.add(file);            		
            	}
            	process(model, user.getId(), trainFiles, null, "");
            	trainFiles.clear();
            	FileUtil.delAllFile(trainFolder);
            }
        	
        } else {
        	throw new RuntimeException();
        }
        logger1.info(user.getId()+"@训练结束");
        return;
	}
	
	public Map<String, int[] > process(CorpusModel model, Long userId, List<File> trainFiles, List<File> testFiles, String group) {
		String corpusModelPath = modelPath + "/" + model.getCorpusId(); 
		FileUtil.createFolder(corpusModelPath);
		corpusModelPath = corpusModelPath + "/" + model.getModelName();
		FileUtil.createFolder(corpusModelPath);
		
		Vector<NERFeatureExtractor> feaExtractors = new Vector<>();
        feaExtractors.add(ChiCharNgramFeature.getDefault());
        NERTrainer.feaExtractors = feaExtractors;

        File featureFile = new File(corpusModelPath + "/feature"+group+".txt");
    	
    	File modelFile = new File(corpusModelPath + "/model"+group+".bin");
    	
    	File trainingLogFile = new File(corpusModelPath + "/training"+group+".log");
    	
    	Map<String, int[] > evaluateMap = new HashMap<>();
    	try {
			featureFile.createNewFile();
			modelFile.createNewFile();
			trainingLogFile.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	if (trainFiles != null && trainFiles.size() > 0) {
	        try {
	        	NERTrainer.train( trainFiles, featureFile, modelFile, group, userId );
			} catch (IOException | UIMAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	if (testFiles != null && testFiles.size() > 0) {
	        try {
	        	evaluateMap = NERTrainer.predict( testFiles, modelFile, group, userId);
			} catch (IOException | UIMAException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
        
    	try {
        	Vector<NERFeatureConf> featureConfs = new Vector<>();
			NERFeatureConf conf = NERFeatureConf.fromFile( new File( "workspace/ComponentLibrary/ML_components/NER_feature_extractor/Chinese_Ngram_feature/config.conf" ) );
			featureConfs.add(conf);
        	
        	if (StringUtils.isEmpty(group)) {
				File jarFile = new File(corpusModelPath + "/model.jar");
				jarFile.createNewFile();
				ConfigUtil.exportNERModel(modelFile, featureConfs, jarFile);
				model.setJarFile(FileUtil.readFileByStream(corpusModelPath + "/model.jar"));
				model.setTrainingLog(FileUtil.readFileByChars(corpusModelPath + "/training.log"));
				corpusModelDao.updateModelJarFileByModelId(model);
        	}
			
        	if (testFiles != null && testFiles.size() > 0) {
	        	if (testFiles != null && testFiles.size() > 0) {
	        		for (int i = 0; i < testFiles.size(); i++) {
	        			File file = testFiles.get(i);
	        			String fileName = file.getName();
	                    String name = fileName.substring(0, fileName.lastIndexOf("."));
	        			ModelOutput outputFile = new ModelOutput();
	        			outputFile.setCorpusId(model.getCorpusId());
	        			outputFile.setCreatedBy(String.valueOf(userId));
	        			outputFile.setCreatedTime(new Date());
	        			outputFile.setEncoder("UTF-8");
	        			outputFile.setFileName(name+("".equals(group)?"":"_"+group));
	        			outputFile.setFileType("xmi");
	        			outputFile.setModelId(model.getModelId());
	        			outputFile.setXmiContent(FileUtil.readFileByChars(corpusModelPath + "/output/" + name+("".equals(group)?"":"_"+group) + ".xmi"));
	        			corpusModelDao.insertModelOutput(outputFile);
	        		}
	        	}
        	}
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return evaluateMap;
        
	}

}
