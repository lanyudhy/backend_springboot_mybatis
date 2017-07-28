package com.hebta.plato.dto;

import java.util.ArrayList;
import java.util.List;

import com.hebta.plato.pojo.TypeDefEntity;
import com.hebta.plato.pojo.TypeDefRelation;

public class TypeDefXml {
	private List<TypeDefEntity> entity_types = new ArrayList<>();
	
	private List<TypeDefRelation> relation_types = new ArrayList<>();

	public List<TypeDefEntity> getEntity_types() {
		return entity_types;
	}

	public void setEntity_types(List<TypeDefEntity> entity_types) {
		this.entity_types = entity_types;
	}

	public List<TypeDefRelation> getRelation_types() {
		return relation_types;
	}

	public void setRelation_types(List<TypeDefRelation> relation_types) {
		this.relation_types = relation_types;
	}
}
