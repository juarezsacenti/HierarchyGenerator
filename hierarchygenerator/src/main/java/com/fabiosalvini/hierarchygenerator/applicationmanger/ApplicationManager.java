package com.fabiosalvini.hierarchygenerator.applicationmanger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import com.fabiosalvini.hierarchygenerator.database.repository.ResourceRepository;
import com.fabiosalvini.hierarchygenerator.service.HierarchyBuilder;
import com.fabiosalvini.hierarchygenerator.service.ProcessorsManager;

@Service
public class ApplicationManager {

	private static final Logger log = LoggerFactory.getLogger(ApplicationManager.class);

	@Autowired
	protected ApplicationContext applicationContext;

	@Autowired
	private ProcessorsManager processorsManager;
	@Autowired
	private HierarchyBuilder hierarchyBuilder;
	@Autowired
	private ResourceRepository resourceRepository;

	public ApplicationManager() {
	}

	public void start() {
		log.debug("Elaborating resources");
		processorsManager.startElaboration();
	}
	
	public void resourceElaborationFinished() {
		log.info("Resources elaboration finished");
		/*List<Resource> resToDelete = resourceRepository.getResourcesWithoutLabel();
		resourceRepository.delete(resToDelete);*/
		hierarchyBuilder.buildHierarchies();
		stop();
	}

	public void stop() {
		log.info("Closing application context");
		((ConfigurableApplicationContext)applicationContext).close();
	}

}
