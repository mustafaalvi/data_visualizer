package algorithms;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * This class specifies how an algorithm will expect the dataset to be. It is
 * provided as a rudimentary structure only, and does not include many of the
 * sanity checks and other requirements of the use cases. As such, you can
 * completely write your own class to represent a set of data instances as long
 * as the algorithm can read from and write into two {@link java.util.Map}
 * objects representing the name-to-label map and the name-to-location (i.e.,
 * the x,y values) map. These two are the {@link DataSet#labels} and
 * {@link DataSet#locations} maps in this class.
 *
 * @author Ritwik Banerjee
 */
public class DataSet {

    public void toChartData(LineChart<Number, Number> chart, Scene primaryScene) {
    }


    public static class InvalidDataNameException extends Exception {

        private static final String NAME_ERROR_MSG = "All data instance names must start with the @ character.";

        public InvalidDataNameException(String name) {
            super(String.format("Invalid name '%s'." + NAME_ERROR_MSG, name));
        }
    }

    private static String nameFormatCheck(String name) throws InvalidDataNameException {
        if (!name.startsWith("@"))
            throw new InvalidDataNameException(name);
        return name;
    }

    private static Point2D locationOf(String locationString) {
        String[] coordinateStrings = locationString.trim().split(",");
        return new Point2D(Double.parseDouble(coordinateStrings[0]), Double.parseDouble(coordinateStrings[1]));
    }

    private Map<String, String>  labels;
    private Map<String, Point2D> locations;

    /** Creates an empty dataset. */
    public DataSet() {
        labels = new HashMap<>();
        locations = new HashMap<>();
    }

    public DataSet(HashMap labels, HashMap locations) {
        this.labels = labels;
        this.locations = locations;
    }

    public Map<String, String> getLabels()     { return labels; }

    public Map<String, Point2D> getLocations() { return locations; }



    private void addInstance(String tsdLine) throws InvalidDataNameException {
        String[] arr = tsdLine.split("\t");
        labels.put(nameFormatCheck(arr[0]), arr[1]);
        locations.put(arr[0], locationOf(arr[2]));
    }

    public static DataSet fromTSDFile(Path tsdFilePath) throws IOException {
        DataSet dataset = new DataSet();
        Files.lines(tsdFilePath).forEach(line -> {
            try {
                dataset.addInstance(line);
            } catch (InvalidDataNameException e) {
                e.printStackTrace();
            }
        });
        return dataset;
    }
}