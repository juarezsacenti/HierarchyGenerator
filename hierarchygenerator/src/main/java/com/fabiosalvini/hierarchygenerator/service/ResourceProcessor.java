package com.fabiosalvini.hierarchygenerator.service;

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
import com.fabiosalvini.hierarchygenerator.dataset.config.Dataset;
import com.fabiosalvini.hierarchygenerator.dataset.config.SparqlEndpoint;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;


public class ResourceProcessor extends Thread {
	
	private static final Logger log = LoggerFactory.getLogger(ResourceProcessor.class);
	

	private ProcessorsManager processorsManager;
	private DatasetsManager datasetsManager;
	private ResourceRepository resourceRepository;
	private ResourceSameAsRepository resourceSameAsRepository;
	private ResourceParentRepository resourceParentRepository;
	private ResourceValidator resourceValidator;
	
	private int identifier;
	private boolean skipSameAs;

	public ResourceProcessor(
			int identifier,
			ProcessorsManager processorsManager, 
			DatasetsManager datasetsManager, 
			ResourceRepository resourceRepository, 
			ResourceSameAsRepository resourceSameAsRepository,
			ResourceParentRepository resourceParentRepository,
			ResourceValidator resourceValidator,
			boolean skipSameAs) {
		this.identifier = identifier;
		this.processorsManager = processorsManager;
		this.datasetsManager = datasetsManager;
		this.resourceRepository = resourceRepository;
		this.resourceSameAsRepository = resourceSameAsRepository;
		this.resourceParentRepository = resourceParentRepository;
		this.resourceValidator = resourceValidator;
		this.skipSameAs = skipSameAs;
	}
	
	@Override
	public void run() {
		Model model = ModelFactory.createDefaultModel();
		while (!Thread.currentThread().isInterrupted()) {
            Resource res = processorsManager.getResourceToElaborate();
			if(res != null) {
				log.info("[Thread {}] Processing resource {}", identifier, res.getUrl());
				try {
					model.read(res.getUrl());
					
					extractLabel(res, model);
					
					if(!skipSameAs) {
						searchSameAs(res, model);
						searchReversedSameAs(res);
					}
					
					searchParents(res, model);
				} catch(Exception e) {
					log.error("Error processing resource {}: {}", res.getUrl(), e);
				}
				
				res.setProcessed(true);
				res = resourceRepository.save(res);
			} else {
				try {
					processorsManager.notifyIdle();
					log.debug("Thread {} is going to sleep", identifier);
					Thread.sleep(1000);
					processorsManager.notifyActive(); //TODO: is it correct?
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
        }
		log.debug("[Thread {}] ended", identifier);
	}
	
	private void extractLabel(Resource res, Model model) {
		log.debug("Extracting the label");
		Iterator<String> labelProps = datasetsManager.getResourceLabelProperty(res).iterator();
		String resLabel = null;
		while(labelProps.hasNext() && resLabel == null) {
			Property labelProperty = model.getProperty(labelProps.next());
			Iterator<RDFNode> labelsIter = model.listObjectsOfProperty(labelProperty);
			if(labelsIter.hasNext()) {
				boolean foundEnglishLabel = false;
				do {
					String label = labelsIter.next().toString();
					if(label.contains("@en")) {
						foundEnglishLabel = true;
					}
					resLabel = getPrettyLabel(label);
				} while(labelsIter.hasNext() && !foundEnglishLabel);
			}
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
					addSameAsConnection(res, sameResUrl);
				}
			}
		}
	}
	
	private void searchReversedSameAs(Resource res) {
		Set<Dataset> linkedByDatasets = datasetsManager.getLinkedByDatasets(res);
		for(Dataset dataset: linkedByDatasets) {
			SparqlEndpoint sparqlEndpoint = dataset.getSparqlEndpoint();
			if(sparqlEndpoint != null) {
				List<String> sameAsProps = dataset.getSameAsProperties();
				if(sameAsProps != null && sameAsProps.size() > 0) {
					
					Iterator<String> sameAsIter = sameAsProps.iterator();
					String queryString = "SELECT ?r WHERE { " +
			        		"{ ?r <"+sameAsIter.next()+"> <"+res.getUrl()+"> }";
					while(sameAsIter.hasNext()) {
						queryString += " UNION { ?r <"+sameAsIter.next()+"> <"+res.getUrl()+"> }";
					}
			        queryString += " }";
			
					Query query = QueryFactory.create(queryString);
					QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndpoint.getUrl(), query);
					
					try {
						log.debug("Executing query at endpoint {}", sparqlEndpoint.getUrl());
					    ResultSet results = qexec.execSelect();
					    while(results.hasNext()) {
					    	QuerySolution row = results.next();
					    	RDFNode node = row.get("r");
					    	
					    	addSameAsConnection(res, node.toString());
					    }
					} catch(Exception e) {
						log.error("Error during query execution and the endpoint: {}, {}", sparqlEndpoint.getUrl(), e);
					}
					finally {
					   qexec.close();
					}
				}
			}
		}
	}
	
	private void addSameAsConnection(Resource res, String sameResUrl) {
		if(resourceValidator.isValid(sameResUrl)) {
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
	
	private void searchParents(Resource res, Model model) {
		log.debug("Searching parents of the resource");
		List<String> childOfProperties = datasetsManager.getChildOfProperties(res);
		for(String childOfProp: childOfProperties) {
			Property childOfProperty = model.getProperty(childOfProp);
			Iterator<RDFNode> childOfIter = model.listObjectsOfProperty(childOfProperty);
			while(childOfIter.hasNext()) {
				String parentResUrl = childOfIter.next().toString();
				if(!res.getUrl().equals(parentResUrl) && resourceValidator.isValid(parentResUrl)) {
					Resource parentRes = resourceRepository.findByUrl(parentResUrl);
					if(parentRes == null) {
						log.debug("Creating resource {}", parentResUrl);
						parentRes = new Resource();
						parentRes.setUrl(parentResUrl);
						parentRes = resourceRepository.save(parentRes);
					}
					log.debug("Saving childOf connection between {} and {}", res.getUrl(), parentRes.getUrl());
					ResourceParent resConnection = resourceParentRepository.getByResources(res.getId(), parentRes.getId());
					if(resConnection == null) {
						resConnection = new ResourceParent();
						resConnection.setChildResource(res);
						resConnection.setParentResource(parentRes);
						resConnection = resourceParentRepository.save(resConnection);
					}
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
