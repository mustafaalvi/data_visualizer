package dataprocessors;

import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Point2D;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import settings.AppPropertyTypes;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;

import java.util.*;


import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * The data files used by this data visualization applications follow a tab-separated format, where each data point is
 * named, labeled, and has a specific location in the 2-dimensional X-Y plane. This class handles the parsing and
 * processing of such data. It also handles exporting the data to a 2-D plot.
 * <p>
 * A sample file in this format has been provided in the application's <code>resources/data</code> folder.
 *
 * @author Ritwik Banerjee
 * @see XYChart
 */
public final class TSDProcessor {

    private Map<String, String> dataLabels; //Instance : Label
    private Map<String, Point2D> dataPoints; //Instance : Point
    private Set<String> duplicateSet;


    private SimpleBooleanProperty isValid = new SimpleBooleanProperty();
    private SimpleBooleanProperty exceptionFound = new SimpleBooleanProperty();


    public TSDProcessor() {
        dataLabels = new HashMap<>();
        dataPoints = new HashMap<>();
        duplicateSet = new HashSet<>();
    }

    public Map<String, String> getDataLabels() {
        return dataLabels;
    }

    public Map<String, Point2D> getDataPoints() {
        return dataPoints;
    }

    public static class InvalidDataNameException extends Exception {

        public InvalidDataNameException() {
        }
    }

    private static String nameFormatCheck(String name) throws InvalidDataNameException {
        if (!name.startsWith("@"))
            throw new InvalidDataNameException();
        return name;
    }

    public static class DuplicateNameException extends Exception {

        public DuplicateNameException() {
        }
    }

    public boolean getIsValid() {
        return isValid.getValue();
    }

    public ArrayList<Double> getxValues() {
        return xValues;
    }

    public ArrayList<Double> getyValues() {
        return yValues;
    }

    public double getMaxX(){
       return findMaxX(xValues);
    }
    public double getMinX(){
        return findMinX(xValues);
    }
    public double getMaxY(){
        return findMaxY(yValues);
    }
    public double getMinY() {
        return findMinY(yValues);
    }

    public double findMaxX(ArrayList<Double> xValues){
        double max = Collections.max(xValues);
        return max;
    }
    public double findMinX(ArrayList<Double> xValues){
        double min = Collections.min(xValues);
        return min;
    }
    public double findMaxY(ArrayList<Double> yValues){
        double max = Collections.max(yValues);
        return max;
    }
    public double findMinY(ArrayList<Double> yValues){
        double min = Collections.min(yValues);
        return min;
    }

    private ArrayList<Double> xValues = new ArrayList<>();
    private ArrayList<Double> yValues = new ArrayList<>();





    public double getxAvg() {
        double xAvg = ((getMaxX() + getMinX()) / 2);
        return xAvg;
    }

    public double getyAvg() {
        double yAvg = ((getMaxY() + getMinY()) / 2);
        return yAvg; }

    /**
     * Processes the data and populated two {@link Map} objects with the data.
     *
     * @param tsdString the input data provided as a single {@link String}
     * @throws Exception if the input string does not follow the <code>.tsd</code> data format
     */
    public void processString(String tsdString) throws InvalidDataNameException, DuplicateNameException {
        Stream.of(tsdString.split("\n"))
                .map(line -> Arrays.asList(line.split("\t")))
                .forEach(list -> {
                    try {
                        if (exceptionFound.get())
                            return;
                        String name = nameFormatCheck(list.get(0));
                        if (!(duplicateSet.add(list.get(0)))) {
                            throw new DuplicateNameException();
                        }
                        String label = (list.get(1));
                        String[] pair = (list.get(2)).split(",");
                        Point2D point = new Point2D(Double.parseDouble(pair[0]), Double.parseDouble(pair[1]));

                        xValues.add(point.getX());
                        yValues.add(point.getY());

                        dataLabels.put(name, label);
                        dataPoints.put(name, point);
                        isValid.set(true);
                    } catch (InvalidDataNameException e) {
                        invalidDataDialog();
                        isValid.set(false);
                        exceptionFound.set(true);
                    } catch (DuplicateNameException e) {
                        duplicateDialog(duplicateSet.size() + 1);
                        isValid.set(false);
                        exceptionFound.set(true);
                    }
                });
    }



    /**
     * Exports the data to the specified 2-D chart.
     *
     * @param chart the specified chart
     */
    //IMPLEMENT THIS USING LINECHART VS XY TO GET THE AVERAGE LINE THING
   public void toChartData(XYChart<Number, Number> chart) {
//      int sumOfY = 0;  int counter = 0; int instance = 1;
        chart.getData().clear();
        Set<String> labels = new HashSet<>(dataLabels.values());
        for (String label : labels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);
            dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataPoints.get(entry.getKey());
                series.getData().add(new XYChart.Data<>(point.getX(), point.getY()));
            });
            chart.getData().add(series);

 /*           for (XYChart.Series<Number, Number> s : chart.getData()) {
                for (XYChart.Data<Number, Number> x : s.getData()) {
                    Tooltip.install(x.getNode(), new Tooltip("Instance Value: " + "\n" +
                            x.getXValue().toString() + "," + x.getYValue()));
                    //sumOfY += x.getYValue().intValue(); counter++;

                    x.getNode().setOnMouseEntered(event -> x.getNode().getStyleClass().add("onHover"));
                    x.getNode().setOnMouseExited(event -> x.getNode().getStyleClass().remove("onHover"));
                }
            }*/
            // int average = sumOfY/counter;

        }

    }
    public void toChartClassData(XYChart<Number, Number> chart) {
//      int sumOfY = 0;  int counter = 0; int instance = 1;
        chart.getData().clear();
        Set<String> labels = new HashSet<>(dataLabels.values());
        for (String label : labels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);
            dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataPoints.get(entry.getKey());
                series.getData().add(new XYChart.Data<>(point.getX(), point.getY()));
            });
            chart.getData().add(series);


        }
    }


    void clear() {
        dataPoints.clear();
        dataLabels.clear();
    }

    private void invalidDataDialog() {
        ErrorDialog dialog = ErrorDialog.getDialog();
        PropertyManager manager = new ApplicationTemplate().manager;
        String errTitle = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name());
        String errMsg = manager.getPropertyValue(AppPropertyTypes.INVALID_FORMAT.name());
        dialog.show(errTitle, errMsg);

    }

    private void duplicateDialog(int line) {
        ErrorDialog dialog = ErrorDialog.getDialog();
        PropertyManager manager = new ApplicationTemplate().manager;
        String errTitle = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name());
        String errMsg = manager.getPropertyValue(AppPropertyTypes.DUPLICATE.name());
        String errInput = manager.getPropertyValue(AppPropertyTypes.TEXT_AREA.name());
        dialog.show(errTitle, errMsg + " line number " + line);
    }

}
