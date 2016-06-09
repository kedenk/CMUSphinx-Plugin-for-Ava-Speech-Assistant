package com.stt.sphinx;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CMULogConfigurator {

	private static CMULogConfigurator instance = new CMULogConfigurator();

	final static Logger log = LogManager.getLogger(CMULogConfigurator.class);


	private static java.util.logging.Logger cmuSphinxLogger;

	private CMULogConfigurator() {}

	public static CMULogConfigurator getInstance() {
		return instance;
	}

	public static void setCMULogger(java.util.logging.Logger cmuLogger) {
		if( cmuLogger == null ) {
			log.error("No valid CMU Sphinx Logger given as parameter. Parameter is null");
			return;
		}

		log.debug("CMU Sphinx logger was set in CMULogConfigurator.");
		cmuSphinxLogger = cmuLogger;
	}

	public static void setCMULogLevel(String level) {
		if( cmuSphinxLogger == null ) {
			log.error("Please call first 'setCMULogger' method with a valid logger.");
			return;
		}

		java.util.logging.Level l = convertStringToLogLevel(level);
		if ( l == null ) {
			log.error("Not a valid log level for CMU SPhinx logger given.");
			return;
		}

		cmuSphinxLogger.setLevel(l);
		log.info("STTPlugin internal CMU SPhinx log level set to " + level);

	}

	private static java.util.logging.Level convertStringToLogLevel(String level) {

		try {
			return java.util.logging.Level.parse(level);
		} catch (Exception e) {
			log.catching(Level.DEBUG, e);
		}
		return null;
	}
}
