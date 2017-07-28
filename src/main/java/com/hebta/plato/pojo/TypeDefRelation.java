package com.hebta.plato.pojo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.hebta.plato.dto.TypeDefRelationArg;

public class TypeDefRelation {
	private static final String ARG_ROLE_FROM = "from";
	private static final String ARG_ROLE_TO = "to";
	
    private Long id;    
    private Long projectId;
    private String type;
    private String labelsStr;
    private String dashArray;
    private String color;
    private String fromTargets;
    private String toTargets;
    
    private List<String> labels;
    private List<TypeDefRelationArg> args = new ArrayList<>();
    
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
	public String getLabelsStr() {
		return labelsStr;
	}
	public void setLabelsStr(String labelsStr) {
		List<String> list = buildList(labelsStr);
		this.labels = list;
		this.labelsStr = labelsStr;
	}
	private List<String> buildList(String labelsStr) {
		String[] arr = labelsStr.split(",");
		List<String> list = new ArrayList<>();
		for (String elem : arr){
			list.add(elem);
		}
		return list;
	}
	public String getDashArray() {
		return dashArray;
	}
	public void setDashArray(String dashArray) {
		this.dashArray = dashArray;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public String getFromTargets() {
		return fromTargets;
	}
	public void setFromTargets(String fromTargets) {
		List<String> list = buildList(fromTargets);
		this.args.add(new TypeDefRelationArg("from", list));
		this.fromTargets = fromTargets;
	}
	public String getToTargets() {
		return toTargets;
	}
	public void setToTargets(String toTargets) {
		List<String> list = buildList(toTargets);
		this.args.add(new TypeDefRelationArg("to", list));
		this.toTargets = toTargets;
	}
	public List<String> getLabels() {
		return labels;
	}
	public void setLabels(List<String> labels) {
		this.labelsStr = StringUtils.join(labels, ",");
		this.labels = labels;
	}
	public List<TypeDefRelationArg> getArgs() {
		return args;
	}
	public void setArgs(List<TypeDefRelationArg> args) {
		if (args.size() != 2){
			throw new RuntimeException("参数 args 必须包含 2 个元素，一个表示 from, 一个表示 to");
		}
		for (TypeDefRelationArg arg : args){
			if (ARG_ROLE_FROM.equalsIgnoreCase(arg.getRole())){
				this.fromTargets = StringUtils.join(arg.getTargets(), ",");
			} else if (ARG_ROLE_TO.equalsIgnoreCase(arg.getRole())){
				this.toTargets = StringUtils.join(arg.getTargets(), ",");
			}
		}
		this.args = args;
	}
}

