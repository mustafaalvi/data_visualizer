package algorithms;

import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class RandomClusterer extends Clusterer {

        private DataSet dataset;

        private final int                   maxIterations;
        private final int                   updateInterval;
        private AtomicBoolean tocontinue;
        private boolean                     isContinous;
        private ApplicationTemplate applicationTemplate;
        private LineChart<Number,Number> chart;


        public RandomClusterer(ApplicationTemplate applicationTemplate,DataSet dataset,
                               int maxIterations, int updateInterval,
                               boolean isContinous, int numberOfClusters) {
            super(numberOfClusters);
            this.dataset = dataset;
            this.maxIterations = maxIterations;
            this.updateInterval = updateInterval;
            this.isContinous = isContinous;
            this.applicationTemplate = applicationTemplate;
            this.chart = ((AppUI)(applicationTemplate.getUIComponent())).getChart();
        }

        @Override
        public int getMaxIterations() { return maxIterations; }

        @Override
        public int getUpdateInterval() { return updateInterval; }

        @Override
        public boolean tocontinue() { return tocontinue.get(); }

        public static String getName(){ return "RandomClusterer";}


        /**0. display param dataset data
         * 1. create new dataset instance
         * 2. add new instance to dataset every iteration
         * 3. remove that instance from parm dataset
         * 4. update chart with new dataset data
         */
        @Override
        public void run() {
           // ((AppUI)(applicationTemplate.getUIComponent())).setScrnshotButton(true);
            List<String> instances = dataset.getInstances();
            int instanceIndex = 0;

            for (int i = 1; i <= maxIterations; i++) {
                if(i % updateInterval == 0){
                    System.out.println(instances.get(instanceIndex)+", "+dataset.getLabels().get(instances.get(instanceIndex)));
                    //System.out.println(dataset.toString());
                    String label = Integer.toString((int)(Math.random()* numberOfClusters + 1));
                    dataset.updateLabel(instances.get(instanceIndex), label);
                    //System.out.println(dataset.toString());
                    System.out.println(instances.get(instanceIndex)+", "+dataset.getLabels().get(instances.get(instanceIndex)));
                    instanceIndex++;

                    Platform.runLater(()->{
                        dataset.toChartData(chart,(applicationTemplate.getUIComponent()).getPrimaryScene());
                    });

                    if (!isContinous) {
                        try {
                            ((AppUI) (applicationTemplate.getUIComponent())).pauseAlgorithmn();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }


            }
        }

    }
