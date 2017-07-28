package com.hebta.plato.dto;

import java.util.List;
import java.util.Map;

import com.hebta.plato.pojo.CorpusTaggedFile;

public class TaggedFileReturn{
	private  String bratFile;
	
	private String bratSem;
	
	private String tokenBratFile;
	
	private String tokenBratSem;
	
	private String sentBratFile;
	
	private String sentBratSem;
	
	private TypeDefXml typeDefXml;
	
	private String outputTxt;
	
	private Map<String, String> pieCharts;

	public String getBratFile() {
		return bratFile;
	}

	public void setBratFile(String bratFile) {
		this.bratFile = bratFile;
	}

	public String getBratSem() {
		return bratSem;
	}

	public void setBratSem(String bratSem) {
		this.bratSem = bratSem;
	}

	public String getTokenBratFile() {
		return tokenBratFile;
	}

	public void setTokenBratFile(String tokenBratFile) {
		this.tokenBratFile = tokenBratFile;
	}

	public String getTokenBratSem() {
		return tokenBratSem;
	}

	public void setTokenBratSem(String tokenBratSem) {
		this.tokenBratSem = tokenBratSem;
	}

	public String getSentBratFile() {
		return sentBratFile;
	}

	public void setSentBratFile(String sentBratFile) {
		this.sentBratFile = sentBratFile;
	}

	public String getSentBratSem() {
		return sentBratSem;
	}

	public void setSentBratSem(String sentBratSem) {
		this.sentBratSem = sentBratSem;
	}

	public TypeDefXml getTypeDefXml() {
		return typeDefXml;
	}

	public void setTypeDefXml(TypeDefXml typeDefXml) {
		this.typeDefXml = typeDefXml;
	}

	public String getOutputTxt() {
		return outputTxt;
	}

	public void setOutputTxt(String outputTxt) {
		this.outputTxt = outputTxt;
	}

	public Map<String, String> getPieCharts() {
		return pieCharts;
	}

	public void setPieCharts(Map<String, String> pieCharts) {
		this.pieCharts = pieCharts;
	}

}