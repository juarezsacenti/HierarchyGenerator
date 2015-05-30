package com.fabiosalvini.hierarchygenerator.database.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.Query;

import com.fabiosalvini.hierarchygenerator.database.model.Resource;


public interface ResourceRepository extends org.springframework.data.repository.Repository<Resource,Integer> {
	
	public Resource findById(Integer id);
	public Resource findByUrl(String url);

	@Query(value = "SELECT * FROM resources WHERE processed = false", nativeQuery = true)
	public Set<Resource> getResourceToBeProcessed();
	
	public Resource save(Resource resource);
}
