package com.fabiosalvini.hierarchygenerator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fabiosalvini.hierarchygenerator.database.repository.ResourceParentRepository;
import com.fabiosalvini.hierarchygenerator.database.repository.ResourceRepository;
import com.fabiosalvini.hierarchygenerator.database.repository.ResourceSameAsRepository;

@Service
public class HierarchyBuilder {
	
	@Autowired
	private ResourceRepository resourceRepository;
	@Autowired
	private ResourceSameAsRepository resourceSameAsRepository;
	@Autowired
	private ResourceParentRepository resourceParentsRepository;

	public HierarchyBuilder() {
	}
	
	public void buildHierarchies() {
		
	}

}
