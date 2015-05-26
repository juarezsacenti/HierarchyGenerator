package com.fabiosalvini.hierarchygenerator.database.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "resources", schema = "public", uniqueConstraints = @UniqueConstraint(columnNames = { "url" }))
public class Resource {
	
	private int id;
	private String url;
	private String label;
	private boolean processedSameAs;
	private boolean processedParents;
	
	public Resource() {
	}

	public Resource(int id, String url, String label, boolean processedSameAs, boolean processedParents) {
		this.id = id;
		this.url = url;
		this.label = label;
		this.processedSameAs = processedSameAs;
		this.processedParents = processedParents;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column(name = "url", nullable = false)
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Column(name = "label", nullable = false)
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Column(name = "processed_sameas", nullable = false)
	public boolean isProcessedSameAs() {
		return processedSameAs;
	}

	public void setProcessedSameAs(boolean processedSameAs) {
		this.processedSameAs = processedSameAs;
	}

	@Column(name = "processed_parents", nullable = false)
	public boolean isProcessedParents() {
		return processedParents;
	}

	public void setProcessedParents(boolean processedParents) {
		this.processedParents = processedParents;
	}
}
