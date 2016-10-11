package opt.test;

import opt.*;
import opt.example.*;
import opt.ga.*;
import shared.*;
import func.nn.backprop.*;

import java.util.*;
import java.io.*;
import java.text.*;

/**
 * TODO: Add MIMIC algorithm to list
 * TODO: Add my two datasets
 * TODO: Output in correct format (if it already doesn't)
 * TODO: assignment specs say not to use backprop but the neural networks seem to use that here. is there an alternative option?
 * TODO: add a function to convert discrete datasets to binary strings 
 */

/**
 * Implementation of randomized hill climbing, simulated annealing, and genetic algorithm to
 * find optimal weights to a neural network that is classifying abalone as having either fewer 
 * or more than 15 rings. 
 *
 * @author Hannah Lau
 * @author Mohammad Hassan Salim
 * @version 1.0
 */
public class AbaloneTest {
    //private static Instance[] instances = initializeExampleInstances();
    private static Instance[] instances = initializeTicTacToeInstances();
    //private static Instance[] instances = initializeNurseryInstances();    
    
    private static int inputLayer = 8, hiddenLayer = 5, outputLayer = 1, trainingIterations = 1000;
    private static BackPropagationNetworkFactory factory = new BackPropagationNetworkFactory();
    
    private static ErrorMeasure measure = new SumOfSquaresError();

    private static DataSet set = new DataSet(instances);

    private static BackPropagationNetwork networks[] = new BackPropagationNetwork[3];
    private static NeuralNetworkOptimizationProblem[] nnop = new NeuralNetworkOptimizationProblem[3];

    private static OptimizationAlgorithm[] oa = new OptimizationAlgorithm[3];
    private static String[] oaNames = {"RHC", "SA", "GA"};
    private static String results = "";

    private static DecimalFormat df = new DecimalFormat("0.000");

    public static void main(String[] args) {
        for(int i = 0; i < oa.length; i++) {
            networks[i] = factory.createClassificationNetwork(
                new int[] {inputLayer, hiddenLayer, outputLayer});
            nnop[i] = new NeuralNetworkOptimizationProblem(set, networks[i], measure);
        }

        oa[0] = new RandomizedHillClimbing(nnop[0]);
        oa[1] = new SimulatedAnnealing(1E11, .95, nnop[1]);
        oa[2] = new StandardGeneticAlgorithm(200, 100, 10, nnop[2]);

        for(int i = 0; i < oa.length; i++) {
            double start = System.nanoTime(), end, trainingTime, testingTime, correct = 0, incorrect = 0;
            train(oa[i], networks[i], oaNames[i]); //trainer.train();
            end = System.nanoTime();
            trainingTime = end - start;
            trainingTime /= Math.pow(10,9);

            Instance optimalInstance = oa[i].getOptimal();
            networks[i].setWeights(optimalInstance.getData());

            double predicted, actual;
            start = System.nanoTime();
            for(int j = 0; j < instances.length; j++) {
                networks[i].setInputValues(instances[j].getData());
                networks[i].run();

                predicted = Double.parseDouble(instances[j].getLabel().toString());
                actual = Double.parseDouble(networks[i].getOutputValues().toString());

                @SuppressWarnings("unused")
				double trash = Math.abs(predicted - actual) < 0.5 ? correct++ : incorrect++;
            }
            end = System.nanoTime();
            testingTime = end - start;
            testingTime /= Math.pow(10,9);

            results +=  "\nResults for " + oaNames[i] + ": \nCorrectly classified " + correct + " instances." +
                        "\nIncorrectly classified " + incorrect + " instances.\nPercent correctly classified: "
                        + df.format(correct/(correct+incorrect)*100) + "%\nTraining time: " + df.format(trainingTime)
                        + " seconds\nTesting time: " + df.format(testingTime) + " seconds\n";
        }

        System.out.println(results);
    }

    private static void train(OptimizationAlgorithm oa, BackPropagationNetwork network, String oaName) {
        System.out.println("\nError results for " + oaName + "\n---------------------------");

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

            System.out.println(df.format(error));
        }
    }
    
    private static Instance[] initializeInstances(String filePath, int samples, int numberOfAttributes) {
        double[][][] attributes = new double[samples][][];

        BufferedReader br = null;
        Scanner scan = null;
        try {
            br = new BufferedReader(new FileReader(new File(filePath)));

            for(int i = 0; i < attributes.length; i++) {
                scan = new Scanner(br.readLine());
                scan.useDelimiter(",");

                attributes[i] = new double[2][];
                attributes[i][0] = new double[numberOfAttributes]; // 7 attributes
                attributes[i][1] = new double[1];

                for(int j = 0; j < 7; j++) {
                    attributes[i][0][j] = Double.parseDouble(scan.next());
                }
                
                attributes[i][1][0] = Double.parseDouble(scan.next());
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

        Instance[] instances = new Instance[attributes.length];

        for(int i = 0; i < instances.length; i++) {
            instances[i] = new Instance(attributes[i][0]);
            // classifications range from 0 to 30; split into 0 - 14 and 15 - 30
            instances[i].setLabel(new Instance(attributes[i][1][0] < 15 ? 0 : 1));
        }

        return instances;
    }

    private static Instance[] initializeTicTacToeInstances() {
    	return initializeInstances("tic-tac-toe-binary.txt", 958, 9);
    }

    private static Instance[] initializeNurseryInstances() {
    	return initializeInstances("nursery-binary.txt", 12960, 8);
    }
    
    private static Instance[] initializeExampleInstances() {
    	return initializeInstances("src/opt/test/abalone.txt", 4177, 7);
    }
}
