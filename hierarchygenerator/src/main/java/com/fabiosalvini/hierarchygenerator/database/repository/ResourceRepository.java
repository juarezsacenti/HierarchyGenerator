package com.fabiosalvini.hierarchygenerator.database.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.fabiosalvini.hierarchygenerator.database.model.Resource;


public interface ResourceRepository extends org.springframework.data.repository.CrudRepository<Resource,Integer> {
	
	public Resource findById(Integer id);
	public Resource findByUrl(String url);

	@Query(value = "SELECT * FROM resources WHERE processed = false", nativeQuery = true)
	public List<Resource> getResourceToBeProcessed();
	
	@Query(value = "SELECT * FROM resources WHERE label IS NULL", nativeQuery = true)
	public List<Resource> getResourcesWithoutLabel();
	
	@SuppressWarnings("unchecked")
	public Resource save(Resource resource);
}
