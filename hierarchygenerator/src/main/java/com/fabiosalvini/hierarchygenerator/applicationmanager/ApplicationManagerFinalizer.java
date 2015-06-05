package com.fabiosalvini.hierarchygenerator.applicationmanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;

@Service
public class ApplicationManagerFinalizer implements ApplicationListener<ContextClosedEvent> {
	
	private static boolean executedEnd = false;
	
	@Autowired
	ApplicationManager applicationManager;

	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		if(!executedEnd) {
			applicationManager.stop();
			executedEnd = true;
		}
	}
}
