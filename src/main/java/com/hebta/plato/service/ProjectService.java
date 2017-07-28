package com.hebta.plato.service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hebta.plato.dao.CorpusTaggedFileMapper;
import com.hebta.plato.dao.ProjectMapper;
import com.hebta.plato.dao.UserMapper;
import com.hebta.plato.pojo.CorpusTaggedFile;
import com.hebta.plato.pojo.Project;
import com.hebta.plato.pojo.User;
import com.hebta.plato.utilities.SessionUtil;

@Service
public class ProjectService {
	private Logger logger = LoggerFactory.getLogger(ProjectService.class);
	
	@Autowired
	private JdbcTemplate jdbcTemp;
	@Autowired
	private ProjectMapper projectDao;
	@Autowired
	private CorpusTaggedFileMapper taggedDao;
	@Autowired
	private UserMapper userDao;
	
	@Transactional
	public List<Project> saveProject(Project requestProject){
		User user = SessionUtil.getSessionUser();
		logger.info("进入 ProjectService.saveProject()");
		List<Integer> pairType = requestProject.getPairType();
		assert pairType == null : "至少要选择一种项目类型";
		
		List<Project> projects = new ArrayList<>();
		List<Long> savedIds = new ArrayList<>();
		for (Integer type : pairType){
			Project project = new Project();
			String suffix = null;
			if (type.equals(1)){
				suffix = "_CRS";
			} else if (type.equals(2)){
				suffix = "_PLP";
			}
			project.setName(requestProject.getName() + suffix);
			project.setDescription(requestProject.getDescription());
			project.setType(type);
			
			projectDao.insertSelective(project);
			savedIds.add(project.getId());
			
			StringBuffer sb = new StringBuffer();
			sb.append("INSERT INTO USER_PROJECT (USER_ID, PROJECT_ID, PROJECT_OWNER) VALUES (")
				.append(SessionUtil.getSessionUser().getId()).append(", ")
				.append(project.getId()).append(", 1)");
			jdbcTemp.execute(sb.toString());
		}
		logger.info("保存完成，再从数据库取出此次保存的项目信息");
		projects = projectDao.selectByIds(savedIds);
		for (Project p : projects){
			p.setProjectOwner(user.getLoginName());
		}
		return projects;
	}
	
	public List<Project> getMyProjectsByType(Integer type) {
		logger.info("进入 ProjectService.getMyProjectsByType()");
		return projectDao.selectMyProjectsByType(SessionUtil.getSessionUser().getId(), type);
	}

	public List<Project> getSharedCorpusProjects(){
		logger.info("进入 ProjectService.getSharedCorpusProjects()");
		Long userId = SessionUtil.getSessionUser().getId();
		List<Project> projects = projectDao.selectSharedCorpusProjects(userId);
		if (projects == null || projects.size() == 0){
			return null;
		}
		
		return setProjectsOwners(projects);
	}
	
	public List<Project> getAllCorpusProjects(){
		logger.info("进入 ProjectService.getAllCorpusProjects()");
		Long userId = SessionUtil.getSessionUser().getId();
		List<Project> projects = projectDao.selectAllCorpusProjects(userId);
		if (projects == null || projects.size() == 0){
			return null;
		}
		
		return setProjectsOwners(projects);
	}

	private List<Project> setProjectsOwners(List<Project> projects) {
		List<Long> pIds = projects.stream().map(Project::getId).collect(Collectors.toList());
		
		String sql = "SELECT UP.PROJECT_ID, UI.LOGIN_NAME FROM USER_PROJECT UP JOIN USER_INFO UI ON UP.USER_ID = UI.ID WHERE UP.PROJECT_OWNER = '1' AND UP.PROJECT_ID IN (" + StringUtils.join(pIds, ",") + ")";
		Map<Long, String> nameMap = new HashMap<>();
		jdbcTemp.query(sql, (rs) -> {
			nameMap.put(rs.getLong("PROJECT_ID"), rs.getString("LOGIN_NAME"));
		});
		logger.info("开始设置项目所有者信息");
		projects.forEach(project -> {
			project.setProjectOwner(nameMap.get(project.getId()));
		});
		
		return projects;
	}
	
	public Project updateName(Project project) {
		logger.info("进入 ProjectService.updateName()");
		projectDao.updateName(project);	
		return projectDao.selectById(project.getId());
	}
	
	public Project updateStatus(Project project) {
		logger.info("进入 ProjectService.updateStatus()");
		projectDao.updateStatus(project);	
		return projectDao.selectById(project.getId());
	}
	
	public Project getProjectById(Long id) {
		logger.info("进入 ProjectService.getProjectById()");	
		return projectDao.selectById(id);
	}
	
	@Transactional
	public List<Long> batchDelete(String idsStr) {
		logger.info("进入 ProjectService.batchDelete()");
		assert idsStr == null || idsStr.length() == 0 : "没有传入要删除的项目ID";
		
		List<Long> ids = new ArrayList<>();
		for (String id : idsStr.split(",")){
			ids.add(Long.valueOf(id));
		}
		jdbcTemp.execute("DELETE FROM USER_PROJECT WHERE PROJECT_ID IN (" + idsStr + ")");
		int del = projectDao.batchDelete(ids);
		if (del > 0) {
			return ids;
		} else {
			throw new RuntimeException("批量删除用户失败");
		}		
	}
	
	public int shareProject(Long projectId, String userIdsStr) {
		logger.info("进入 ProjectService.shareProject()");
		assert userIdsStr == null || userIdsStr.length() == 0 : "没有传入将被分享的用户ID";
		List<Long> userIds = new ArrayList<>();
		List<CorpusTaggedFile> fileList = taggedDao.selectOriFile(projectId, "train");
		for (String id : userIdsStr.split(",")){ 
			userIds.add(Long.valueOf(id));
			User user = userDao.selectByPrimaryKey(Long.valueOf(id));
			for (CorpusTaggedFile file : fileList) {
				file.setFileId(null);
				file.setCreatedTime(new Date());
				file.setModifiedBy(user.getLoginName());
				taggedDao.insertCorpusTaggedFile(file);
			}
		}
		
		String sql = "INSERT INTO USER_PROJECT (USER_ID, PROJECT_ID, PROJECT_OWNER) VALUES (?, ?, ?)";

		jdbcTemp.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setLong(1, userIds.get(i));
				ps.setLong(2, projectId);
				ps.setInt(3, 0);
			}

			@Override
			public int getBatchSize() {
				return userIds.size();
			}
		});
		return 1;
	}
}
