package com.fabiosalvini.hierarchygenerator.database.repository;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fabiosalvini.hierarchygenerator.database.model.Resource;
import com.fabiosalvini.hierarchygenerator.database.model.ResourceParent;

@Repository
public class DimensionRepository {
	
	@PersistenceContext(unitName="persistenceUnitHierarchyGenerator")
	EntityManager entityManager;
	
	@Autowired
	private ResourceParentRepository resourceParentsRepository;
	
	@Transactional
	public void createDimensionTable(int levels) {
		String query = "CREATE TABLE dimensions ("
				       + "id serial PRIMARY KEY, "
				       + "entity_id integer NOT NULL ";
		for(int i = 0; i < levels; i++) {
			query += ", level_"+ i +" character varying(255)";
		}
		query += ")";
		Query q = entityManager.createNativeQuery(query);
		q.executeUpdate();
	}
	
	@Transactional
	public void saveEntityDimension(Integer entityId, Resource lvlZeroRes) {
		List<Object> params = new LinkedList<Object>();
		String query = "INSERT INTO dimensions (entity_id";
		String values = "?";
		params.add(entityId);
		Resource parentRes = lvlZeroRes;
		int i = 0;
		do {
			query += ", level_"+ i;
			values += ",?";
			params.add(parentRes.getLabel());
			List<ResourceParent> resParents = resourceParentsRepository.findByChildResource(parentRes);
			if(resParents != null && resParents.size() > 0) {
				parentRes = resParents.get(0).getParentResource();
			} else {
				parentRes = null;
			}
			i++;
		} while(parentRes != null);
		query += ") VALUES (" + values + ")";
		
		Query q = entityManager.createNativeQuery(query);
		applyParameters(q, params);
		q.executeUpdate();
	}
	
	/**
	 * Apply parameters to the query, following the list order.
	 * 
	 * @param q Query
	 * @param params parameters
	 */
	private void applyParameters(Query q, List<Object> params) {
		Iterator<Object> paramIter = params.iterator();
		int paramNum = 1;
		while(paramIter.hasNext()) {
			q.setParameter(paramNum, paramIter.next());
			paramNum++;
		}
	}
}
