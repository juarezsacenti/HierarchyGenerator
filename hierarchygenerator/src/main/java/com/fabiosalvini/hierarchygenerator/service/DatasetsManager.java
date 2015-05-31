package com.fabiosalvini.hierarchygenerator.service;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fabiosalvini.hierarchygenerator.database.model.Resource;
import com.fabiosalvini.hierarchygenerator.dataset.config.Dataset;
import com.fabiosalvini.hierarchygenerator.dataset.config.DatasetsConfiguration;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DatasetsManager {
	
	private static final Logger log = LoggerFactory.getLogger(DatasetsManager.class);
	
	@Value("${const.resource.label_property}")
	private String resourceLabelProperty;
	
	private DatasetsConfiguration datasetsConfiguration;
	
	
	public DatasetsManager() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		try {
			InputStreamReader datasetsConfigFile = null;
			datasetsConfigFile = new InputStreamReader(new FileInputStream("src/main/resources/datasets/datasets.json"), "UTF-8");
			datasetsConfiguration = mapper.readValue(datasetsConfigFile, DatasetsConfiguration.class);
		} catch(Exception e) {
			log.error("Error loading datasets configuration file: {}", e);
			throw new RuntimeException("Error loading datasets configuration file");
		}
	}
	
	/**
	 * Get the properties used to find the label of the resource
	 * @param res resource to use
	 * @return A list String containing the label properties
	 */
	public List<String> getResourceLabelProperty(Resource res) {
		LinkedList<String> labelProperties = new LinkedList<String>();
		labelProperties.add(resourceLabelProperty);
		for(Dataset dataset: datasetsConfiguration.getDatasets()) {
			if(res.getUrl().contains(dataset.getDomain())) {
				if(dataset.getResourceLabel() != null) {
					labelProperties.add(dataset.getResourceLabel());
				}
				return labelProperties;
			}
		}
		return labelProperties;
	}
	
	/**
	 * Return the properties used to associate the specific resource with resources of other datasets.
	 * @param resource Resource to associate
	 * @return A List of Strings containing the properties
	 */
	public List<String> getSameAsProperties(Resource resource) {
		for(Dataset dataset: datasetsConfiguration.getDatasets()) {
			if(resource.getUrl().contains(dataset.getDomain())) {
				return dataset.getSameAsProperties();
			}
		}
		return Collections.emptyList();
	}
	
	/**
	 * Get the properties used to find the parents of the specific resource.
	 * @param resource Resource to associate
	 * @return A List of Strings containing the properties
	 */
	public List<String> getChildOfProperties(Resource resource) {
		for(Dataset dataset: datasetsConfiguration.getDatasets()) {
			if(resource.getUrl().contains(dataset.getDomain())) {
				return dataset.getChildOfProperties();
			}
		}
		return Collections.emptyList();
	}
	
	/**
	 * Get the datasets which resources are linked with the dataset of the specific resource.
	 * @param resource Resource to be linked
	 * @return A Set of Datasets that have links with the dataset of the resource
	 */
	public Set<Dataset> getLinkedByDatasets(Resource resource) {
		int resourceDatasetId = -1;
		Iterator<Dataset> datasetIter = datasetsConfiguration.getDatasets().iterator();
		while(datasetIter.hasNext() && resourceDatasetId == -1) {
			Dataset dataset = datasetIter.next();
			if(resource.getUrl().contains(dataset.getDomain())) {
				resourceDatasetId = dataset.getId();
			}
		}
		Set<Dataset> linkedByDatasets = new HashSet<Dataset>();
		for(Dataset dataset: datasetsConfiguration.getDatasets()) {
			if(dataset.getLinkedWithDatasets().contains(resourceDatasetId)) {
				linkedByDatasets.add(dataset);
			}
		}
		
		return linkedByDatasets;
	}
	
	public List<String> getExcludeUrls() {
		return datasetsConfiguration.getExcludeUrls();
	}
}
