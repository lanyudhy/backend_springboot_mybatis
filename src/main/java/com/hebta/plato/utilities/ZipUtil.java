package com.hebta.plato.utilities;
 
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
 
public final class ZipUtil {
 
	 /** 压缩单个文件*/
    public static void ZipFile(String filepath ,String zippath) {
    	try {
            File file = new File(filepath);
            File zipFile = new File(zippath);
            InputStream input = new FileInputStream(file);
            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
            zipOut.putNextEntry(new ZipEntry(file.getName()));
            int temp = 0;
            while((temp = input.read()) != -1){
                zipOut.write(temp);
            }
            input.close();
            zipOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    /** 一次性压缩多个文件，文件存放至一个文件夹中*/
    public static void ZipMultiFile(String filepath ,String zippath) {
		try {
	        File file = new File(filepath);// 要被压缩的文件夹
	        File zipFile = new File(zippath);
	        InputStream input = null;
	        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
	        if(file.isDirectory()){
	            File[] files = file.listFiles();
	            for(int i = 0; i < files.length; ++i){
	                input = new FileInputStream(files[i]);
	                zipOut.putNextEntry(new ZipEntry(file.getName() + File.separator + files[i].getName()));
	                int temp = 0;
	                while((temp = input.read()) != -1){
	                    zipOut.write(temp);
	                }
	                input.close();
	            }
	        }
	        zipOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    /** 一次性压缩多个文件，文件存放至一个文件夹中*/
    public static void ZipMultiFileWithFiles(File[] files, String zippath) {
		try {
	        File zipFile = new File(zippath);
	        InputStream input = null;
	        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
            for(int i = 0; i < files.length; ++i){
                input = new FileInputStream(files[i]);
                zipOut.putNextEntry(new ZipEntry("download" + File.separator + files[i].getName()));
                int temp = 0;
                while((temp = input.read()) != -1){
                    zipOut.write(temp);
                }
                input.close();
            }
	        zipOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    /**  解压缩（解压缩单个文件）*/
    public static void ZipContraFile(String zippath ,String outfilepath ,String filename) {
		try {
	        File file = new File(zippath);//压缩文件路径和文件名
	        File outFile = new File(outfilepath);//解压后路径和文件名
	        ZipFile zipFile = new ZipFile(file);
	        ZipEntry entry = zipFile.getEntry(filename);//所解压的文件名
	        InputStream input = zipFile.getInputStream(entry);
	        OutputStream output = new FileOutputStream(outFile);
	        int temp = 0;
	        while((temp = input.read()) != -1){
	            output.write(temp);
	        }
	        input.close();
	        output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    /**  解压缩（压缩文件中包含多个文件）可代替上面的方法使用。
     * ZipInputStream类
     * 当我们需要解压缩多个文件的时候，ZipEntry就无法使用了，
     * 如果想操作更加复杂的压缩文件，我们就必须使用ZipInputStream类
     * */
    public static void ZipContraMultiFile(String zippath, String outzippath){
    	try {
            File file = new File(zippath);
            File outFile = null;
            ZipFile zipFile = new ZipFile(file);
            ZipInputStream zipInput = new ZipInputStream(new FileInputStream(file));
            ZipEntry entry = null;
            InputStream input = null;
            OutputStream output = null;
            while((entry = zipInput.getNextEntry()) != null){
                System.out.println("解压缩" + entry.getName() + "文件");
                outFile = new File(outzippath + File.separator + entry.getName());
                if(!outFile.getParentFile().exists()){
                    outFile.getParentFile().mkdir();
                }
                if(!outFile.exists()){
                    outFile.createNewFile();
                }
                input = zipFile.getInputStream(entry);
                output = new FileOutputStream(outFile);
                int temp = 0;
                while((temp = input.read()) != -1){
                    output.write(temp);
                }
                
                input.close();
                output.close();
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public static Map<String, String> ZipContraMultiFile(String zipPath){
    	Map<String, String> map = new HashMap<>();
    	try {
            File file = new File(zipPath);
//            File outFile = null;
            ZipFile zipFile = new ZipFile(file);
            ZipInputStream zipInput = new ZipInputStream(new FileInputStream(file));
            ZipEntry entry = null;
            InputStream input = null;
//            OutputStream output = null;
            
            while((entry = zipInput.getNextEntry()) != null){
//                System.out.println("解压缩" + entry.getName() + "文件");
//                outFile = new File(outzippath + File.separator + entry.getName());
//                if(!outFile.getParentFile().exists()){
//                    outFile.getParentFile().mkdir();
//                }
//                if(!outFile.exists()){
//                    outFile.createNewFile();
//                }
                input = zipFile.getInputStream(entry);
                String content = IOUtils.toString(input, "UTF-8");
                map.put(entry.getName(), content);
                input.close();
            }
            
            zipInput.close();
            zipFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

        return map;
    }
    
    /**  解压缩（压缩文件中包含多个文件）可代替上面的方法使用。
     * ZipInputStream类
     * 当我们需要解压缩多个文件的时候，ZipEntry就无法使用了，
     * 如果想操作更加复杂的压缩文件，我们就必须使用ZipInputStream类
     * */
    public static Map<String, String> ZipContraMultiFileWithReturn(String zipPath, String outzippath){
    	Map<String, String> map = new HashMap<>();
    	try {
            File file = new File(zipPath);
            File outFile = null;
            ZipFile zipFile = new ZipFile(file);
            ZipInputStream zipInput = new ZipInputStream(new FileInputStream(file));
            ZipEntry entry = null;
            InputStream input = null;
            OutputStream output = null;
            while((entry = zipInput.getNextEntry()) != null){
            	input = zipFile.getInputStream(entry);
            	
            	if (entry.getName().endsWith(".xmi")) {
            		String content = IOUtils.toString(input, "UTF-8");
            		map.put(entry.getName(), content);
            	} else {
            		System.out.println("解压缩" + entry.getName() + "文件");
                    outFile = new File(outzippath + File.separator + entry.getName());
                    if(!outFile.getParentFile().exists()){
                        outFile.getParentFile().mkdir();
                    }
                    if(!outFile.exists()){
                        outFile.createNewFile();
                    }
                    
                    output = new FileOutputStream(outFile);
                    int temp = 0;
                    while((temp = input.read()) != -1){
                        output.write(temp);
                    }
                    output.close();
                    map.put(entry.getName(), outFile.getPath());
            	}

                input.close();
                
            }
            zipInput.close();
            zipFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

        return map;
    }
    
    public static void main(String[] args){
    	String a = "aaa.txt.txt";
    	String c = a.substring(0, a.lastIndexOf("."));
    	System.out.println(c);
    }
}