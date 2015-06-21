package com.fabiosalvini.hierarchygenerator.database.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "dimensions", schema = "public")
public class Dimension {
	
	private int id;
	private Integer entityId;
	private String levelName;
	private Integer levelDistance;
	
	public Dimension() {
	}

	public Dimension(int id, Integer entityId, String levelName,
			Integer levelDistance) {
		super();
		this.id = id;
		this.entityId = entityId;
		this.levelName = levelName;
		this.levelDistance = levelDistance;
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

	@Column(name = "level_name")
	public String getLevelName() {
		return levelName;
	}

	public void setLevelName(String levelName) {
		this.levelName = levelName;
	}

	@Column(name = "level_distance", nullable = false)
	public Integer getLevelDistance() {
		return levelDistance;
	}

	public void setLevelDistance(Integer levelDistance) {
		this.levelDistance = levelDistance;
	}
	
}
