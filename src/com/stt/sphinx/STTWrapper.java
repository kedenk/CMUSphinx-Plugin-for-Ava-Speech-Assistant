package com.stt.sphinx;


import java.net.URL;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ava.eventhandling.STTEventBus;
import org.ava.eventhandling.UtteranceRecognizedEvent;
import org.ava.pluginengine.PluginState;
import com.stt.data.Data;
import com.stt.data.SRConfigData;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.InternalConfigurationException;


/**
 * This class wrappes the functionality of the CMU Sphinx4 speech recognition api. 
 * There a several methods to start, interrupt, continuous and close the recognition process. 
 * The first method after you create a instance of this class is the <code>init</code> method.  
 * 
 * This class implements the <code>Runnable</code> interface. So it can be executed in an additional thread. 
 * 
 * @author Kevin
 * @since 2016-03-27
 * @version 1
 */
public class STTWrapper implements Runnable {
	
	final Logger log = LogManager.getLogger(STTWrapper.class); 
	
	private final String sphinxConfigFile = "/edu/cmu/sphinx/api/default.config.xml"; 
	
	private Configuration conf = null; 
	private edu.cmu.sphinx.frontend.InsertableDataBlocker dataInserter = null; 
	private ModifiedLiveSpeechRecognizer recognizer = null; 
	private SRConfigData confData = null; 
	private ResultListener resultListener; 
	
	private boolean isRequestedResult = false; 
	private boolean isClosing = false; 
	
	/**
	 * Event bus which should be triggered if a new utterance is recognized. 
	 */
	private STTEventBus eventBus; 
	
	public STTWrapper() { }
	
	
	@Override
	public void run() {
		this.startRecognition();
	}
	
	
	/**
	 * The <code>init</code> method initialize this class, sets the configuration of the sphinx4 speech api, 
	 * registrate the STTEventBus and starts the recognizer. 
	 * 
	 * @param data The <code>SRConfigData</code> object with data for the speech recognizer. 
	 * @return true, if everything ok. False if something went wrong. 
	 */
	public boolean init(SRConfigData data) {
		
		if( data != null )
			this.confData = data; 
		
		this.conf = new Configuration(); 
		
		URL url = getClass().getResource(this.sphinxConfigFile); 
		ConfigurationManager cm = new ConfigurationManager(url);
		try	{
			java.util.logging.Logger cmuLogger = cm.getRootLogger(); 
			CMULogConfigurator.setCMULogger(cmuLogger); 
			CMULogConfigurator.setCMULogLevel(this.confData.cmuSphinxLogLevel);
			
			this.dataInserter = (edu.cmu.sphinx.frontend.InsertableDataBlocker)cm.lookup("insertableDataBlocker");
			
		} catch (InternalConfigurationException e) {
			log.fatal("Sphinx4 configuration file in the sphinx-core.jar is missing. Plugin will be shutdown.");
			log.catching(Level.DEBUG, e);
			return false; 
		}
		
		this.conf.setAcousticModelPath(this.confData.acousticModelPath);
		this.conf.setDictionaryPath(this.confData.dictionaryPath);
		this.conf.setLanguageModelPath(this.confData.languageModelPath);
		
		try {
			
			this.recognizer = new ModifiedLiveSpeechRecognizer(this.conf);
			
		} catch (Exception e) {
			log.fatal("Error while initialize the CMU Speech Recognizer class (LiveSpeechRecognizer)");
			log.catching(Level.DEBUG, e);
			return false; 
		}
		
		this.eventBus = STTEventBus.getInstance(); 
		
		try {
			this.recognizer.startRecognition(true);
		} catch (Exception e) {
			log.fatal("Error while initialize the CMU Speech Recognizer class (LiveSpeechRecognizer)");
			log.catching(Level.DEBUG, e);
			return false; 
		}
		
		return true; 
	}
	
	
	/**
	 * Call this method to start the recognition of the wrapped speech api. Before you have to initialize
	 * the speech recognition api with the <code>init</code> method of this class. 
	 * This method will run until the plugin state is RUNNING and the variable <code>isClosing</code> is false.  
	 */
	public synchronized void startRecognition() {
		if( this.recognizer == null ) {
			log.error("Please call method 'init' first. Speech Recognizer has to be initialized.");
			return;
		} 

		SpeechResult result; 
		 // System.out.println("Listening...");		
		Data.setPluginState(PluginState.RUNNING); 
		
		while( !isClosing && (Data.getPluginState() == PluginState.RUNNING) && ((result = recognizer.getResult()) != null)) { 

			if( Data.getPluginState() == PluginState.INTERRUPTED ) {
				try {
					// System.out.println(">>>>>>>>>>>>> wait. Recognition loop is interrupted.");
					this.wait();
					continue; 
				} catch (InterruptedException e) {
					log.catching(Level.DEBUG, e);
				} 
			}
			
			String hypothesis = result.getHypothesis(); 
			log.debug("Recognized a utterance: " + hypothesis);
			
			if( this.isRequestedResult ) {
				this.isRequestedResult = false; 
				this.resultListener.addResultString(hypothesis);
			}
			
			if( !hypothesis.equals("") && !this.isRequestedResult && !isClosing ) {
				this.eventBus.fireUtteranceRecognizedEvent(new UtteranceRecognizedEvent(hypothesis));
			}
			
		}
		log.debug("Recognition loop terminated.");
	}
	
	
	/**
	 * This method sets the plugin state to INTERRUPTED and cancel the recognition. 
	 */
	public void stopRecognition() {

		Data.setPluginState(PluginState.INTERRUPTED);
		this.cancelRecognition();
		
//		int count = 0; 
//		while( this.recognizer.getState() != Recognizer.State.READY ) {
//			try {
//				if( count++ >= 10 )
//					break; 
//				Thread.sleep(500); 
//				//System.out.println(">> " + count + "  " + this.recognizer.getState());
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
		
		try {
			this.recognizer.stopRecognition();
		} catch (IllegalStateException e) {
			// There is no other way to exit the speech recognizer while it is recognizing. 
			// After this exception, the plugin will shut down. 
			// System.out.println(">>>>> Exception while stopRecognition()");
		}
	}
	
	
	/**
	 * This method abort the recognition process and close the recognizer. 
	 */
	public void closeRecognizer() {
		this.isClosing = true; 
		this.stopRecognition();
		this.recognizer = null; 
		this.conf = null; 
	}
	
	
	/**
	 * This method continue the interrupted speech recognition. 
	 * Is the speech recognition api already recognizing, nothin will be done. 
	 */
	public synchronized void continueRecognition() {
		log.info("Speech recognition will be continued.");
		Data.setPluginState(PluginState.RUNNING);
		try {
			this.notify();
		} catch (IllegalMonitorStateException e) {
			log.catching(Level.DEBUG, e);
		}
	}
	
	/**
	 * This method calls the injectInterrupt method of the InsertableDataBlocker. 
	 * The method interrupts the blocked getResult() method of the class ModifiedLiveSpeechRecognizer. 
	 */
	private void cancelRecognition() {
		//this.dataInserter.injectInterrupt();
		
//		URL url = getClass().getResource(this.sphinxConfigFile); 
//		ConfigurationManager cm = new ConfigurationManager(url); 
//		Microphone src = (Microphone)cm.lookup("microphone");
//		
//		src.stopRecording();
		this.recognizer.cancelRecognition();

	}
	
	public void setIsRequestedResult(boolean b) {
		this.isRequestedResult = b; 
	}
	
	public boolean isRequestedResult() { return this.isRequestedResult; }
	
	public void setResultListener( ResultListener listener ) {
		if( listener == null ) {
			log.error("Given Parameter 'listener' is null.");
			return; 	
		}
		
		this.resultListener = listener; 
	}

}
