package algorithms;

import dataprocessors.AppData;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.sun.deploy.trace.Trace.flush;

/**
 * @author Ritwik Banerjee
 */
public class  RandomClassifier extends Classifier{

    private static final Random RAND = new Random();

    @SuppressWarnings("FieldCanBeLocal")
    // this mock classifier doesn't actually use the data, but a real classifier will
    private DataSet dataset;
    private final int maxIterations;
    private final int updateInterval;

    // currently, this value does not change after instantiation
    private final AtomicBoolean tocontinue;

    private List<Double> xIntercepts = new ArrayList<>();
    private List<Double> yIntercepts = new ArrayList<>();
    private List<Integer> constants = new ArrayList<>();

    public List<Double> getxIntercepts() {
        return xIntercepts;
    }

    public List<Double> getyIntercepts() {
        return yIntercepts;
    }

    @Override
    public int getMaxIterations() {
        return maxIterations;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public boolean tocontinue() {
        return tocontinue.get();
    }

    public RandomClassifier(DataSet dataset, int maxIterations, int updateInterval, boolean tocontinue) {
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
    }

    public void addInterceptsToSeries(int xCoefficient, int yCoefficient, int constant){

        if(!(xCoefficient < 0)) {
            double xIntercept = ((double)constant/xCoefficient); //(xIntercept, 0)
            xIntercepts.add(xIntercept);
        }else
            xIntercepts.add(0.0);
        double yIntercept = ((double)constant/yCoefficient); //(0, yIntercept)
        yIntercepts.add(yIntercept);

    }



    @Override
    public void run() {
        for (int i = 1; i <= maxIterations && tocontinue(); i++) {
            int xCoefficient = new Long(-1 * Math.round((2 * RAND.nextDouble() - 1) * 10)).intValue();
            int yCoefficient = 10;
            int constant = RAND.nextInt(11);

            // this is the real output of the classifier
            output = Arrays.asList(xCoefficient, yCoefficient, constant);
            addInterceptsToSeries(xCoefficient, yCoefficient, constant);


            // everything below is just for internal viewing of how the output is changing
            // in the final project, such changes will be dynamically visible in the UI
            /*if (i % updateInterval == 0) {
                System.out.printf("Iteration number %d: ", i); //
                flush();
            }
            if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                System.out.printf("Iteration number %d: ", i);
                flush();
                break;
            }
        }*/
        }
    }

    // for internal viewing only
//    protected void flush() {
//        System.out.printf("%d\t%d\t%d%n", output.get(0), output.get(1), output.get(2));
//    }

    // A placeholder main method to just make sure this code runs smoothly *//*
//    public static void main(String... args) throws IOException {
//        DataSet          dataset    = DataSet.fromTSDFile(Paths.get("hw1/out/production/data-vilij/data/sample-data.tsd"));
//        RandomClassifier classifier = new RandomClassifier(dataset, 100, 5, true);
//        classifier.run(); // no multithreading yet
//    }
}
