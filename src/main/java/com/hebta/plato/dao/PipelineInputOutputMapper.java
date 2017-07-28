package com.hebta.plato.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.hebta.plato.dto.FileNameToType;
import com.hebta.plato.pojo.PipelineInput;
import com.hebta.plato.pojo.PipelineOutput;

public interface PipelineInputOutputMapper {
    int insertPipelineInput(PipelineInput input);
    
    int insertPipelineOutput(PipelineOutput output);
    
    List<PipelineInput> selectPipelineInputByPipeLineId(Long pipelineId);
    
    List<PipelineOutput> selectPipelineOutputByPipeLineId(Long pipelineId);
    
    PipelineInput selectPipelineInputByPrimaryKey(Long fileId);
    
    PipelineOutput selectPipelineOutputByPrimaryKey(Long fileId);
    
    int deletePipelineInputByPipelineId(Long pipelineId);
    
    int deletePipelineOutputByPipelineId(Long pipelineId);
    
    int deletePipelineInputByFileId(Long fileId);
    
    int deletePipelineOutputByFileId(Long fileId);
    
    List<FileNameToType> selectPipelineInputWithPagination(@Param("pipelineId") Long pipelineId, @Param("page") Integer page, @Param("countPerPage") Integer countPerPage);
    
    List<FileNameToType> selectPipelineOutputWithPagination(@Param("pipelineId") Long pipelineId, @Param("page") Integer page, @Param("countPerPage") Integer countPerPage);
    
    Integer selectTotalCountInputByPipelineId(Long pipelineId);
    
    Integer selectTotalCountOutputByPipelineId(Long pipelineId);
    
    List<PipelineOutput> selectOutputFileByFileIds(List<Long> fileIds);
}