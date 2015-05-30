package com.fabiosalvini.hierarchygenerator.statistics;

import org.springframework.stereotype.Service;

@Service
public class Statistics {
	
	private int sameAsLinksCount;
	private int parentLinksCount;

	public Statistics() {
		sameAsLinksCount = 0;
		parentLinksCount = 0;
	}
	
	public synchronized void addSameAsLink() {
		sameAsLinksCount++;
	}
	
	public synchronized void addParentLink() {
		parentLinksCount++;
	}

}
