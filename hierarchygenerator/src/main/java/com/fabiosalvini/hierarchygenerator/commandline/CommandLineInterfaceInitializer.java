package com.fabiosalvini.hierarchygenerator.commandline;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

@Service
public class CommandLineInterfaceInitializer implements ApplicationListener<ContextRefreshedEvent> {
	
	private static boolean executedInit = false;
	
	@Autowired
	CommandLineInterface commandLineInterface;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if(!executedInit) {
			executedInit = true;
			commandLineInterface.start();
		}
	}
}
