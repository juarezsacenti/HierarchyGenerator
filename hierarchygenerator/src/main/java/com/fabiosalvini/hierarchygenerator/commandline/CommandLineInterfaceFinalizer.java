package com.fabiosalvini.hierarchygenerator.commandline;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Service;

@Service
public class CommandLineInterfaceFinalizer implements ApplicationListener<ContextClosedEvent> {
	
	private static boolean executedEnd = false;
	
	@Autowired
	CommandLineInterface commandLineInterface;

	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		if(!executedEnd) {
			commandLineInterface.stop();
			executedEnd = true;
		}
	}
}
