package test.learn.util;

/**
 * function
 * 
 * @author hades
 */
public class Function {

    /**
     * sigmoid function
     * 
     * @param val
     * @return
     */
    public static double sigmoid(double val) {
        return 1.0d / (1.0d + Math.exp(-val));
    }
}
