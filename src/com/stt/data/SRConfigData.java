package com.stt.data;


/**
 * This class wrap up data for the speech recognition api. 
 * Containing data are: dictionary, language model and acoustic model paths. 
 * 
 * @author Kevin
 * @since 2016-03-27
 * @version 1
 * 
 */
public class SRConfigData {
	
	public String dictionaryPath; 
	public String languageModelPath; 
	public String acousticModelPath; 
	
	public String cmuSphinxLogLevel = "OFF"; 
	
	public SRConfigData() {}
	
	public SRConfigData( String dictionaryPath, String languageModelPath, String acousticModelPath, String cmuSphinxLogLevel ) 
	{
		this.dictionaryPath = dictionaryPath; 
		this.languageModelPath = languageModelPath; 
		this.acousticModelPath = acousticModelPath; 
		
		if( cmuSphinxLogLevel != null )
			this.cmuSphinxLogLevel = cmuSphinxLogLevel; 
	}

}
