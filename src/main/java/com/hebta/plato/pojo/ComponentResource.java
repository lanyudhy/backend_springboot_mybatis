package com.hebta.plato.pojo;

import java.util.Date;

public class ComponentResource {
	private Long resourceId;
	private String resourceName;
	private Long componentId;
	private String type;
	private byte[] content;
    private Long editable;
    private Long pipelineId;
    private Date createdTime;
    private String createdBy;
    private String contentStr;
    private Integer confSelected;
    
	public Long getResourceId() {
		return resourceId;
	}
	public void setResourceId(Long resourceId) {
		this.resourceId = resourceId;
	}
	public String getResourceName() {
		return resourceName;
	}
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	public Long getComponentId() {
		return componentId;
	}
	public void setComponentId(Long componentId) {
		this.componentId = componentId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public byte[] getContent() {
		return content;
	}
	public void setContent(byte[] content) {
		this.content = content;
	}
	public Long getEditable() {
		return editable;
	}
	public void setEditable(Long editable) {
		this.editable = editable;
	}
	public Long getPipelineId() {
		return pipelineId;
	}
	public void setPipelineId(Long pipelineId) {
		this.pipelineId = pipelineId;
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
	public String getContentStr() {
		return contentStr;
	}
	public void setContentStr(String contentStr) {
		this.contentStr = contentStr;
	}
	public Integer getConfSelected() {
		return confSelected;
	}
	public void setConfSelected(Integer confSelected) {
		this.confSelected = confSelected;
	}
}