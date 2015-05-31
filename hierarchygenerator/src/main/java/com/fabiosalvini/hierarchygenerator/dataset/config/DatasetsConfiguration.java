package com.fabiosalvini.hierarchygenerator.dataset.config;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DatasetsConfiguration {
	
	private List<Dataset> datasets;
	private List<String> excludeUrls;

	public DatasetsConfiguration() {
	}

	public List<Dataset> getDatasets() {
		return datasets;
	}

	public void setDatasets(List<Dataset> datasets) {
		this.datasets = datasets;
	}

	@JsonProperty("exclude_urls")
	public List<String> getExcludeUrls() {
		return excludeUrls;
	}

	public void setExcludeUrls(List<String> excludeUrls) {
		this.excludeUrls = excludeUrls;
	}

}