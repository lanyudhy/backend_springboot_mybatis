package com.hebta.plato.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.hebta.plato.dto.FileNameToType;
import com.hebta.plato.pojo.CorpusTaggedFile;

public interface CorpusTaggedFileMapper {
    int insertCorpusTaggedFile(CorpusTaggedFile file);
    
    List<CorpusTaggedFile> selectCorpusTaggedFileByCorpusId(@Param("corpusId") Long corpusId, @Param("folder") String folder);
    
    CorpusTaggedFile selectCorpusTaggedFileByPrimaryKey(@Param("fileId") Long fileId);
    
    int deleteCorpusTaggedFileByCorpusId(@Param("corpusId") Long corpusId, @Param("folder") String folder, @Param("userId") String userId);
    
    int deleteCorpusTaggedFileByFileId(@Param("fileId") Long fileId, @Param("folder") String folder);
    
    List<FileNameToType> selectTaggedFiletWithPagination(@Param("corpusId") Long corpusId, @Param("page") Integer page, @Param("countPerPage") Integer countPerPage, @Param("folder") String folder, @Param("userName") String userName);
    
    Integer selectTotalCountTaggedFileByCorpusId(@Param("corpusId") Long corpusId, @Param("folder") String folder, @Param("userName") String userName);
    
    List<FileNameToType> selectTaggedFiletWithPaginationByOwner(@Param("corpusId") Long corpusId, @Param("page") Integer page, @Param("countPerPage") Integer countPerPage, @Param("folder") String folder, @Param("userName") String userName);
    
    Integer selectTotalCountTaggedFileByCorpusOwner(@Param("corpusId") Long corpusId, @Param("folder") String folder, @Param("userName") String userName);
    
    List<CorpusTaggedFile> selectCorpusTaggedFileByFileIds(@Param("list") List<Long> list, @Param("folder") String folder);
    
    int deleteCorpusTaggedFileByFileIds(@Param("list") List<Long> list, @Param("folder") String folder);
    
    int updateCorpusTaggedFile(CorpusTaggedFile file);
    
    List<CorpusTaggedFile> selectCorpusTaggedFileByFolder(@Param("folder") String folder);
    
    List<CorpusTaggedFile> selectOriFile(@Param("corpusId") Long corpusId, @Param("folder") String folder);

}