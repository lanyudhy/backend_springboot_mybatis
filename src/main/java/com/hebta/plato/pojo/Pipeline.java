package com.hebta.plato.pojo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class Pipeline {
	private Long projectId;
	private String desc;
	private List<NLPProcessor> NLPProcessor;

	public Long getProjectId() {
		return projectId;
	}

	@XmlTransient
	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public String getDesc() {
		return desc;
	}

	@XmlAttribute
	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	public List<NLPProcessor> getNLPProcessor() {
		return NLPProcessor;
	}

	@XmlElement
	public void setNLPProcessor(List<NLPProcessor> NLPProcessor) {
		this.NLPProcessor = NLPProcessor;
	}	
	
	public static void main(String[] args) {
		NLPProcessor nlp = new NLPProcessor();
		nlp.setConf("Sentence detector/DF_Punctuation_sentence_detector/");
		NLPProcessor nlp2 = new NLPProcessor();
		nlp2.setConf("Tokenizer/DF_Tokenize_by_chars/");
		List<NLPProcessor> processors = new ArrayList<>();
		processors.add(nlp);
		processors.add(nlp2);
		
		Pipeline pp = new Pipeline();
		pp.setProjectId(55L);
		pp.setDesc("DESCRIPTION:INPUT:OUTPUT:CATEGORY:");
		pp.setNLPProcessor(processors);
		
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Pipeline.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			
			jaxbMarshaller.marshal(pp, new File("C:/Users/Lenovo/Desktop/1234.pipeline"));
			System.out.println("done");
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
}
