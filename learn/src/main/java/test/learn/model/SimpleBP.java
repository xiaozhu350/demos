package test.learn.model;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import test.learn.exception.InitException;
import test.learn.util.Function;

public class SimpleBP {

    private Brain brain = null;
    private Map<double[], double[]> dataMap = null;

    public SimpleBP() {
    }

    public void initBrain(int inputSize, int hideSize, int outputSize, double e) {
        this.brain = new Brain(inputSize, hideSize, outputSize, e);
    }

    public void initData(Map<double[], double[]> dataMap) {
        this.dataMap = dataMap;
    }

    public double train(int times) {
        if (brain == null) {
            throw new InitException("[ERROR]brain not init!");
        }
        if (dataMap == null) {
            throw new InitException("[ERROR]dataMap not init!");
        }
        Set<Entry<double[], double[]>> entrySet = dataMap.entrySet();
        for (Map.Entry<double[], double[]> entry : entrySet) {
            if (entry.getKey().length != brain.inputSize || entry.getValue().length != brain.outputSize) {
                throw new InitException("[ERROR]dataMap not match the current brain!");
            }
        }

        entrySet = dataMap.entrySet();
        double result = 0.0d;
        while (times-- > 0) {
            for (Map.Entry<double[], double[]> entry : entrySet) {
                result = brain.train(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    public double[] getResult(double[] input) {
        return brain.expect(input);
    }

    private class Brain {

        private int inputSize;
        private int hideSize;
        private int outputSize;

        /**
         * weight matrix from input to hide
         */
        private double[][] w_ih;
        /**
         * weight matrix from hide to output
         */
        private double[][] w_ho;
        /**
         * threshold from input to hide
         */
        private double[] b_ih;
        /**
         * threshold from hide to output
         */
        private double[] b_ho;

        /**
         * learning rate from input to hide
         */
        private double e_ih;
        /**
         * learning rate from hide to output
         */
        private double e_ho;

        private Random random;

        private Brain(int inputSize, int hideSize, int outputSize, double e) {
            init(inputSize, hideSize, outputSize, e);
        }

        private void init(int inputSize, int hideSize, int outputSize, double e) {
            this.inputSize = inputSize;
            this.hideSize = hideSize;
            this.outputSize = outputSize;
            this.e_ih = e;
            this.e_ho = e;

            w_ih = new double[inputSize][hideSize];
            w_ho = new double[hideSize][outputSize];
            b_ih = new double[hideSize];
            b_ho = new double[outputSize];

            random = new Random();
            initW(w_ih);
            initW(w_ho);
            initB(b_ih);
            initB(b_ho);
        }

        private void initW(double[][] w) {
            int jSize = w[0].length;
            int iSize = w.length;
            double nextDouble;
            for (int i = 0; i < iSize; i++) {
                for (int j = 0; j < jSize; j++) {
                    nextDouble = random.nextDouble();
                    while (nextDouble == 0.0d) {
                        nextDouble = random.nextDouble();
                    }
                    w[i][j] = nextDouble * 2 - 1;
                }
            }
        }

        private void initB(double[] b) {
            int size = b.length;
            double nextDouble;
            for (int i = 0; i < size; i++) {
                nextDouble = random.nextDouble();
                while (nextDouble == 0.0d) {
                    nextDouble = random.nextDouble();
                }
                b[i] = nextDouble * 2 - 1;
            }
        }

        public double train(double[] inputData, double[] resultData) {
            double err = 0.0d;

            double[] hide = new double[hideSize];
            double[] output = new double[outputSize];

            double sum;
            for (int i = 0; i < hideSize; i++) {
                sum = 0;
                for (int j = 0; j < inputSize; j++) {
                    sum += inputData[j] * w_ih[j][i];
                }
                sum += b_ih[i];
                hide[i] = Function.sigmoid(sum);
            }

            for (int i = 0; i < outputSize; i++) {
                sum = 0;
                for (int j = 0; j < hideSize; j++) {
                    sum += hide[j] * w_ho[j][i];
                }
                sum += b_ho[i];
                output[i] = Function.sigmoid(sum);
            }

            double[] reviceOutput = new double[outputSize];
            for (int i = 0; i < outputSize; i++) {
                reviceOutput[i] = output[i] * (1 - output[i]) * (resultData[i] - output[i]);
                err += Math.pow((resultData[i] - output[i]), 2);
            }

            double[] reviceHide = new double[hideSize];
            for (int i = 0; i < hideSize; i++) {
                sum = 0;
                for (int j = 0; j < outputSize; j++) {
                    sum += reviceOutput[j] * w_ho[i][j];
                }
                reviceHide[i] = hide[i] * (1 - hide[i]) * sum;
            }

            for (int i = 0; i < outputSize; i++) {
                for (int j = 0; j < hideSize; j++) {
                    w_ho[j][i] += e_ho * reviceOutput[i] * hide[j];
                }
                b_ho[i] = e_ho * reviceOutput[i];
            }

            for (int i = 0; i < hideSize; i++) {
                for (int j = 0; j < inputSize; j++) {
                    w_ih[j][i] += e_ih * reviceHide[i] * inputData[j];
                }
                b_ih[i] = e_ih * reviceHide[i];
            }
            return err;
        }

        public double[] expect(double[] input) {
            double[] exceptData = new double[outputSize];

            double[] hide = new double[hideSize];
            for (int i = 0; i < hideSize; i++) {
                hide[i] = 0.0d;
                for (int j = 0; j < inputSize; j++) {
                    hide[i] += input[j] * w_ih[j][i];
                }
                hide[i] += b_ih[i];
                hide[i] = Function.sigmoid(hide[i]);
            }
            for (int i = 0; i < outputSize; i++) {
                exceptData[i] = 0.0d;
                for (int j = 0; j < hideSize; j++) {
                    exceptData[i] += hide[j] * w_ho[j][i];
                }
                exceptData[i] += b_ho[i];
                exceptData[i] = Function.sigmoid(exceptData[i]);
            }
            return exceptData;
        }
    }
}