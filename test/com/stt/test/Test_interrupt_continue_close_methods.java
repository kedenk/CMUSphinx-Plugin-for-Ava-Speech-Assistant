package com.stt.test;

import com.stt.STTPluginImplementation;


public class Test_interrupt_continue_close_methods {

	private static final int waitAfterStart = 5000; 
	private static final int waitAfterInterrupt = 10000; 
	private static final int waitBeforeClose = 10000; 
	
	
	public static void main(String[] args) throws Exception {

		STTPluginImplementation s = new STTPluginImplementation(); 
		
		System.out.println(">>> Testmain: Starting recognizer");
		s.start();
		
		Thread.sleep(waitAfterStart);
		
		System.out.println(">>> Testmain: Interrupt recognizer");
		s.interruptExecution();
		
		Thread.sleep(waitAfterInterrupt);
		
		System.out.println(">>> Testmain: Continuou recognizer. In one minute, the plugin will be closed.");
		s.continueExecution();
		
		Thread.sleep(waitBeforeClose);
		System.out.println(">>> Testmain: Plugin will be closed.");
		s.stop();
		System.out.println("EXIT");
		
	}

}
