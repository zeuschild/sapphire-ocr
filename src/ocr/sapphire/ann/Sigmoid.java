/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ocr.sapphire.ann;

/**
 *
 * @author Do Bich Ngoc
 */
public class Sigmoid {
    private static final double MAX_RANGE = 25;
    private static final int SIZE = 5001;
    private static double sigmoid[];
    private static double delta;

    static {
        sigmoid = new double[SIZE];
        delta = MAX_RANGE / (SIZE - 1);
        initialize();
    }

    public Sigmoid() {
        
    }

    private static void initialize() {
        double x = 0;
        for(int i = 0; i < SIZE; i++) {
            sigmoid[i] = 1.0 / (1 + Math.exp(-1.0 * x));
            x += delta;
        }
    }

    public static double sigmoid(double x) {
        if (x < -MAX_RANGE) {
            return 0;
        }
        else if (x > MAX_RANGE) {
            return 1;
        }
        else {
            double index = Math.abs(x / delta);
            double a = Math.floor(index);
            double b = Math.ceil(index);
            double sa = sigmoid[(int) a];
            double sb = sigmoid[(int) b];
            double result;
            if (a == b) {
                result = sa;
            }
            else {
                result = (index - a) / (b - a) * (sb - sa) + sa;
            }
            if (x >= 0) {
                return result;
            }
            else {
                return (1 - result);
            }
        }
    }

    public static void main(String args[]) {
        System.out.println(Sigmoid.sigmoid(-9.3211685768));
    }

}
