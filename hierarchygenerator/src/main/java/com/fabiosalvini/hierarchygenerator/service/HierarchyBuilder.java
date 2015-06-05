package com.fabiosalvini.hierarchygenerator.service;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fabiosalvini.hierarchygenerator.database.model.EntityMapping;
import com.fabiosalvini.hierarchygenerator.database.model.Resource;
import com.fabiosalvini.hierarchygenerator.database.model.ResourceParent;
import com.fabiosalvini.hierarchygenerator.database.model.ResourceSameAs;
import com.fabiosalvini.hierarchygenerator.database.repository.EntityMappingRepository;
import com.fabiosalvini.hierarchygenerator.database.repository.ResourceParentRepository;
import com.fabiosalvini.hierarchygenerator.database.repository.ResourceRepository;
import com.fabiosalvini.hierarchygenerator.database.repository.ResourceSameAsRepository;

@Service
public class HierarchyBuilder {
	
	private static final Logger log = LoggerFactory.getLogger(HierarchyBuilder.class);
	
	@Autowired
	private ResourceRepository resourceRepository;
	@Autowired
	private ResourceSameAsRepository resourceSameAsRepository;
	@Autowired
	private ResourceParentRepository resourceParentsRepository;
	@Autowired
	private EntityMappingRepository entityMappingRepository;

	public HierarchyBuilder() {
	}
	
	public void buildHierarchies() {
		mergeResources();
		deleteResourceConnectionsToAncestors();
		weigthResources();
		removeMultipleParents();
	}
	
	/**
	 * Delete all the sameAs connections, merging equal resources together.
	 */
	private void mergeResources() {
		log.info("Merging resources");
		ResourceSameAs resSameAs = resourceSameAsRepository.getOne();
		while(resSameAs != null) {
			Resource resToKeep;
			Resource resToMerge;
			if(resSameAs.getFirstResource().getLabel() != null) {
				resToKeep = resSameAs.getFirstResource();
				resToMerge = resSameAs.getSecondResource();
			} else {
				resToMerge = resSameAs.getFirstResource();
				resToKeep = resSameAs.getSecondResource();
			}
			
			// Update entity mappings
			Set<EntityMapping> eMappings = entityMappingRepository.findByResource(resToMerge);
			for(EntityMapping eMap: eMappings) {
				if(entityMappingRepository.getByEntityIdAndResource(eMap.getId(), resToKeep.getId()) == null) {
					EntityMapping newEMap = new EntityMapping();
					newEMap.setEntityId(eMap.getEntityId());
					newEMap.setResource(resToKeep);
					newEMap = entityMappingRepository.save(newEMap);
				}
				entityMappingRepository.delete(eMap);
			}
			
			//Update resource parents
			List<ResourceParent> resParents = resourceParentsRepository.findByChildResource(resToMerge);
			for(ResourceParent resParent: resParents) {
				if(resourceParentsRepository.getByResources(resToKeep.getId(), resParent.getParentResource().getId()) == null) {
					resParent.setChildResource(resToKeep);
					resParent = resourceParentsRepository.save(resParent);
				} else {
					resourceParentsRepository.delete(resParent);
				}
			}
			List<ResourceParent> resChildrens = resourceParentsRepository.findByParentResource(resToMerge);
			for(ResourceParent resChildren: resChildrens) {
				if(resourceParentsRepository.getByResources(resChildren.getChildResource().getId(), resToKeep.getId()) == null) {
					resChildren.setParentResource(resToKeep);
					resChildren = resourceParentsRepository.save(resChildren);
				} else {
					resourceParentsRepository.delete(resChildren);
				}
			}
			
			//Update resource sameAs
			Set<ResourceSameAs> otherResSameAs = resourceSameAsRepository.getByResource(resToMerge.getId());
			for(ResourceSameAs sameAs: otherResSameAs) {
				if(sameAs.getId() != resSameAs.getId()) {
					Resource otherRes = (sameAs.getFirstResource().getId() == resToMerge.getId()) ? sameAs.getSecondResource() : sameAs.getFirstResource();
					if(resourceSameAsRepository.getByResources(resToKeep.getId(), otherRes.getId()) == null) {
						if(sameAs.getFirstResource().getId() == otherRes.getId()) {
							sameAs.setSecondResource(resToKeep);
						} else {
							sameAs.setFirstResource(resToKeep);
						}
						sameAs = resourceSameAsRepository.save(sameAs);
					} else {
						resourceSameAsRepository.delete(sameAs);
					}
				}
			}
			
			resourceSameAsRepository.delete(resSameAs);
			resourceRepository.delete(resToMerge);
			resSameAs = resourceSameAsRepository.getOne();
		}
	}
	
	/**
	 * Delete parent relations between a resource and all of its ancestors (except the parent).
	 */
	private void deleteResourceConnectionsToAncestors() {
		log.debug("Deleting resources connections to ancestors");
		Iterator<Resource> resourcesIter = resourceRepository.findAll().iterator();
		while(resourcesIter.hasNext()) {
			Resource res = resourcesIter.next();
			List<ResourceParent> parents = resourceParentsRepository.findByChildResource(res);
			do {
				List<ResourceParent> parentsOfParents = new LinkedList<ResourceParent>();
				for(ResourceParent resParent: parents) {
					parentsOfParents.addAll(resourceParentsRepository.findByChildResource(resParent.getParentResource()));
				}
				for(ResourceParent parOfPar: parentsOfParents) {
					ResourceParent resParent = resourceParentsRepository.getByResources(res.getId(), parOfPar.getParentResource().getId());
					if(resParent != null) {
						resourceParentsRepository.delete(resParent);
					}
				}
				parents = parentsOfParents;
			} while(parents.size() > 0);
		}
	}
	
	/**
	 * Weight the resources. A resource weight is the number of distinct entities "contained" by the resource.
	 */
	private void weigthResources() {
		log.debug("Weighting resources");
		Iterator<Resource> resourcesIter = resourceRepository.findAll().iterator();
		while(resourcesIter.hasNext()) {
			Resource res = resourcesIter.next();
			int resWeight = resourceRepository.getResourceWeigth(res.getId());
			res.setWeight(resWeight);
			res = resourceRepository.save(res);
		}
	}
	
	/**
	 * Keep only the parent with the higher weight for the resources that have multiple parents.
	 */
	private void removeMultipleParents() {
		List<Resource> resources = resourceRepository.getResourcesWithMultipleParents();
		for(Resource res: resources) {
			// Find the parent with the higher weight
			List<ResourceParent> resParents = resourceParentsRepository.findByChildResource(res); 
			int maxWeightIndex = 0;
			for(int i = 1; i < resParents.size(); i++) {
				if(resParents.get(i).getParentResource().getWeight() > resParents.get(0).getParentResource().getWeight()) {
					maxWeightIndex = i;
				}
			}
			// Delete all the other parents connections
			for(int i = 0; i < resParents.size(); i++) {
				if(i != maxWeightIndex) {
					resourceParentsRepository.delete(resParents.get(i));
				}
			}
			
		}
	}

}
