package com.fabiosalvini.hierarchygenerator.service;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fabiosalvini.hierarchygenerator.database.model.Dimension;
import com.fabiosalvini.hierarchygenerator.database.model.EntityMapping;
import com.fabiosalvini.hierarchygenerator.database.model.Resource;
import com.fabiosalvini.hierarchygenerator.database.model.ResourceParent;
import com.fabiosalvini.hierarchygenerator.database.repository.DimensionRepository;
import com.fabiosalvini.hierarchygenerator.database.repository.EntityMappingRepository;
import com.fabiosalvini.hierarchygenerator.database.repository.ResourceParentRepository;
import com.fabiosalvini.hierarchygenerator.database.repository.ResourceRepository;

@Service
public class DimensionCreator {
	
	private static final Logger log = LoggerFactory.getLogger(DimensionCreator.class);
	
	@Autowired
	private ResourceRepository resourceRepository;
	@Autowired
	private ResourceParentRepository resourceParentsRepository;
	@Autowired
	private EntityMappingRepository entityMappingRepository;
	@Autowired
	private DimensionRepository dimensionRepository;
	
	@Value("${const.root_name}")
	private String rootName;
	@Value("${const.other_name}")
	private String otherName;
	@Value("${dimensions.threshold}")
	private int threshold;
	
	private int otherCount;

	public DimensionCreator() {
		otherCount = 1;
	}
	
	public void createDimensions() {
		log.info("Creating root resource");
		Resource root = createRoot();
		log.info("Filtering resources");
		filterResources(root);
		log.info("Merging others");
		mergeOthers(root);
		log.info("Finding new root");
		root = findNewRoot(root);
		log.info("Saving dimensions");
		saveDimensions(getMaxLevelDepth(root));
	}
	
	/**
	 * Weight the resources. A resource weight is the number of distinct entities "contained" by the resource.
	 */
	/*private void weigthResources() {
		log.debug("Weighting resources");
		Iterator<Resource> resourcesIter = resourceRepository.findAll().iterator();
		while(resourcesIter.hasNext()) {
			Resource res = resourcesIter.next();
			int resWeight = resourceRepository.getResourceWeigth(res.getId());
			res.setWeight(resWeight);
			res = resourceRepository.save(res);
		}
	}*/
	
	/**
	 * Weight the resources. A resource weight is the number of distinct entities "contained" by the resource.
	 */
	private int filterResources(Resource root) {
		int weight = resourceRepository.getResourceEntitiesCount(root.getId());
		List<ResourceParent> resChildren = resourceParentsRepository.findByParentResource(root);
		for(ResourceParent resPar : resChildren) {
			weight += filterResources(resPar.getChildResource());
		}
		root.setHits(weight);
		if(weight < threshold) {
			root.setLabel(otherName);
		}
		root = resourceRepository.save(root);
		return weight;
	}
	
	private void mergeOthers(Resource root) {
		List<ResourceParent> rootChildren = resourceParentsRepository.findByParentResource(root);
		List<Resource> childrenToMerge = new LinkedList<Resource>();
		for(ResourceParent resPar : rootChildren) {
			Resource childRes = resPar.getChildResource();
			if(otherName.equals(childRes.getLabel())) {
				childrenToMerge.add(childRes);
			} else {
				mergeOthers(childRes);
			}
		}
		if(childrenToMerge.size() > 0) {
			Resource newOther = new Resource();
			newOther.setUrl("http://other/" + otherCount);
			otherCount++;
			newOther.setLabel(otherName);
			newOther = resourceRepository.save(newOther);
			
			for(Resource child : childrenToMerge) {
				List<EntityMapping> mappingsToUpdate = entityMappingRepository.findByResource(child);
				for(EntityMapping eMap : mappingsToUpdate) {
					eMap.setResource(newOther);
					eMap = entityMappingRepository.save(eMap);
				}
				List<ResourceParent> childChildren = resourceParentsRepository.findByParentResource(child);
				for(ResourceParent childPar : childChildren) {
					resourceParentsRepository.delete(childPar);
					
					ResourceParent newResPar = new ResourceParent();
					newResPar.setChildResource(childPar.getChildResource());
					newResPar.setParentResource(newOther);
					newResPar = resourceParentsRepository.save(newResPar);
				}
				
			}
			
			mergeOthers(newOther);
		}
	}
	
	/**
	 * Find the least common ancestor.
	 * @param root root of the hierarchy
	 * @return the new root
	 */
	private Resource findNewRoot(Resource root) {
		List<ResourceParent> rootChildren = resourceParentsRepository.findByParentResource(root);
		if(rootChildren.size() == 1) {
			Resource child = rootChildren.get(0).getChildResource();
			resourceRepository.delete(root);
			return findNewRoot(child);
		} else {
			return root;
		}
	}
	
	/**
	 * Create a common root for all the hierarchies.
	 * @return the newly created root
	 */
	private Resource createRoot() {
		List<Resource> rootResources = resourceRepository.getRootResources();
		Resource root = resourceRepository.findByUrl("http://root");
		if(root == null) {
			root = new Resource();
			root.setLabel(rootName);
			root.setUrl("http://root");
			root = resourceRepository.save(root);
		}
		for(Resource res : rootResources) {
			if(res.getId() != root.getId()) {
				ResourceParent resParent = new ResourceParent();
				resParent.setChildResource(res);
				resParent.setParentResource(root);
				resParent = resourceParentsRepository.save(resParent);
			}
		}
		return root;
	}
	
	/**
	 * Get the maximum hierarchy depth.
	 * @param root root of the hierarchy
	 * @return the maximum hierarchy depth
	 */
	private int getMaxLevelDepth(Resource root) {
		int maxDepth = 0;
		List<ResourceParent> rootChildren = resourceParentsRepository.findByParentResource(root);
		for(ResourceParent resPar : rootChildren) {
			int childDepth = getMaxLevelDepth(resPar.getChildResource());
			if(childDepth > maxDepth) {
				maxDepth = childDepth;
			}
		}
		return maxDepth + 1;
	}
	
	private void saveDimensions(int maxDepth) {
		Iterable<EntityMapping> entitiesMap = entityMappingRepository.findAll();
		for(EntityMapping eMap : entitiesMap) {
			Integer entityId = eMap.getEntityId();
			Resource res = eMap.getResource();
			int levelDistance = 0;
			while(res != null) {
				Dimension dim = new Dimension();
				dim.setEntityId(entityId);
				dim.setLevelName(res.getLabel());
				dim.setLevelDistance(levelDistance);
				dim = dimensionRepository.save(dim);
				List<ResourceParent> resParents = resourceParentsRepository.findByChildResource(res);
				if(resParents != null && resParents.size() > 0) {
					res = resParents.get(0).getParentResource();
				} else {
					res = null;
				}
				levelDistance++;
			}
		}
	}
	
}
