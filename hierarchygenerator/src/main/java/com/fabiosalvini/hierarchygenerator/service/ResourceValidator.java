package com.fabiosalvini.hierarchygenerator.service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResourceValidator{
	
	private static final Logger log = LoggerFactory.getLogger(ResourceValidator.class);
	
	@Autowired
	private DatasetsManager datasetsManager;

	public ResourceValidator() {
	}
	
	/**
	 * Check if the given url is valid
	 * @param resUrl url to check
	 * @return true if the url is valid, false otherwise
	 */
	public boolean isValid(String resUrl) {
		List<String> excludeUrls = datasetsManager.getExcludeUrls();
		for(String url: excludeUrls) {
			Pattern pattern = Pattern.compile(url);
			Matcher matcher = pattern.matcher(resUrl);
			if (matcher.find()) {
				return false;
			}
		}
		return true;
	}
}
