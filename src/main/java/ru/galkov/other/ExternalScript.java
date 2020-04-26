package ru.galkov.other;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ExternalScript {

	
	
	
	 public static int executeExternalScript(String path) 	 {
		 
		 int exitVal = 0;
		 
		 try { 

			ProcessBuilder proc = new ProcessBuilder(path); 
			proc.redirectErrorStream(true);
		  
		  	Process process = proc.start();
		  
		  	BufferedReader brStdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
		 
		  	String line = null;
		  	while((line = brStdout.readLine()) != null) {   System.out.println(line);   }
		   
		  	exitVal = process.waitFor();
		  	
		  	brStdout.close();

		 }
		 catch (InterruptedException | IOException e ) { e.printStackTrace(); }
		  	return exitVal;
	 }
	
}