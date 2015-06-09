package com.fabiosalvini.hierarchygenerator.database.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fabiosalvini.hierarchygenerator.database.model.Resource;


public interface ResourceRepository extends org.springframework.data.repository.CrudRepository<Resource,Integer> {
	
	public Resource findById(Integer id);
	public Resource findByUrl(String url);

	@Query(value = "SELECT * FROM resources WHERE processed_at IS NULL", nativeQuery = true)
	public List<Resource> getResourceToBeProcessed();
	
	@Query(value = "SELECT * FROM resources WHERE label IS NULL", nativeQuery = true)
	public List<Resource> getResourcesWithoutLabel();
	
	@Query(value = "WITH RECURSIVE rec_resources_parents(child_resource_id) AS "
			     + "(SELECT child_resource_id "
			     + "FROM resources_parents "
			     + "WHERE parent_resource_id = :resourceId "
			     + "UNION "
			     + "SELECT :resourceId "
			     + "UNION "
			     + "SELECT rp.child_resource_id "
			     + "FROM resources_parents rp "
			     + "INNER JOIN rec_resources_parents rrp ON rp.parent_resource_id = rrp.child_resource_id "
			     + ") "
			     + "SELECT count(DISTINCT em.entity_id) "
			     + "FROM rec_resources_parents rrp "
			     + "INNER JOIN entities_mappings em ON rrp.child_resource_id = em.resource_id", nativeQuery = true)
	public int getResourceWeigth(@Param("resourceId")Integer resourceId);
	
	@Query(value = "SELECT * "
			     + "FROM resources r "
			     + "WHERE (SELECT COUNT(*) FROM resources_parents WHERE child_resource_id = r.id) > 1", nativeQuery = true)
	public List<Resource> getResourcesWithMultipleParents();
	
	@Query(value = "SELECT * "
		     + "FROM resources r "
		     + "WHERE NOT EXISTS (SELECT * FROM resources_parents WHERE child_resource_id = r.id)", nativeQuery = true)
	public List<Resource> getRootResources();
	
	@Query(value = "SELECT COUNT(*) "
		     + "FROM entities_mappings "
		     + "WHERE resource_id = :resourceId", nativeQuery = true)
	public int getResourceEntitiesCount(@Param("resourceId")Integer resourceId);
	
	@SuppressWarnings("unchecked")
	public Resource save(Resource resource);
}
