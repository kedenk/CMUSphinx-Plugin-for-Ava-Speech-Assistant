package com.stt;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ava.pluginengine.PluginState;
import org.ava.pluginengine.STTPlugin;
import org.ava.util.PropertiesFileLoader;
import com.stt.data.Data;
import com.stt.data.SRConfigData;
import com.stt.sphinx.ResultListener;
import com.stt.sphinx.STTWrapper;

import edu.cmu.sphinx.linguist.g2p.Path;


/**
 * The <code>STTPluginImplementation</code> implements the STTPlugin interface of the speech assistant Ava. 
 * The class contains methods to initialize, start, interrupt, continuou and stop the recognizer. 
 * 
 * @author Kevin
 * @since 2016-03-27
 * @version 1
 */
public class STTPluginImplementation implements STTPlugin {

	final Logger log = LogManager.getLogger();
	final int waitForThreadTermination = 500; 
	
	private STTWrapper stt; 
	private Thread asrThread; 
	
	private java.nio.file.Path CONFIG_PATH; 
	private String CONFIG_NAME = "sttPlugin.properties";
	
	private ResultListener listener; 
	
	public STTPluginImplementation() {
		
		this.stt = new STTWrapper(); 
		this.listener = new ResultListener(); 
		this.stt.setResultListener(this.listener);
		this.asrThread = new Thread(this.stt); 
		this.asrThread.setName("Sphinx4_Thread");
		
	}
	
	@Override
	public void start() {
		log.info("Starting the speech recognition plugin.");
		
		log.debug("Loading the properties file for different needed data: " + Data.confDir + Data.confFileName);
		
		// initialize the config file path
		try {
		    java.nio.file.Path basePath = new File(STTPluginImplementation.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toPath().getParent();
		    CONFIG_PATH = Paths.get(basePath.toString(), "/res/" + this.CONFIG_NAME);
		} catch (URISyntaxException e) {
		    log.error("Error while creating the spotify configuration file path: " + e.getMessage());
		    log.catching(Level.DEBUG, e);
		}
		
		PropertiesFileLoader fileLoader = new PropertiesFileLoader(CONFIG_PATH); 
		if( !fileLoader.readPropertiesFile() )	{
			log.fatal("Error while loading the STTPlugin properties file. Plugin can't be started.");
			return; 
		}
		
		if( !prepareDataObject(fileLoader) )  {
			log.fatal("Error while loading and validating the property file for the speech recognition plugin.");
			return;
		}
		fileLoader = null; 
		
		if( !this.stt.init(Data.srConf) )
			return; 
		
		try {
			
			log.debug("Starting the recognition");
			// Starting the speech recognition api
			this.asrThread.start();
			//this.stt.startRecognition();
			
		} catch (IllegalThreadStateException e) {
			log.catching(Level.DEBUG, e);
			log.fatal(this.asrThread.getName() + " can't be stated. Illegal thread state.");
			log.fatal("Plugin can't be started.");
			if( this.stt != null )
				this.stt = null; 
			
			return; 
		} 
		
		//this.stt.startRecognition();		
		// TODO evtl warten, bis sr bereit ist
	}

	@Override
	public void stop() {
		log.debug("Closing speech recognizer.");
		this.stt.closeRecognizer();
		log.info("Speech recognizer closed");
		
		try {
			if( this.asrThread != null ) {
				log.debug("Waiting for termination of " + this.asrThread.getName());
				this.asrThread.join(this.waitForThreadTermination);
				log.debug(this.asrThread.getName() + " terminated.");
			}
			
			if( this.stt != null ) {
				this.stt = null; 
			}
			
		} catch (InterruptedException e) {
			log.catching(Level.DEBUG, e);
			log.error("Error while waiting for termination of the " + this.asrThread.getName() + " thread."); 
		}
	}

	@Override
	public void continueExecution() {
		log.info("Continuou speech recognition.");
		if( this.stt == null ) {
			log.error("Speech recognition plugin is not started. Can't continuou recognition.");
			return; 
		}
		
		if( Data.getPluginState() == PluginState.RUNNING ) {
			log.debug("Can't continuou speech recognition. Recognizer is already running.");
			return; 
		}

		this.stt.continueRecognition(); 		
	}

	@Override
	public void interruptExecution() {
		log.info("Speech recognizer will be interrupt.");
		if( this.stt == null ) {
			log.error("Speech recognition plugin is not started. Can't interrupt recognition.");
			return; 
		}
		
		if( Data.getPluginState() == PluginState.INTERRUPTED ) {
			log.debug("Can't interrupt speech recognition. Recognizer is already interrupted.");
			return; 
		}
		
		Data.setPluginState(PluginState.INTERRUPTED);
	}
	
	private boolean prepareDataObject(PropertiesFileLoader fileLoader) {
		
		ArrayList<String> neededProperties = new ArrayList<String>(); 		
		neededProperties.add("name"); 
		neededProperties.add("version"); 
		neededProperties.add("acousticModelPath"); 
		neededProperties.add("languageModelPath"); 
		neededProperties.add("dictPath"); 
		neededProperties.add("CMULogLevel");
		
		if( fileLoader.isPropertiesFileValid(neededProperties) != null ) 
			return false; 
		

		SRConfigData srData = new SRConfigData(); 
		srData.acousticModelPath = fileLoader.getPropertie("acousticModelPath"); 
		srData.languageModelPath = fileLoader.getPropertie("languageModelPath"); 
		srData.dictionaryPath = fileLoader.getPropertie("dictPath"); 
		srData.cmuSphinxLogLevel = fileLoader.getPropertie("CMULogLevel"); 
		
		Data.srConf = srData; 
		Data.name = fileLoader.getPropertie("name"); 
		Data.version = fileLoader.getPropertie("version"); 
		
		neededProperties = null; 
		return true; 
	}

	@Override
	public String requestText() {
		if( this.asrThread == null || this.stt == null ) {
			log.error("Speech recognition is not started. You can't recognize a utterance.");
			return null; 
		}
		//log.info("Setting speech recognition sate to 'INTERRUPT' to recognize a requested utterance.");
		//this.interruptExecution();
		
		this.stt.setIsRequestedResult(true);
		//this.continueExecution();
		String requestedText = this.listener.getResultString();		
		log.debug("Requested text is '" + requestedText + "'");
		return requestedText;
	}

}
