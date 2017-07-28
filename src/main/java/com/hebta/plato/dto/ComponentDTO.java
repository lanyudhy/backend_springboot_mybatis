package com.hebta.plato.dto;

import java.util.List;

import com.hebta.plato.pojo.Component;
import com.hebta.plato.pojo.ComponentResource;

public class ComponentDTO extends Component{
	private List<ComponentResource> resourceList;

	public List<ComponentResource> getResourceList() {
		return resourceList;
	}

	public void setResourceList(List<ComponentResource> resourceList) {
		this.resourceList = resourceList;
	}

}