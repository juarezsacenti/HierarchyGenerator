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
@Table(name = "resources_sameas", schema = "public", uniqueConstraints = @UniqueConstraint(columnNames = { "first_resource_id", "second_resource_id" }))
public class ResourceSameAs {
	
	private int id;
	private Resource firstResource;
	private Resource secondResource;
	
	public ResourceSameAs() {
	}

	public ResourceSameAs(int id, Resource firstResource, Resource secondResource) {
		this.id = id;
		this.firstResource = firstResource;
		this.secondResource = secondResource;
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
	@JoinColumn(name = "first_resource_id", nullable = false)
	public Resource getFirstResource() {
		return firstResource;
	}

	public void setFirstResource(Resource firstResource) {
		this.firstResource = firstResource;
	}
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "second_resource_id", nullable = false)
	public Resource getSecondResource() {
		return secondResource;
	}

	public void setSecondResource(Resource secondResource) {
		this.secondResource = secondResource;
	}
}
