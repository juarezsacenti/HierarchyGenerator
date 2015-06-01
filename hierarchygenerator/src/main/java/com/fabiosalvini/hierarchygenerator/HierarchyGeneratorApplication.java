package com.fabiosalvini.hierarchygenerator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class HierarchyGeneratorApplication {
	
    final static Logger logger = LoggerFactory.getLogger(HierarchyGeneratorApplication.class);
    
	private static final Logger log = LoggerFactory.getLogger(ConfigurableApplicationContext.class);
	
	private static ConfigurableApplicationContext context;
	
	private static Boolean running = true;
	
	public static synchronized void stopApplication() {
		running = false;
	}

	public static void main(String[] args) {
		

		log.info("#####################################################");
		log.info("##########       Hierarchy Generator      ###########");
		log.info("#####################################################");
		
		if(System.getProperty("spring.profiles.active") == null) {
			log.debug("No active profile is found, setting default (development)");
			System.setProperty("spring.profiles.active", "development");
		} 
		
		context = new ClassPathXmlApplicationContext("classpath:spring/application-config.xml");

		if(context.getEnvironment().getActiveProfiles().length == 0) {
			context.getEnvironment().setActiveProfiles(new String [] {"development"});
		}

		if(context.getEnvironment().getActiveProfiles()[0].compareToIgnoreCase("development") == 0) {
			log.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			log.warn("!!!!!!!!!!!!! WARNING: DEVELOPMENT MODE !!!!!!!!!!!!!");
			log.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		}
		
		log.info("Version: {}", getApplicationVersion());
		
		while (context.isActive()) {
			synchronized (running) {
				if(!running) {
					break;
				}
			}
			try {
				Thread.sleep(1000);
			} catch(InterruptedException ex) {
				break;
			}
		}
		
		log.info("Exit from application");
		
	}
	
	public static ApplicationContext getContext() {
		return context;
	}

	private static String getApplicationVersion() {

		/**
		 * Since the context has not been all loaded yet, the only way that we have
		 * to retrieve a value from the property file is to retrieve it "in the old way".
		 * 
		 * Once the context has been completely loaded, we will retrieve properties 
		 * with the annotation @Value(...)
		 */
		
		Properties prop = new Properties();
		String propFileName = "project.properties";

		InputStream inputStream = HierarchyGeneratorApplication.class.getClassLoader().getResourceAsStream(propFileName);

		try {
			if (inputStream != null) {
				prop.load(inputStream);
			}
			return prop.getProperty("project.version");
		} catch(IOException ex) {
			log.error("", ex);
			return null;
		}
	}
	
}
