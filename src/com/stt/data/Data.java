package com.stt.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ava.pluginengine.PluginState;


/**
 * This class includes all data, using by this plugin. 
 * 
 * @author Kevin
 * @since 2016-03-27
 * @version 1
 */
public class Data {
	
	final static Logger log = LogManager.getLogger(Data.class); 

	private static Data instance = new Data(); 
	
	public static String version; 
	public static  String name;
	public static String confDir 		= "./res/";  			// TODO �ndern un absoluten pfad
	public static String confFileName	= "sttPlugin.properties"; 
	
	private static PluginState pluginState = null; 
	
	public static SRConfigData srConf = null; 
	
	private Data() {
		
	}
	
	public static Data getInstace() {
		return instance; 
	}
	
	public static PluginState getPluginState() { return pluginState; }
	
	public static void setPluginState(PluginState state) {
		pluginState = state; 
		log.info("Plugin state is set to " + state.toString());
	}
	
	public static void setSRConfigData(SRConfigData conf) {
		if( conf != null )
			srConf = conf; 
	}
}
