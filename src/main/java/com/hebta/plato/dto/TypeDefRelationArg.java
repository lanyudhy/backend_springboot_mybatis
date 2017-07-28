package com.hebta.plato.dto;

import java.util.ArrayList;
import java.util.List;

public class TypeDefRelationArg {
	private String role;
	private List<String> targets = new ArrayList<>();
		
	public TypeDefRelationArg() {
		super();
	}
	public TypeDefRelationArg(String role, List<String> targets) {
		super();
		this.role = role;
		this.targets = targets;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public List<String> getTargets() {
		return targets;
	}
	public void setTargets(List<String> targets) {
		this.targets = targets;
	}
}
