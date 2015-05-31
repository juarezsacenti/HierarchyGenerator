package com.fabiosalvini.hierarchygenerator.dataset.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SparqlEndpoint {
	
	private String url;
	private Integer maxRequestsSecond;
	
	public SparqlEndpoint() {
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@JsonProperty("max_requests_second")
	public Integer getMaxRequestsSecond() {
		return maxRequestsSecond;
	}

	public void setMaxRequestsSecond(Integer maxRequestsSecond) {
		this.maxRequestsSecond = maxRequestsSecond;
	}

}
