package util;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class DiscreteArffToBinary {
	public static void Convert(String arffFilePath) {
		Scanner scanner = null;
		
		try {
			// Create scanner with given file.
			scanner = new Scanner(new File(arffFilePath));
			
			// Flag to keep track if we found the actual data or not in the file.
			boolean foundData = false;
	
			// Attributes list for attribute to binary mapping.
			ArrayList<HashMap<String,String>> attributes = new ArrayList<HashMap<String,String>>();
			
			// Converted data in binary format.
			ArrayList<String> dataBinary = new ArrayList<String>();
			
			// Start to scan the file.
			while (scanner.hasNextLine()) {
				// Grab the line and trim off empty space.
				String line = scanner.nextLine().trim();
				
				// If it's an empty string, the skip.
				if(line.equals("")) {
					continue;
				}
				
				// If we found the data flag, then start to handle data.
				if(foundData) {
					// Get the parts of this data line.
					String[] parts = line.split(",");
					
					// Make sure the data parts is the same size as the attributes.
					if(parts.length != attributes.size()) {
						throw new Exception("Data line has wrong number of attributes: " + parts);
					}
					
					// Convert each data attribute to it's respective binary string.
					String converted = "";
					for(int i = 0; i < parts.length; i++) {
						String dataValue = removeFinalComma(parts[i]);
						String comma = i < parts.length - 1 ? "," : "";
						converted += attributes.get(i).get(dataValue) + comma;
					}
					dataBinary.add(converted);
					
					// Don't worry about looking for anything else.
					continue;
				}
				
				// If it's an comment or table name, then skip.
				if(line.startsWith("%") || line.startsWith("@relation")) {
					continue;
				}
				
				// If it's an attribute,
				if(line.startsWith("@attribute")) {
					// Separate line into parts.
					// Ex: @attribute parents {usual,pretentious,great_pret}
					String[] parts = line.split(" ");
					
					// If there isn't 3 parts, throw error.
					if(parts.length != 3) {
						throw new Exception("Attribute line doesn't have three parts: " + line);
					}
					
					// Make sure the attribute values are formatted correctly.
					if(!parts[2].startsWith("{") || !parts[2].endsWith("}")) {
						throw new Exception("Attribute values are malformed: " + parts[2]);
					}
					
					// Get values from comma separated string.
					String[] values = parts[2].substring(1, parts[2].length() - 1).split(",");
					
					// Add each value with it's paired binary to a dictionary.
					HashMap<String,String> hashValues = new HashMap<String,String>();
					for(int i = 0; i < values.length; i++) {
						String binary = (int)Math.pow(10, i) + "";
						hashValues.put(removeFinalComma(values[i]), binary);
					}
					
					// Add dictionary to the attributes list. This MUST be added in order.
					attributes.add(hashValues);
				}
			
				// Flag if we started to stream data.
				if(line.startsWith("@data")) {
					foundData = true;
				}
			}
			
			// Store data into a file.
			int extensionIndex = arffFilePath.lastIndexOf(".arff");
			String newFilePath = arffFilePath.substring(0, extensionIndex) + "-binary.txt" ;				
			Path file = Paths.get(newFilePath);
			Files.write(file, dataBinary, Charset.forName("UTF-8"));
			/*
			for(int i = 0; i < dataBinary.size(); i++) {
				System.out.println(dataBinary.get(i));
			}
			*/
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Close the scanner properly.
			if(scanner != null) {
				scanner.close();
			}
		}
	}
	
	public static String removeFinalComma(String str) {
		String trimmed = str.trim();
		return trimmed.endsWith(",") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
	}
	
	public static void main(String[] args) {
		System.out.println("Starting conversion");
		Convert("tic-tac-toe.arff");
		Convert("nursery.arff");
		System.out.println("Ending conversion");
	}
}
