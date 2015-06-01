package com.fabiosalvini.hierarchygenerator.database.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fabiosalvini.hierarchygenerator.database.model.EntityMapping;
import com.fabiosalvini.hierarchygenerator.database.model.Resource;


public interface EntityMappingRepository extends org.springframework.data.repository.CrudRepository<EntityMapping,Integer> {
	
	public EntityMapping findById(Integer id);
	public Set<EntityMapping> findByResource(Resource resource);
	
	@Query(value = "SELECT * FROM entities_mappings WHERE entityId = :entityId AND resource_id = :resourceId", nativeQuery = true)
	public EntityMapping getByEntityIdAndResource(@Param("entityId")Integer entityId, @Param("resourceId")Integer resourceId);

	@SuppressWarnings("unchecked")
	public EntityMapping save(EntityMapping resourceSameAs);
}
