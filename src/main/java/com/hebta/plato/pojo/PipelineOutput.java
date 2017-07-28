package com.hebta.plato.pojo;

import java.util.Date;

public class PipelineOutput{
	private Long fileId;
	private Long pipelineId;
	private String fileName;
	private String fileType;
	private String encoder;
	private String xmiContent;
	private Date createdTime;
	private String createdBy;
	private Long inputFileId;
	private String tokenContent;
	private String oriContent;
	private String txtContent;
	
	public Long getFileId() {
		return fileId;
	}
	public void setFileId(Long fileId) {
		this.fileId = fileId;
	}
	public Long getPipelineId() {
		return pipelineId;
	}
	public void setPipelineId(Long pipelineId) {
		this.pipelineId = pipelineId;
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
	public Long getInputFileId() {
		return inputFileId;
	}
	public void setInputFileId(Long inputFileId) {
		this.inputFileId = inputFileId;
	}
	public String getTokenContent() {
		return tokenContent;
	}
	public void setTokenContent(String tokenContent) {
		this.tokenContent = tokenContent;
	}
	public String getOriContent() {
		return oriContent;
	}
	public void setOriContent(String oriContent) {
		this.oriContent = oriContent;
	}
	public String getTxtContent() {
		return txtContent;
	}
	public void setTxtContent(String txtContent) {
		this.txtContent = txtContent;
	}
}