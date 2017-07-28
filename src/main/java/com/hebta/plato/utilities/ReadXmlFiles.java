package com.hebta.plato.utilities;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class ReadXmlFiles {

	private static final Logger LOGGER = Logger.getLogger(ReadXmlFiles.class.getName());

	public ArrayList<String> readXmlPath(String xmlPath) throws IOException {
		ArrayList<String> paths = new ArrayList<String>();
		File file = new File(xmlPath);
		if (file.isDirectory()) {
			String files[] = file.list();
			for (int i = 0; i < files.length; i++) {
				File f = new File(xmlPath + "/" + files[i]);
				if (f.isDirectory()) {
					paths.addAll(readXmlPath(f.getPath()));
				} else {
					paths.add(f.getPath());
				}
			}
		} else {
			LOGGER.info(xmlPath);
		}
		// LOGGER.info(paths.toString());
		return paths;
	}

	public String updateXmlFile(String path, String replaceStr){
		
		String xmlType = "";
		SAXReader reader = new SAXReader();
        Document document;
		try {
			document = reader.read(path);
		         
	        Element root = document.getRootElement();
	
	        @SuppressWarnings("unchecked")
			List<Element> list = root.elements() ;
	        for (Element node : list){
	        	if(node.getName().equals("params")){
	        		List<Element> list2 = node.elements();
	        		if (list2.size() == 1) {
	        			list2.get(0).setText(replaceStr);
	        		} else if (list2.size() > 1) {
	        			for (Element e : list2) {
	        				if (e.getName().equals("dictFile")) {
	        					e.setText(replaceStr);
	        				}
	        			}
	        		}
	        	}
	        }
	        
	        return document.asXML();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	}	
	
	public String getXmlFileParam(String path){
		
		String xmlType = "";
		SAXReader reader = new SAXReader();
        Document document;
		try {
			document = reader.read(path);
		         
	        Element root = document.getRootElement();
	
	        @SuppressWarnings("unchecked")
			List<Element> list = root.elements() ;
	        for (Element node : list){
	        	if(node.getName().equals("params")){
	        		List<Element> list2 = node.elements();
	        		if (list2.size() == 1) {
	        			return list2.get(0).getText();
	        		} else if (list2.size() > 1) {
	        			for (Element e : list2) {
	        				if (e.getName().equals("dictFile")) {
	        					return e.getText();
	        				}
	        			}
	        		}
	        	}
	        }
	        return null;
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		ReadXmlFiles r = new ReadXmlFiles();
		System.out.println(r.updateXmlFile("D:/config.conf","dddd"));
	}

}
