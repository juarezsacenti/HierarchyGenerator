package com.fabiosalvini.hierarchygenerator.database.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fabiosalvini.hierarchygenerator.database.model.ResourceSameAs;


public interface ResourceSameAsRepository extends org.springframework.data.repository.Repository<ResourceSameAs,Integer> {
	
	public ResourceSameAs findById(Integer id);
	
	@Query(value = "SELECT * "
			     + "FROM resources_sameas "
			     + "WHERE (first_resource_id = :firstResourceId AND second_resource_id = :secondResourceId) "
			        + "OR (first_resource_id = :secondResourceId AND second_resource_id = :firstResourceId)", nativeQuery = true)
	public ResourceSameAs getByResources(@Param("firstResourceId")Integer firstResourceId, @Param("secondResourceId")Integer secondResourceId);
	
	public ResourceSameAs save(ResourceSameAs resourceSameAs);
}
