package opt.example;

import util.linalg.Vector;
import opt.EvaluationFunction;
import shared.Instance;

/**
 * A continuous peaks function
 * @author Andrew Guillory gtg008g@mail.gatech.edu
 * @version 1.0
 */
public class ContinuousPeaksEvaluationFunction implements EvaluationFunction {
    /**
     * The t value
     */
    private int t;
    
    /**
     * Make a new continuous peaks function
     * @param t the t value
     */
    public ContinuousPeaksEvaluationFunction(int t) {
        this.t = t;
    }

    /**
     * @see opt.EvaluationFunction#value(opt.OptimizationData)
     */
    public double value(Instance d) {
        Vector data = d.getData();
        
        // Handle 0's.
        int max0 = findMaxNumberOfContineousX(0, data);
        
        // Handle 1's.
        int max1 = findMaxNumberOfContineousX(1, data);
        
        // Compare both max values to T.
        int r = 0;
        if (max1 > t && max0 > t) {
            r = data.size();
        }
        return Math.max(max1, max0) + r;
    }
    
    private int findMaxNumberOfContineousX(int x, Vector data) {
        int max = 0;
        int count = 0;
        
        // Iterate vector.
        for (int i = 0; i < data.size(); i++) {
        	// Continue on X trend.
            if (data.get(i) == x) {
                count++;
                continue;
            }
            
            // Trend broken. Check if it's greater than the max trend length.
            if (count > max) {
            	max = count;
                count = 0;
            }
        }
        
        // Check one more time after loop.
        if (count > max) {
            max = count;
        }
        
        return max;
    }
}
