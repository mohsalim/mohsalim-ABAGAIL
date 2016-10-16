package util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import opt.EvaluationFunction;
import opt.OptimizationAlgorithm;
import shared.FixedIterationTrainer;

public class RunLogFitnessFunction {
    public static void run(OptimizationAlgorithm oa, String oaName, int iterations, EvaluationFunction ef, String problemName) {
    	System.out.println("Starting: " + oaName);
        ArrayList<String> values = new ArrayList<String>();
        ArrayList<String> times = new ArrayList<String>();
        for(int i = 0; i <= iterations; i++) {
        	System.out.println(oaName + " i: " + i);
            long startTime = System.currentTimeMillis();
            FixedIterationTrainer fit = new FixedIterationTrainer(oa, i);
            fit.train();
            long endTime = System.currentTimeMillis() - startTime;

            values.add("" + ef.value(oa.getOptimal()));
            times.add("" + endTime);
        }
		String newFilePath = oaName + "-fitness-" + problemName + ".txt" ;				
		Path file = Paths.get(newFilePath);
		try {
			Files.write(file, values, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		newFilePath = oaName + "-clock-time-" + problemName + ".txt" ;				
		file = Paths.get(newFilePath);
		try {
			Files.write(file, times, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
    	System.out.println("Ending: " + oaName);
    } 
}
