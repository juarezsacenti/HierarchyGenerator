package com.fabiosalvini.hierarchygenerator.database.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fabiosalvini.hierarchygenerator.database.model.ResourceSameAs;


public interface ResourceSameAsRepository extends org.springframework.data.repository.CrudRepository<ResourceSameAs,Integer> {
	
	public ResourceSameAs findById(Integer id);
	
	@Query(value = "SELECT * FROM resources_sameas LIMIT 1", nativeQuery = true)
	public ResourceSameAs getOne();
	
	@Query(value = "SELECT * "
		     + "FROM resources_sameas "
		     + "WHERE first_resource_id = :resourceId "
		        + "OR second_resource_id = :resourceId", nativeQuery = true)
	public Set<ResourceSameAs> getByResource(@Param("resourceId")Integer resourceId);
	
	@Query(value = "SELECT * "
			     + "FROM resources_sameas "
			     + "WHERE (first_resource_id = :firstResourceId AND second_resource_id = :secondResourceId) "
			        + "OR (first_resource_id = :secondResourceId AND second_resource_id = :firstResourceId)", nativeQuery = true)
	public ResourceSameAs getByResources(@Param("firstResourceId")Integer firstResourceId, @Param("secondResourceId")Integer secondResourceId);
	
	@SuppressWarnings("unchecked")
	public ResourceSameAs save(ResourceSameAs resourceSameAs);
}
