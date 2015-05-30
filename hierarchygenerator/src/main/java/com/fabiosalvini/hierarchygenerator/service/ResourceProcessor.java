package com.fabiosalvini.hierarchygenerator.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fabiosalvini.hierarchygenerator.database.model.Resource;
import com.fabiosalvini.hierarchygenerator.database.model.ResourceParent;
import com.fabiosalvini.hierarchygenerator.database.model.ResourceSameAs;
import com.fabiosalvini.hierarchygenerator.database.repository.ResourceParentRepository;
import com.fabiosalvini.hierarchygenerator.database.repository.ResourceRepository;
import com.fabiosalvini.hierarchygenerator.database.repository.ResourceSameAsRepository;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;


public class ResourceProcessor extends Thread {
	
	private static final Logger log = LoggerFactory.getLogger(ResourceProcessor.class);
	

	private DatasetsManager datasetsManager;
	private ResourceRepository resourceRepository;
	private ResourceSameAsRepository resourceSameAsRepository;
	private ResourceParentRepository resourceParentRepository;
	
	private Set<Resource> resources;
	private boolean skipSameAs;

	public ResourceProcessor(DatasetsManager datasetsManager, ResourceRepository resourceRepository, ResourceSameAsRepository resourceSameAsRepository,ResourceParentRepository resourceParentRepository,
			Collection<Resource> resources, boolean skipSameAs) {
		this.datasetsManager = datasetsManager;
		this.resourceRepository = resourceRepository;
		this.resourceSameAsRepository = resourceSameAsRepository;
		this.resourceParentRepository = resourceParentRepository;
		this.resources = new HashSet<Resource>();
		for(Resource res: resources) {
			this.resources.add(res);
		}
		this.skipSameAs = skipSameAs;
	}
	
	@Override
	public void run() {
		Model model = ModelFactory.createDefaultModel();
		for(Resource res: resources) {
			model.read(res.getUrl());
			
			extractLabel(res, model);
			
			if(!skipSameAs) {
				searchSameAs(res, model);
			}
			
			searchParents(res, model);
			
			res.setProcessed(true);
			res = resourceRepository.save(res);
			
		}
	}
	
	private void extractLabel(Resource res, Model model) {
		log.debug("Extracting the label");
		Property labelProperty = model.getProperty("http://www.w3.org/2000/01/rdf-schema#label");
		Iterator<RDFNode> labelsIter = model.listObjectsOfProperty(labelProperty);
		String resLabel = null;
		if(labelsIter.hasNext()) {
			boolean foundEnglishLabel = false;
			do {
				String label = labelsIter.next().toString();
				if(label.contains("@en")) {
					foundEnglishLabel = true;
				}
				resLabel = getPrettyLabel(label);
			} while(labelsIter.hasNext() && !foundEnglishLabel);
		} else {
			log.warn("Label not found for resource {}", res.getUrl());
		}
		res.setLabel(resLabel);
	}
	
	private void searchSameAs(Resource res, Model model) {
		log.debug("Searching same representation of the resource");
		List<String> sameAsProperties = datasetsManager.getSameAsProperties(res);
		for(String sameAsProp: sameAsProperties) {
			Property sameAsProperty = model.getProperty(sameAsProp);
			Iterator<RDFNode> sameAsIter = model.listObjectsOfProperty(sameAsProperty);
			while(sameAsIter.hasNext()) {
				String sameResUrl = sameAsIter.next().toString();
				if(!res.getUrl().equals(sameResUrl)) {
					Resource sameRes = resourceRepository.findByUrl(sameResUrl);
					if(sameRes == null) {
						log.debug("Creating resource {}", sameResUrl);
						sameRes = new Resource();
						sameRes.setUrl(sameResUrl);
						sameRes = resourceRepository.save(sameRes);
					}
					log.debug("Saving sameAs connection between {} and {}", res.getUrl(), sameRes.getUrl());
					ResourceSameAs resConnection = resourceSameAsRepository.getByResources(res.getId(), sameRes.getId());
					if(resConnection == null) {
						resConnection = new ResourceSameAs();
						resConnection.setFirstResource(res);
						resConnection.setSecondResource(sameRes);
						resConnection = resourceSameAsRepository.save(resConnection);
					}
				}
			}
		}
	}
	
	private void searchParents(Resource res, Model model) {
		log.debug("Searching parents of the resource");
		List<String> childOfProperties = datasetsManager.getChildOfProperties(res);
		for(String childOfProp: childOfProperties) {
			Property childOfProperty = model.getProperty(childOfProp);
			Iterator<RDFNode> childOfIter = model.listObjectsOfProperty(childOfProperty);
			while(childOfIter.hasNext()) {
				String parentResUrl = childOfIter.next().toString();
				if(!res.getUrl().equals(parentResUrl)) {
					Resource parentRes = resourceRepository.findByUrl(parentResUrl);
					if(parentRes == null) {
						log.debug("Creating resource {}", parentResUrl);
						parentRes = new Resource();
						parentRes.setUrl(parentResUrl);
						parentRes = resourceRepository.save(parentRes);
					}
					log.debug("Saving childOf connection between {} and {}", res.getUrl(), parentRes.getUrl());
					ResourceParent resConnection = new ResourceParent();
					resConnection.setChildResource(res);
					resConnection.setParentResource(parentRes);
					resConnection = resourceParentRepository.save(resConnection);
				}
			}
		}
	}
	
	/**
	 * Get the label without the language information.
	 * @param label label to process
	 * @return a String containing label without the language information
	 */
	public String getPrettyLabel(String label) {
		String prettyLabel;
		if(label.contains("@")) {
			prettyLabel = label.substring(0, label.indexOf("@"));
		} else {
			prettyLabel = label;
		}
		return prettyLabel;
	}
	

}
