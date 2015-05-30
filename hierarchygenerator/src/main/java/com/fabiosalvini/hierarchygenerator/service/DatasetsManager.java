package com.fabiosalvini.hierarchygenerator.service;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fabiosalvini.hierarchygenerator.database.model.Resource;
import com.fabiosalvini.hierarchygenerator.dataset.config.Dataset;
import com.fabiosalvini.hierarchygenerator.dataset.config.DatasetsConfiguration;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DatasetsManager {
	
	private static final Logger log = LoggerFactory.getLogger(DatasetsManager.class);
	
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
	 * Return the properties used to find the parents of the specific resource.
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
}
