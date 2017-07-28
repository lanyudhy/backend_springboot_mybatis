package com.hebta.plato.pojo;

import java.util.Date;

public class CorpusModel {
	private Long modelId;
	private String modelName;
	private byte[] jarFile;
	private String trainingLog;
	private Long corpusId;
	private Date createdTime;
	private String createdBy;
	
	public Long getModelId() {
		return modelId;
	}
	public void setModelId(Long modelId) {
		this.modelId = modelId;
	}
	public String getModelName() {
		return modelName;
	}
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	public byte[] getJarFile() {
		return jarFile;
	}
	public void setJarFile(byte[] jarFile) {
		this.jarFile = jarFile;
	}
	public String getTrainingLog() {
		return trainingLog;
	}
	public void setTrainingLog(String trainingLog) {
		this.trainingLog = trainingLog;
	}
	public Long getCorpusId() {
		return corpusId;
	}
	public void setCorpusId(Long corpusId) {
		this.corpusId = corpusId;
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