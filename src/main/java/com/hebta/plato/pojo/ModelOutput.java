package com.hebta.plato.pojo;

import java.util.Date;

public class ModelOutput{
	private Long fileId;
	private Long modelId;
	private Long corpusId;
	private String fileName;
	private String fileType;
	private String encoder;
	private String xmiContent;
	private Date createdTime;
	private String createdBy;
	
	public Long getFileId() {
		return fileId;
	}
	public void setFileId(Long fileId) {
		this.fileId = fileId;
	}
	public Long getModelId() {
		return modelId;
	}
	public void setModelId(Long modelId) {
		this.modelId = modelId;
	}
	public Long getCorpusId() {
		return corpusId;
	}
	public void setCorpusId(Long corpusId) {
		this.corpusId = corpusId;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public String getEncoder() {
		return encoder;
	}
	public void setEncoder(String encoder) {
		this.encoder = encoder;
	}
	public String getXmiContent() {
		return xmiContent;
	}
	public void setXmiContent(String xmiContent) {
		this.xmiContent = xmiContent;
	}
	public Date getCreatedTime() {
		return createdTime;
	}
	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	
	
}