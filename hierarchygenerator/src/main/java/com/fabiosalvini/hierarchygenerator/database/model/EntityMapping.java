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
@Table(name = "entities_mappings", schema = "public", uniqueConstraints = @UniqueConstraint(columnNames = { "entity_id", "resource_id" }))
public class EntityMapping {
	
	private int id;
	private Integer entityId;
	private Resource resource;
	
	public EntityMapping() {
	}

	public EntityMapping(int id, Integer entityId, Resource resource) {
		this.id = id;
		this.entityId = entityId;
		this.resource = resource;
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
	
	@Column(name = "entity_id", nullable = false)
	public Integer getEntityId() {
		return entityId;
	}

	public void setEntityId(Integer entityId) {
		this.entityId = entityId;
	}
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "resource_id", nullable = false)
	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}
}
