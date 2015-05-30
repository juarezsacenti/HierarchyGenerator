package com.fabiosalvini.hierarchygenerator.applicationmanger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.fabiosalvini.hierarchygenerator.database.repository.ResourceParentRepository;
import com.fabiosalvini.hierarchygenerator.database.repository.ResourceRepository;
import com.fabiosalvini.hierarchygenerator.database.repository.ResourceSameAsRepository;
import com.fabiosalvini.hierarchygenerator.service.DatasetsManager;
import com.fabiosalvini.hierarchygenerator.service.ResourceProcessor;

@Service
public class ApplicationManager {

	private static final Logger log = LoggerFactory.getLogger(ApplicationManager.class);

	@Autowired
	protected ApplicationContext applicationContext;

	@Autowired
	private DatasetsManager datasetsManager;
	@Autowired
	private ResourceRepository resourceRepository;
	@Autowired
	private ResourceSameAsRepository resourceSameAsRepository;
	@Autowired
	private ResourceParentRepository resourceParentsRepository;

	public ApplicationManager() {
	}

	public void start() {
		
		ResourceProcessor rp = new ResourceProcessor(datasetsManager,resourceRepository,resourceSameAsRepository,resourceParentsRepository,resourceRepository.getResourceToBeProcessed(), false);
		rp.start();
		
	}

	public void stop() {
	}

}
