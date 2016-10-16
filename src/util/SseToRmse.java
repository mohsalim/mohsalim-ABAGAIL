package util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

public class SseToRmse {
	
	public static double[] calculateRmse(double[] errors, int numberOfInstances) {
		double n = (double) numberOfInstances;
		double[] rmse = new double[errors.length];
		for(int i = 0; i < rmse.length; i++) {
			rmse[i] = Math.sqrt(errors[i] / n);
		}
		return rmse;
	}
	
	public static double[] getSseFromFile(String filePath) {
		ArrayList<Double> values = new ArrayList<Double>();
		
		// Read file.
		try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
		    String line;
		    
		    // Iterate each line of the file.
		    while ((line = br.readLine()) != null) {
		    	// Convert each line to Double.
		    	try {
		    		Double sse = Double.parseDouble(line);
		    		values.add(sse);
		    	} catch(Exception e) {
		    		System.out.println("Line is not a double: " + line);
		    		e.printStackTrace();
		    	}
		    }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Convert Doubles array list to primitive double array.
		double[] ret = new double[values.size()];
		for(int i = 0; i < ret.length; i++) {
			ret[i] = values.get(i).doubleValue();
		}
		
		// Return SSE array.
		return ret;
	}
	
	public static void saveRmseToFile(double[] rmse, String filePath) {
		ArrayList<String> rmseString = new ArrayList<String>();
		
		// Convert doubles to strings array list.
		for(int i = 0; i < rmse.length; i++) {
			rmseString.add("" + rmse[i]);
		}
		
		// Save file.
		Path file = Paths.get(filePath);
		try {
			Files.write(file, rmseString, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void sseFileToRmseFile(String sseFilePath, int numberofInstances, String rmseFilePath) {
		double[] sse = getSseFromFile(sseFilePath);
		double[] rmse = calculateRmse(sse, numberofInstances);
		saveRmseToFile(rmse, rmseFilePath);
	}
	
	public static void main(String[] args) {
		// Counted the number of valid lines from tic-tac-toe-binary file.
		int numberOfInstances = 958;
		try(Stream<Path> paths = Files.walk(Paths.get("./results/final results/"))) {
		    paths.forEach(filePath -> {
		    	if (Files.isRegularFile(filePath)) {		    
		    		String filePathString = filePath.toString();
		    		String suffix = "error-results.txt";
		    		if(filePathString.endsWith(suffix)) {    		
		    			System.out.println(filePathString);
		    			int suffixIndex = filePathString.indexOf("error-results.txt");
		    			String rmseFile = filePathString.substring(0, suffixIndex) + "rmse-" + suffix;
		    			sseFileToRmseFile(filePathString, numberOfInstances, rmseFile);
		    		}
		    	}       
		    });
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

}
