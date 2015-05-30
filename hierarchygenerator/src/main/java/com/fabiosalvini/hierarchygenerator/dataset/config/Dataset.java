package com.fabiosalvini.hierarchygenerator.dataset.config;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Dataset {
	
	private Integer id;
	private String domain;
	private List<Integer> linkedWithDatasets;
	private List<String> sameAsProperties;
	private List<String> childOfProperties;
	
	public Dataset() {
		linkedWithDatasets = Collections.emptyList();
		sameAsProperties = Collections.emptyList();
		childOfProperties = Collections.emptyList();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	@JsonProperty("linked_with_datasets")
	public List<Integer> getLinkedWithDatasets() {
		return linkedWithDatasets;
	}

	public void setLinkedWithDatasets(List<Integer> linkedWithDatasets) {
		this.linkedWithDatasets = linkedWithDatasets;
	}

	@JsonProperty("sameas_properties")
	public List<String> getSameAsProperties() {
		return sameAsProperties;
	}

	public void setSameAsProperties(List<String> sameAsProperties) {
		this.sameAsProperties = sameAsProperties;
	}

	@JsonProperty("child_of_properties")
	public List<String> getChildOfProperties() {
		return childOfProperties;
	}

	public void setChildOfProperties(List<String> childOfProperties) {
		this.childOfProperties = childOfProperties;
	}
	
}
