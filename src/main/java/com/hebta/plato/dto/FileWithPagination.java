package com.hebta.plato.dto;

import java.util.List;

public class FileWithPagination{
	private List<FileNameToType> fileFullNameList;
	private Integer currentPage;
	private Integer totalPage;
	
	public List<FileNameToType> getFileFullNameList() {
		return fileFullNameList;
	}
	public void setFileFullNameList(List<FileNameToType> fileFullNameList) {
		this.fileFullNameList = fileFullNameList;
	}
	public Integer getCurrentPage() {
		return currentPage;
	}
	public void setCurrentPage(Integer currentPage) {
		this.currentPage = currentPage;
	}
	public Integer getTotalPage() {
		return totalPage;
	}
	public void setTotalPage(Integer totalPage) {
		this.totalPage = totalPage;
	}
	
	
}