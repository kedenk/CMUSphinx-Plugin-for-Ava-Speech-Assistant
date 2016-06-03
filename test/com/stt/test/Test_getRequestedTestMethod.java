package com.stt.test;

import com.stt.STTPluginImplementation;

public class Test_getRequestedTestMethod {
	
	private static boolean isInterrupted = true; 

	public static void main(String[] args) throws InterruptedException {
		
		STTPluginImplementation s = new STTPluginImplementation(); 
		
		System.out.println(">>> Testmain: Starting recognizer");
		s.start();

		if( isInterrupted )
			s.interruptExecution();
		
		System.out.println("Please wait 3 seconds.");
		Thread.sleep(3000);
		
		System.out.println("Say something...");
		String utterance = s.requestText(); 
		System.out.println("Requested utterance: " + utterance);
	}

}
