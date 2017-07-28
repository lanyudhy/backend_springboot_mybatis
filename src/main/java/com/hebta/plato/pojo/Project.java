package com.hebta.plato.pojo;

import java.util.List;

public class Project {
    private Long id;

    private String name;

    private Integer type;
    
    // 这个字段是用户选择创建 Corpus, Pipeline或者两者都创建的标识
    private List<Integer> pairType;

    private String description;

    private String createdTime;
    
    private String projectOwner;
    
    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public List<Integer> getPairType() {
		return pairType;
	}

	public void setPairType(List<Integer> pairType) {
		this.pairType = pairType;
	}

	public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime.substring(0, 16);
    }

	public String getProjectOwner() {
		return projectOwner;
	}

	public void setProjectOwner(String projectOwner) {
		this.projectOwner = projectOwner;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
}