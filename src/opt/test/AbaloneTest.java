package opt.test;

import opt.*;
import opt.example.*;
import opt.ga.*;
import shared.*;
import func.nn.backprop.*;

import java.util.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.*;

/**
 * TODO: how to split into training/test?
 * TODO: cross validation sets?
 * 
 * TODO: part 1: test first 3 algorithms (no MIMIC) on a dataset
 * TODO: part 2: find 3 fitness function problems and test all 4 algorithms.
 */

/**
 * Implementation of randomized hill climbing, simulated annealing, and genetic algorithm to
 * find optimal weights to a neural network that is classifying one of the following:
 * 	1. abalone as having either fewer or more than 15 rings.
 * 	2. all possible tic tac toe end game board positions where it attempts to classify if player X won or not.
 * 	3. nursery school application rankings and classifies what the rank of each application is (this data-set isn't converted correctly, so this won't work right).
 *
 * @author Hannah Lau
 * @author Mohammad Hassan Salim
 * @version 1.0
 */
public class AbaloneTest {
	// Select which data-set you want to run!
    //private static Instance[] instances = initializeExampleInstances();
    private static Instance[] instances = initializeTicTacToeInstances();
    //private static Instance[] instances = initializeNurseryInstances();    
    
    private static int inputLayer = 27, hiddenLayer = 5, outputLayer = 1, trainingIterations = 1000;
    
    // Note: although we are using a back propagation factory to build back propagation ANNs,
    // we are not using the back propagation technique as far as I can tell.
    private static BackPropagationNetworkFactory factory = new BackPropagationNetworkFactory();
    
    private static ErrorMeasure measure = new SumOfSquaresError();

    private static DataSet set = new DataSet(instances);
    
    private static int numberOfAlgorithmsToRun = 13;

    // See comment above on back propagation.
    private static BackPropagationNetwork networks[] = new BackPropagationNetwork[numberOfAlgorithmsToRun];
    private static NeuralNetworkOptimizationProblem[] nnop = new NeuralNetworkOptimizationProblem[numberOfAlgorithmsToRun];

    private static OptimizationAlgorithm[] oa = new OptimizationAlgorithm[numberOfAlgorithmsToRun];
    private static String[] oaNames = {
    		"RHC", 
    		"SA", 
    		"SA-High-Heating", 
    		"SA-Low-Cooling", 
    		"SA-High-Heating-Low-Cooling", 
    		"GA", 
    		"GA-High-Population", 
    		"GA-High-Mating", 
    		"GA-High-Mutation",
    		"GA-High-Population-High-Mating",
    		"GA-High-Population-High-Mutation", 
    		"GA-High-Mating-High-Mutation",
    		"GA-High-Population-High-Mating-High-Mutation"
    		};
    private static String results = "";

    private static DecimalFormat df = new DecimalFormat("0.000");

    public static void main(String[] args) {
    	// Create neural networks for each algorithm.
        for(int i = 0; i < oa.length; i++) {
        	// Create a neural network with layer information.
        	int[] layers = new int[] {inputLayer, hiddenLayer, outputLayer};
            networks[i] = factory.createClassificationNetwork(layers);
            
            // Create an ANN optimization problem object with the data-set, ANN, and error measure function.
            nnop[i] = new NeuralNetworkOptimizationProblem(set, networks[i], measure);
        }

        // Create each randomization optimization algorithm
        
        // Randomized hill climbing.
        // No configurations.
        oa[0] = new RandomizedHillClimbing(nnop[0]);
        
        // Simulated annealing.
        // Default configurations.
        oa[1] = new SimulatedAnnealing(1E11, .95, nnop[1]);
        // Higher heating.
        oa[2] = new SimulatedAnnealing(1E2, .95, nnop[2]);
        // Lower cooling
        oa[3] = new SimulatedAnnealing(1E11, .75, nnop[3]);
        // Higher heating and lower cooling
        oa[4] = new SimulatedAnnealing(1E2, .75, nnop[4]);
        
        // Genetic algorithms.
        // Default configurations.
        oa[5] = new StandardGeneticAlgorithm(200, 100, 10, nnop[5]);
        // Higher population.
        oa[6] = new StandardGeneticAlgorithm(400, 100, 10, nnop[6]);
        // Higher mating.
        oa[7] = new StandardGeneticAlgorithm(200, 150, 10, nnop[7]);
        // Higher mutation.
        oa[8] = new StandardGeneticAlgorithm(200, 100, 50, nnop[8]);
		// High population and high mating
        oa[9] = new StandardGeneticAlgorithm(400, 150, 10, nnop[9]);
        // High population and high mutation
        oa[10] = new StandardGeneticAlgorithm(400, 100, 50, nnop[10]);
        // High mating and high mutation
        oa[11] = new StandardGeneticAlgorithm(200, 150, 50, nnop[11]);
        // High population, high mating, and high mutation
        oa[12] = new StandardGeneticAlgorithm(400, 150, 50, nnop[12]);

        // For each randomized algorithm
        for(int i = 0; i < oa.length; i++) {
        	// Train with training set.
            double start = System.nanoTime(), end, trainingTime, testingTime, correct = 0, incorrect = 0;
            train(oa[i], networks[i], oaNames[i]); //trainer.train();
            end = System.nanoTime();
            trainingTime = end - start;
            trainingTime /= Math.pow(10,9);

            // Set weights to the neural network.
            Instance optimalInstance = oa[i].getOptimal();
            networks[i].setWeights(optimalInstance.getData());

            // Test with testing set.
            double predicted, actual;
            start = System.nanoTime();
            for(int j = 0; j < instances.length; j++) {
                networks[i].setInputValues(instances[j].getData());
                networks[i].run();

                actual = Double.parseDouble(instances[j].getLabel().toString());
                predicted = Double.parseDouble(networks[i].getOutputValues().toString());
                
                @SuppressWarnings("unused")
				double trash = Math.abs(predicted - actual) < 0.5 ? correct++ : incorrect++;
            }
            end = System.nanoTime();
            testingTime = end - start;
            testingTime /= Math.pow(10,9);

            // Output results.
            results +=  "\nResults for " + oaNames[i] + ": \nCorrectly classified " + correct + " instances." +
                        "\nIncorrectly classified " + incorrect + " instances.\nPercent correctly classified: "
                        + df.format(correct/(correct+incorrect)*100) + "%\nTraining time: " + df.format(trainingTime)
                        + " seconds\nTesting time: " + df.format(testingTime) + " seconds\n";
        }

        System.out.println(results);
        
        // Store overall results into a file.
		String newFilePath = "overall-results.txt" ;						
		try (PrintStream out = new PrintStream(new FileOutputStream(newFilePath))) {
		    out.println(results);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    private static void train(OptimizationAlgorithm oa, BackPropagationNetwork network, String oaName) {
        System.out.println("\nError results for " + oaName + "\n---------------------------");

        // Keep track of error results.
        ArrayList<String> errorResults = new ArrayList<String>();
        
        // Train the number of iterations given.
        for(int i = 0; i < trainingIterations; i++) {
            oa.train();

            double error = 0;
            for(int j = 0; j < instances.length; j++) {
                network.setInputValues(instances[j].getData());
                network.run();

                Instance output = instances[j].getLabel(), example = new Instance(network.getOutputValues());
                example.setLabel(new Instance(Double.parseDouble(network.getOutputValues().toString())));
                error += measure.value(output, example);
            }

            String errorString = df.format(error);
            System.out.println(errorString);
            errorResults.add(errorString);
        }
        
        // Store error results into a file.
		String newFilePath = oaName + "-error-results.txt" ;				
		Path file = Paths.get(newFilePath);
		try {
			Files.write(file, errorResults, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private static Instance[] initializeInstances(String filePath, int samples, int numberOfAttributes, double classificationMin, double classificationMax) {
        double[][][] dataRow = new double[samples][][];

        BufferedReader br = null;
        Scanner scan = null;
        try {
            br = new BufferedReader(new FileReader(new File(filePath)));

            // For each number of samples.
            for(int i = 0; i < dataRow.length; i++) {
            	// Grab row and split by comma.
                scan = new Scanner(br.readLine());
                scan.useDelimiter(",");

                // Each data row has 2 arrays.
                dataRow[i] = new double[2][];
                
                // First for attributes
                dataRow[i][0] = new double[numberOfAttributes];
                
                // Second for output/classification
                dataRow[i][1] = new double[1];

                // Scan attribute values for this row.
                for(int j = 0; j < numberOfAttributes; j++) {
                    dataRow[i][0][j] = Double.parseDouble(scan.next());
                }
                
                // Scan output/classification result for this row.
                dataRow[i][1][0] = Double.parseDouble(scan.next());
            }
            
        	// Correctly close the buffer reader if it exists.
        	if (br != null) {
        		br.close();
        	}
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
        	// Correctly close the scanner if it exists.
        	if (scan != null) {
        		scan.close();
        	}
        }

        Instance[] instances = new Instance[dataRow.length];

        // Classifications range from X to Y so split in middle.
        // Ex: Abalone data classification ranges from 0 to 30
        // So split into 0 - 14 and 15 - 30
        double splitClassification = classificationMax / 2.0;

        // Iterate each instance and label it's classification value.
        for(int i = 0; i < instances.length; i++) {
            instances[i] = new Instance(dataRow[i][0]);

            // Normalize classification value.
            int normalizedClassification = dataRow[i][1][0] < splitClassification ? 0 : 1;
            instances[i].setLabel(new Instance(normalizedClassification));
        }
        
        return instances;
    }

    private static Instance[] initializeTicTacToeInstances() {
    	// 9 attributes * 3 unique values = 27 binary features
    	return initializeInstances("tic-tac-toe-binary.txt", 958, 27, 0, 1);
    }

    private static Instance[] initializeNurseryInstances() {
    	/**
    	 * @attribute parents {usual,pretentious,great_pret}
		 * @attribute has_nurs {proper,less_proper,improper,critical,very_crit}
		 * @attribute form {complete,completed,incomplete,foster}
		 * @attribute children {1,2,3,more}
		 * @attribute housing {convenient,less_conv,critical}
		 * @attribute finance {convenient,inconv}
		 * @attribute social {nonprob,slightly_prob,problematic}
		 * @attribute health {recommended,priority,not_recom}
		 * @attribute Class {not_recom,recommend,very_recom,priority,spec_prior}
		 * 
		 * 8 attributes => 3 + 5 + 4 + 4 + 3 + 2 + 3 + 3 = 27 binary features
    	 */
    	return initializeInstances("nursery-binary.txt", 12960, 27, 0, 1);
    }
    
    private static Instance[] initializeExampleInstances() {
    	return initializeInstances("src/opt/test/abalone.txt", 4177, 7, 0, 30);
    }
}
