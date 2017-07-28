package com.hebta.plato.pojo;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class TypeDefEntity {
    private Long id;

    private String type;
    
    private List<String> labels;
    
    private String labelsStr;
    
    private String bgColor;
    
    private String borderColor;
    
    private Long projectId;

    public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<String> getLabels() {
		return labels;
	}

	public void setLabels(List<String> labels) {
		this.labelsStr = StringUtils.join(labels, ",");
		this.labels = labels;
	}

	public String getLabelsStr() {
		return labelsStr;
	}

	public void setLabelsStr(String labelsStr) {
		this.labels = Arrays.asList(labelsStr.split(","));
		this.labelsStr = labelsStr;
	}

	public String getBgColor() {
		return bgColor;
	}

	public void setBgColor(String bgColor) {
		this.bgColor = bgColor;
	}

	public String getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(String borderColor) {
		this.borderColor = borderColor;
	}
}