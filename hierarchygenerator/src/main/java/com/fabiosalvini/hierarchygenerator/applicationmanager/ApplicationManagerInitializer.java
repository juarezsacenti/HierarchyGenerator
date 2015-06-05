package com.fabiosalvini.hierarchygenerator.applicationmanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

@Service
public class ApplicationManagerInitializer implements ApplicationListener<ContextRefreshedEvent> {
	
	private static boolean executedInit = false;
	
	@Autowired
	ApplicationManager applicationManager;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if(!executedInit) {
			executedInit = true;
			applicationManager.start();
		}
	}
}
