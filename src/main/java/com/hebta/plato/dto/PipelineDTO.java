package com.hebta.plato.dto;

import java.util.List;

public class PipelineDTO{
	private Long pipelineId;
	private List<ComponentDTO> componentDtoList;
	
	public Long getPipelineId() {
		return pipelineId;
	}
	public void setPipelineId(Long pipelineId) {
		this.pipelineId = pipelineId;
	}
	public List<ComponentDTO> getComponentDtoList() {
		return componentDtoList;
	}
	public void setComponentDtoList(List<ComponentDTO> componentDtoList) {
		this.componentDtoList = componentDtoList;
	}

	
}