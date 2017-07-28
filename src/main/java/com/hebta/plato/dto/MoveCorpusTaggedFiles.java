package com.hebta.plato.dto;

import java.util.List;

public class MoveCorpusTaggedFiles{
	private List<Long> fileIds;
	private String action;
	private String from;
	private String to;
	
	public List<Long> getFileIds() {
		return fileIds;
	}
	public void setFileIds(List<Long> fileIds) {
		this.fileIds = fileIds;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	
}