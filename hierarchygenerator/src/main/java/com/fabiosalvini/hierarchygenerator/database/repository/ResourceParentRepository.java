package com.fabiosalvini.hierarchygenerator.database.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fabiosalvini.hierarchygenerator.database.model.Resource;
import com.fabiosalvini.hierarchygenerator.database.model.ResourceParent;


public interface ResourceParentRepository extends org.springframework.data.repository.CrudRepository<ResourceParent,Integer> {
	
	public ResourceParent findById(Integer id);
	public Set<ResourceParent> findByChildResource(Resource childResource);
	public Set<ResourceParent> findByParentResource(Resource parentResource);
	
	@Query(value = "SELECT * "
			     + "FROM resources_parents "
			     + "WHERE child_resource_id = :childResourceId AND parent_resource_id = :parentResourceId", nativeQuery = true)
	public ResourceParent getByResources(@Param("childResourceId")Integer childResourceId, @Param("parentResourceId")Integer parentResourceId);
	
	@SuppressWarnings("unchecked")
	public ResourceParent save(ResourceParent resourceParent);
}
