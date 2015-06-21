package com.fabiosalvini.hierarchygenerator.database.repository;

import com.fabiosalvini.hierarchygenerator.database.model.Dimension;


public interface DimensionRepository extends org.springframework.data.repository.CrudRepository<Dimension,Integer> {
	
	public Dimension findById(Integer id);

	@SuppressWarnings("unchecked")
	public Dimension save(Dimension dimension);
}
