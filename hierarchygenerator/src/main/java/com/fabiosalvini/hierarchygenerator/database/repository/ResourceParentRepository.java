package com.fabiosalvini.hierarchygenerator.database.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fabiosalvini.hierarchygenerator.database.model.ResourceParent;


public interface ResourceParentRepository extends org.springframework.data.repository.Repository<ResourceParent,Integer> {
	
	public ResourceParent findById(Integer id);
	
	@Query(value = "SELECT * "
			     + "FROM resources_sameas "
			     + "WHERE child_resource_id = :childResourceId AND parent_resource_id = :parentResourceId", nativeQuery = true)
	public ResourceParent getByResources(@Param("childResourceId")Integer childResourceId, @Param("parentResourceId")Integer parentResourceId);
	
	public ResourceParent save(ResourceParent resourceParent);
}
