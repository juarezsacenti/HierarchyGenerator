package com.fabiosalvini.hierarchygenerator.service;

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
		log.info("Merging resources");
		mergeResources();
		log.info("Deleting resources connections to ancestors");
		//deleteResourceConnectionsToAncestors();
		log.info("Removing multiple parents");
		removeMultipleParents();
	}
	
	/**
	 * Delete all the sameAs connections, merging equal resources together.
	 */
	private void mergeResources() {
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
			List<EntityMapping> eMappings = entityMappingRepository.findByResource(resToMerge);
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
					if(resParent.getChildResource().getId() != resParent.getParentResource().getId()) {
						resParent = resourceParentsRepository.save(resParent);
					}
				} else {
					resourceParentsRepository.delete(resParent);
				}
			}
			List<ResourceParent> resChildrens = resourceParentsRepository.findByParentResource(resToMerge);
			for(ResourceParent resChildren: resChildrens) {
				if(resourceParentsRepository.getByResources(resChildren.getChildResource().getId(), resToKeep.getId()) == null) {
					resChildren.setParentResource(resToKeep);
					if(resChildren.getChildResource().getId() != resChildren.getParentResource().getId()) {
						resChildren = resourceParentsRepository.save(resChildren);
					}
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
	 * Keep only the parent with the higher weight for the resources that have multiple parents.
	 */
	/*private void removeMultipleParents() {
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
	}*/
	
	/**
	 * Keep only the parent with the highest level
	 */
	private void removeMultipleParents() {
		List<Resource> resources = resourceRepository.getResourcesWithMultipleParents();
		for(Resource res: resources) {
			List<ResourceParent> resParents = resourceParentsRepository.findByChildResource(res); 
			ResourceParent higherResParent = resParents.get(0);
			int higherLvl = getResourceLevel(higherResParent.getParentResource().getId());
			for(int i = 1; i < resParents.size(); i++) {
				int lvl = getResourceLevel(resParents.get(i).getParentResource().getId());
				if(lvl > higherLvl) {
					resourceParentsRepository.delete(higherResParent);
					higherResParent = resParents.get(i);
					higherLvl = lvl;
				} else {
					resourceParentsRepository.delete(resParents.get(i));
				}
			}
		}
	}
	
	/**
	 * Get the hierarchy level of the resource.
	 * @param resourceId id of the resource to calculate the level
	 * @return the level of the resource
	 */
	private int getResourceLevel(Integer resourceId) {
		Resource res = resourceRepository.findById(resourceId);
		if(res.getLevel() != null) {
			return res.getLevel();
		}
		List<ResourceParent> resParent = resourceParentsRepository.findByChildResource(res);
		if(resParent == null || resParent.size() == 0) {
			res.setLevel(0);
		} else {
			Integer maxParentLevel = getResourceLevel(resParent.get(0).getParentResource().getId());
			for(int i = 1; i < resParent.size(); i++) {
				Integer lvl = getResourceLevel(resParent.get(i).getParentResource().getId());
				if(lvl > maxParentLevel) {
					maxParentLevel = lvl;
				}
			}
			res.setLevel(maxParentLevel + 1);
		}
		res = resourceRepository.save(res);
		return res.getLevel();
	}

}
