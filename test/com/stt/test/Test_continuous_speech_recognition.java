package com.stt.test;

import org.ava.eventhandling.STTEventBus;
import org.ava.eventhandling.UtteranceRecognizedEvent;
import org.ava.eventhandling.UtteranceRecognizedListener;

import com.stt.STTPluginImplementation;

public class Test_continuous_speech_recognition {

	public static void main(String[] args) {
		
		STTPluginImplementation s = new STTPluginImplementation(); 
		
		System.out.println(">>> Testmain: Starting recognizer");
		s.start();
		
		System.out.println(">>> Talk to your computer...");

		
		
		STTEventBus seb = STTEventBus.getInstance();
		
		UtteranceRecognizedListener ucl = new UtteranceRecognizedListener() {
			@Override
			public void processRecognizedUtterance(UtteranceRecognizedEvent event) {
				System.out.println("Utterance '" + event.getUtterance() + "' processed.");
			}};
			
		seb.registerUtteranceRecognizedListener(ucl);
	}

}
