package com.hebta.plato.pojo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NLPProcessor {
	private String conf;

	public String getConf() {
		return conf;
	}

	@XmlAttribute
	public void setConf(String conf) {
		this.conf = conf;
	}	
}
