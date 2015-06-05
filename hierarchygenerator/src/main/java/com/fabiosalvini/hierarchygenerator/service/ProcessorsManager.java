package com.fabiosalvini.hierarchygenerator.service;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fabiosalvini.hierarchygenerator.applicationmanager.ApplicationManager;
import com.fabiosalvini.hierarchygenerator.database.model.Resource;
import com.fabiosalvini.hierarchygenerator.database.repository.ResourceParentRepository;
import com.fabiosalvini.hierarchygenerator.database.repository.ResourceRepository;
import com.fabiosalvini.hierarchygenerator.database.repository.ResourceSameAsRepository;

@Service
public class ProcessorsManager {
	
	private static final Logger log = LoggerFactory.getLogger(ProcessorsManager.class);
	
	@Autowired
	private ApplicationManager applicationManager;
	
	@Autowired
	private DatasetsManager datasetsManager;
	@Autowired
	private ResourceRepository resourceRepository;
	@Autowired
	private ResourceSameAsRepository resourceSameAsRepository;
	@Autowired
	private ResourceParentRepository resourceParentsRepository;
	@Autowired
	private ResourceValidator resourceValidator;
	
	@Value("${processors.max_num}")
	private int maxProcessorNumber;
	
	@Value("${resources.skip_sameas}")
	private boolean skipSameAs;
	
	private Set<ResourceProcessor> processors;
	private int activeProcessorsCount;
	private Set<Resource> resourcesBeingElaborated;

	public ProcessorsManager() {
		processors = new HashSet<ResourceProcessor>();
		resourcesBeingElaborated = new HashSet<Resource>();
	}
	
	public void startElaboration() {
		log.debug("Creating processors");
		for(int i = 1; i <= maxProcessorNumber; i++) {
			ResourceProcessor resProc = new ResourceProcessor(i,this,datasetsManager,resourceRepository,resourceSameAsRepository,resourceParentsRepository,resourceValidator,skipSameAs);
			processors.add(resProc);
			activeProcessorsCount++;
		}
		log.debug("Starting processors");
		for(ResourceProcessor resProc: processors) {
			resProc.start();
		}
	}
	
	public synchronized Resource getResourceToElaborate() {
		List<Resource> resList = resourceRepository.getResourceToBeProcessed();
		Iterator<Resource> resIter = resList.iterator();
		if(resIter == null || !resIter.hasNext()) {
			return null;
		}
		Resource res = resIter.next();
		while(resourcesBeingElaborated.contains(res) && resIter.hasNext()) {
			res = resIter.next();
		}
		if(!resourcesBeingElaborated.contains(res)) {
			resourcesBeingElaborated.add(res);
			return res;
		} else {
			return null;
		}
	}
	
	public synchronized void notifyIdle() {
		activeProcessorsCount--;
		if(activeProcessorsCount == 0) {
			log.info("Resource processing finished");
			for(ResourceProcessor resProc: processors) {
				resProc.interrupt();
			}
			applicationManager.resourceElaborationFinished();
		}
	}
	
	public synchronized void notifyActive() {
		activeProcessorsCount++;
	}

}
