package com.fabiosalvini.hierarchygenerator.commandline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class CommandLineInterface {

	private static final Logger log = LoggerFactory.getLogger(CommandLineInterface.class);

	@Autowired
	protected ApplicationContext applicationContext;


	public CommandLineInterface() {
	}

	public void start() {
	}

	public void stop() {
	}

}
