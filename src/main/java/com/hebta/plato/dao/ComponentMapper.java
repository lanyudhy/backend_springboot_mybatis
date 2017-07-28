package com.hebta.plato.dao;

import java.util.List;

import com.hebta.plato.pojo.Component;
import com.hebta.plato.pojo.ComponentGroup;
import com.hebta.plato.pojo.ComponentResource;

public interface ComponentMapper {
    int insertComponentGroup(ComponentGroup componentGroup);

    int insertComponent(Component component);
    
    int insertComponentResource(ComponentResource componentResource);
    
    int insertComponentLib(Component component);
    
    int insertComponentResourceLib(ComponentResource componentResource);
    
    int batchInsertComponentResource(List<ComponentResource> list);
    
    ComponentGroup selectComponentGroupByPrimaryKey(Long groupId);
    
    Component selectComponentByPrimaryKey(Long componentId);
    
    ComponentResource selectComponentResourceByPrimaryKey(Long resourceId);
    
    List<Component> selectComponentByGroupId(Long groupId);
    
    List<ComponentResource> selectComponentResourceByComponentId(Long componentId);
    
    Component selectComponentLibByPrimaryKey(Long componentId);
    
    ComponentResource selectComponentResourceLibByPrimaryKey(Long resourceId);
    
    List<ComponentResource> selectComponentResourceLibByComponentId(Long componentId);
    
    int updateComponentGroupByPrimaryKey(ComponentGroup componentGroup);

    int updateComponentByPrimaryKey(Component component);
    
    int updateComponentResourceByPrimaryKey(ComponentResource componentResource);
    
    int updateComponentResourceLibByPrimaryKey(ComponentResource componentResource);
    
    List<Component> selectAllComponentLib();
    
    List<Component> selectComponentByPipelineId(Long pipelineId);
    
    Integer selectMaxSeqByPipelineId(Long pipelineId);
    
    int deleteComponentResourceByPipelineId(Long pipelineId);
    
    int deleteComponentByPipelineId(Long pipelineId);
    
    int deleteComponentResourceByComponentId(Long componentId);
    
    int deleteComponentByComponentId(Long componentId);
    
    int deleteComponentResourceByResourceId(Long resourceId);
    
    List<Long> selectParentIdByComponentLibId(Long componentLibId);
}