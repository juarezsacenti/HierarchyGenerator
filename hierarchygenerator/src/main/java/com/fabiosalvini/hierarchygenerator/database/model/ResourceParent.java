package com.fabiosalvini.hierarchygenerator.database.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "resources_parents", schema = "public", uniqueConstraints = @UniqueConstraint(columnNames = { "child_resource_id", "parent_resource_id" }))
public class ResourceParent {
	
	private int id;
	private Resource childResource;
	private Resource parentResource;
	
	public ResourceParent() {
	}

	public ResourceParent(int id, Resource childResource, Resource parentResource) {
		this.id = id;
		this.childResource = childResource;
		this.parentResource = parentResource;
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
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "child_resource_id", nullable = false)
	public Resource getChildResource() {
		return childResource;
	}

	public void setChildResource(Resource childResource) {
		this.childResource = childResource;
	}
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "parent_resource_id", nullable = false)
	public Resource getParentResource() {
		return parentResource;
	}

	public void setParentResource(Resource parentResource) {
		this.parentResource = parentResource;
	}
}
