package com.hebta.plato.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.hebta.plato.dto.FileNameToType;
import com.hebta.plato.pojo.CorpusModel;
import com.hebta.plato.pojo.ModelOutput;

public interface CorpusModelMapper {
	int insertCorpusModel(CorpusModel model);
	
	int insertModelOutput(ModelOutput output);
	
	List<CorpusModel> selectCorpusModelByCorpusId(Long corpusId);
	
	List<ModelOutput> selectModelOutputByModelId(Long modelId);
	
	CorpusModel selectCorpusModelByPrimaryKey(Long modelId);
	
	ModelOutput selectModelOutputByPrimaryKey(Long fileId);
	
	List<FileNameToType> selectModelOutputWithPagination(@Param("modelId") Long modelId, @Param("page") Integer page, @Param("countPerPage") Integer countPerPage);
	
	int deleteCorpusModelByModelId(Long modelId);
	
	int deleteCorpusModelByCorpusId(Long corpusId);
	
	int deleteModelOutputByFileId(Long fileId);
	
	int deleteModelOutputByModelId(Long modelId);
	
	int deleteModelOutputByCorpusId(Long corpusId);
	
	int deleteModelOutputFiles(List<Long> fileIds);
	
	List<ModelOutput> selectModelOutputByFileIds(List<Long> fileId);
	
	Integer selectTotalCountModelOutputByModelId(Long modelId);
	
	int updateModelJarFileByModelId(CorpusModel model);
}