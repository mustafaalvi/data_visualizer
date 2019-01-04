package ui;

import actions.AppActions;
import algorithms.DataSet;
import algorithms.RandomClusterer;
import dataprocessors.AppData;
import algorithms.RandomClassifier;
//import javafx.embed.swing.SwingFXUtils;
import dataprocessors.TSDProcessor;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
//import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
//import javafx.scene.image.WritableImage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import javafx.stage.Stage;
import settings.AppPropertyTypes;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;
import javafx.scene.control.ButtonType;


import javax.sound.sampled.Line;
import java.io.IOException;


import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Integer.parseInt;
import static vilij.settings.PropertyTypes.GUI_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.ICONS_RESOURCE_PATH;

/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
public final class AppUI extends UITemplate {

    /**
     * The application to which this class of actions belongs.
     */
    ApplicationTemplate applicationTemplate;

    @SuppressWarnings("FieldCanBeLocal")
    private Button scrnshotButton;                        // toolbar button to take a screenshot of the data
    private LineChart<Number, Number> chart;       // the chart where data will be displayed
    private CheckBox toggleButton;                        // workspace button to display data on the chart
    private TextArea textArea = new TextArea();           // text area for new data input
    private RandomClassifier randomClassifier;
    private RandomClusterer randomClusterer;
    private DataSet dataSet;

    private VBox leftPanel;
    private boolean isValid;                              // whether or not the text area has any new data since last display
    private boolean hasTwoNonNull;
    Label dataProperties = new Label();
    private RadioButton classification;
    private RadioButton clustering;

    private NumberAxis xAxis = new NumberAxis();
    private NumberAxis yAxis = new NumberAxis();

    private RadioButton class1;
    private RadioButton cluster1;
    private RadioButton cluster2;
    private RadioButton classConfig;
    private RadioButton clusterConfig;
    private RadioButton clusterConfig2;
    private Button run;
    private boolean isContinuous;
    private ToggleGroup algType;
    private AtomicBoolean isRunning = new AtomicBoolean();

    private Thread randomClassThread;
    private Thread randomClusterThread;

    public LineChart<Number, Number> getChart() {
        return chart;
    }

    public RandomClassifier getRandomClassifier() {
        return randomClassifier;
    }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        super.setToolBar(applicationTemplate);
        PropertyManager manager = applicationTemplate.manager;
        String iconsPath = SEPARATOR + String.join(SEPARATOR,
                manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        String scrnshoticonPath = String.join(SEPARATOR,
                iconsPath,
                manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_ICON.name()));
        scrnshotButton = setToolbarButton(scrnshoticonPath,
                manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_TOOLTIP.name()),
                true);
        toolBar.getItems().add(scrnshotButton);
    }

    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {

        newButton.setOnAction(e -> {
            appPane.getChildren().removeAll(workspace);
            layoutWholeAppPane();

        });

        saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        loadButton.setOnAction(e -> applicationTemplate.getActionComponent().handleLoadRequest());
        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
        printButton.setOnAction(e -> applicationTemplate.getActionComponent().handlePrintRequest());
        scrnshotButton.setOnAction(e -> {
            try {
                ((AppActions) (applicationTemplate.getActionComponent())).handleScreenshotRequest();
            } catch (IOException x) {
                ErrorDialog dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                PropertyManager manager = applicationTemplate.manager;
                String errTitle = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name());
                String errMsg = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_MSG.name());
                dialog.show(errTitle, errMsg);
            }
        });
    }

    @Override
    public void initialize() {
        layoutWholeAppPane();
        textArea.setVisible(false);
        toggleButton.setVisible(false);
    }

    @Override
    public void clear() {
        textArea.clear();
        chart.getData().clear();
    }

    public String getCurrentText() {
        return textArea.getText() + "\n";
    }

    private void layoutWholeAppPane() {
        PropertyManager manager = applicationTemplate.manager;

        workspace = new HBox();
        workspace.getChildren().addAll(layoutLeftTextArea(), layoutRight());
        HBox.setHgrow(workspace, Priority.ALWAYS);

        appPane.getChildren().add(workspace);
        VBox.setVgrow(appPane, Priority.ALWAYS);

        getPrimaryScene().getStylesheets().add(manager.getPropertyValue(AppPropertyTypes.DATAVILIG_CSS_PATH.name()));
        setWorkspaceActions();
    }

    private StackPane layoutRight() {
        PropertyManager manager = applicationTemplate.manager;
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(manager.getPropertyValue(AppPropertyTypes.CHART_TITLE.name()));


        StackPane rightPanel = new StackPane(chart);
        rightPanel.setMaxSize(windowWidth * 0.69, windowHeight * 0.69);
        rightPanel.setMinSize(windowWidth * 0.69, windowHeight * 0.69);
        StackPane.setAlignment(rightPanel, Pos.CENTER);

        return rightPanel;

    }

    public String layoutLeftLabels() {

        String dataProp = "";

        String data = textArea.getText();
        if (data != null) {
            String[] line = data.split("\n");
            dataProp += "Number of Instances: " + line.length + "\n";
            dataProp += "Contains Lables: " + "\n";

            List<String> labels = new ArrayList<String>();
            for (int i = 0; i < line.length; i++) {
                String[] point = line[i].split("\t");
                String ctnLabel = point[1];
                if (labels.size() == 0) {
                    labels.add(ctnLabel);
                    dataProp += ctnLabel + "\n";
                } else {
                    boolean contains = false;
                    for (int j = 0; j < labels.size(); j++) {
                        if (labels.get(j).equals(ctnLabel)) {
                            contains = true;
                        }
                    }
                    if (!contains) {
                        labels.add(ctnLabel);
                        dataProp += ctnLabel + "\n";
                    }
                }
            }
            dataProp += "Number of labels: " + labels.size();
            hasTwoNonNull = (labels.size() == 2);
        }
        return dataProp;

    }

    public VBox layoutLeftTextArea() {
        PropertyManager manager = applicationTemplate.manager;
        //if(new button was clicked)
        leftPanel = new VBox(8);
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setPadding(new Insets(10));
        VBox.setVgrow(leftPanel, Priority.ALWAYS);
        leftPanel.setMaxSize(windowWidth * 0.29, windowHeight * 0.7);
        leftPanel.setMinSize(windowWidth * 0.29, windowHeight * 0.7);


        Text leftPanelTitle = new Text(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLE.name()));
        String fontname = manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLEFONT.name());
        Double fontsize = Double.parseDouble(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLESIZE.name()));
        leftPanelTitle.setFont(Font.font(fontname, fontsize));

        textArea = new TextArea();
        // textArea.setEditable(false);

        HBox processButtonsBox = new HBox();

        toggleButton = new CheckBox(manager.getPropertyValue(AppPropertyTypes.TOGGLE_BUTTON_TEXT.name()));
        HBox.setHgrow(processButtonsBox, Priority.ALWAYS);
        processButtonsBox.getChildren().add(toggleButton);
        toggleButton.setSelected(true);

        leftPanel.getChildren().addAll(leftPanelTitle, textArea, processButtonsBox);


        algType = new ToggleGroup();
        classification = new RadioButton("Classification");
        classification.setUserData("CLASS");
        classification.setToggleGroup(algType);
        clustering = new RadioButton("Clustering");
        clustering.setUserData("CLUSTERING");
        clustering.setToggleGroup(algType);

        setUpRun(leftPanel);
        setToggleActions(leftPanel);

        return leftPanel;
    }

    public void setToggleActions(VBox leftPanel) {
        PropertyManager manager = applicationTemplate.manager;
        toggleButton.setOnAction(event -> {
            if (toggleButton.isSelected()) {
                try {
                    AppData data = new AppData(applicationTemplate);
                    data.loadData(textArea.getText());
                    isValid = data.getIsValid();
                } catch (Exception e) {
                    ErrorDialog dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    String errTitle = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name());
                    String errMsg = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_MSG.name());
                    String errInput = manager.getPropertyValue(AppPropertyTypes.TEXT_AREA.name());
                    String eMsg = e.getMessage();
                    dialog.show(errTitle, errMsg + errInput + eMsg);
                }

                textArea.setDisable(true);
                leftPanel.getChildren().remove(dataProperties);
                if (isValid) { //check if data deserves labels
                    dataProperties.setText(layoutLeftLabels());
                    leftPanel.getChildren().addAll(dataProperties);
                }

                //remove algorithm choice from previous runs
                if (leftPanel.getChildren().contains(class1))
                    leftPanel.getChildren().removeAll(class1, classConfig);
                if (leftPanel.getChildren().contains(cluster1))
                    leftPanel.getChildren().removeAll(cluster1, clusterConfig);


                //get rid of algo buttons, add them if data is valid and/or has 2 labels
                if ((leftPanel.getChildren().contains(classification) && leftPanel.getChildren().contains(clustering)))
                    leftPanel.getChildren().removeAll(classification, clustering);

                if (!(leftPanel.getChildren().contains(classification)) && leftPanel.getChildren().contains(clustering))
                    leftPanel.getChildren().remove(clustering);

                if (isValid && hasTwoNonNull)
                    leftPanel.getChildren().addAll(classification, clustering);
                else if (isValid)
                    leftPanel.getChildren().add(clustering);


            } else {
                textArea.setDisable(false);

            }
        });

        algType.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if (algType.getSelectedToggle() != null) {
                    if (leftPanel.getChildren().contains(class1))
                        leftPanel.getChildren().removeAll(class1, classConfig);
                    if (leftPanel.getChildren().contains(cluster1))
                        leftPanel.getChildren().removeAll(cluster1, clusterConfig);


                    String alg = algType.getSelectedToggle().getUserData().toString();
                    if (alg.equals("CLASS")) {
                        addClassAlgorithmButtons(leftPanel);
                    }

                    if (alg.equals("CLUSTERING")) {
                        addClusterAlgorithmButtons(leftPanel);
                    }

                }
            }
        });
    }

    public void addClassAlgorithmButtons(VBox leftPanel) {
        class1 = new RadioButton("Random Classifier");
        classConfig = new RadioButton("Configuration");
        classConfig.setDisable(true);
        leftPanel.getChildren().addAll(class1, classConfig);

        class1.setOnAction(event -> {
            classConfig.setDisable(false);
        });
        classConfig.setOnAction(event -> {
            if (classConfig.isSelected())
                makeClassConfigDialog();
        });
    }

    public void addClusterAlgorithmButtons(VBox leftPanel) {
        cluster1 = new RadioButton("Random Clustering");
        clusterConfig = new RadioButton("Configuration");
        clusterConfig.setDisable(true);
        leftPanel.getChildren().addAll(cluster1, clusterConfig);

        cluster1.setOnAction(event -> {
            clusterConfig.setDisable(false);
        });
        clusterConfig.setOnAction(event -> {
            if (clusterConfig.isSelected())
                makeClusterConfigDialog();
        });
//--------------------------------KMEANS BELOW--------------------------------------
        cluster2 = new RadioButton("KMeansClustering");
        clusterConfig2 = new RadioButton("Configuration");
        clusterConfig2.setDisable(true);
        leftPanel.getChildren().addAll(cluster2, clusterConfig2);

        cluster2.setOnAction(event -> {
            clusterConfig2.setDisable(false);
        });
        clusterConfig2.setOnAction(event -> {
            if (clusterConfig2.isSelected())
                makeClusterConfigDialog();
        });

        ToggleGroup clusterAlgos = new ToggleGroup();
        cluster1.setToggleGroup(clusterAlgos);
        cluster2.setToggleGroup(clusterAlgos);
    }

    public void makeClassConfigDialog() {
        GridPane grid = new GridPane();
        TextField maxIters = new TextField("0");
        TextField updateInt = new TextField("0");
        Button continuous = new Button("Press for Continuous");
        Button done = new Button("Done");
        ButtonType close = ButtonType.CLOSE;

        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(1, 1, 1, 1));


        grid.add(new Label("Max Iterations:"), 0, 0);
        grid.add(maxIters, 1, 0);
        grid.add(new Label("Update Interval:"), 0, 1);
        grid.add(updateInt, 1, 1);
        grid.add(continuous, 1, 3);
        grid.add(done, 4, 4);

        continuous.setOnAction(event -> {
            if(continuous.getText().equals("Running Non-Continuously")) {
                continuous.setText("Running Continuously");
                isContinuous = true;
            } else
                continuous.setText("Running Non-Continuously");
                isContinuous = false;
        });

        javafx.scene.control.Dialog x = new javafx.scene.control.Dialog();
        x.setTitle("Class Configuration");
        x.setGraphic(grid);
        x.getDialogPane().getButtonTypes().add(close);

        x.show();

        done.setOnAction(event -> {
            Map labels = ((AppData) (applicationTemplate.getDataComponent())).getProcessor().getDataLabels();
            Map points = ((AppData) (applicationTemplate.getDataComponent())).getProcessor().getDataPoints();

            int maxIterations = Integer.parseInt(maxIters.getCharacters().toString());
            int updateInterval = Integer.parseInt(updateInt.getCharacters().toString());

            // SHOULD BE DEGRADING GRACEFULLY HERE

            dataSet = new DataSet((HashMap) labels, (HashMap) points);
            randomClassifier = new RandomClassifier(dataSet, maxIterations, updateInterval, true);
            //System.out.print(randomclass.isAlive());
            run.setVisible(true);
            run.setDisable(false);
        });

    }

    public void makeClusterConfigDialog() {
        GridPane grid = new GridPane();
        TextField maxIters = new TextField();
        TextField updateInt = new TextField();
        TextField numClusters = new TextField();
        Button continuous = new Button("Press for Continuous");
        Button done = new Button("Done");
        ButtonType close = ButtonType.CLOSE;


        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(0, 5, 0, 5));


        grid.add(new Label("Max Iterations:"), 0, 0);
        grid.add(maxIters, 1, 0);
        grid.add(new Label("Update Interval:"), 0, 1);
        grid.add(updateInt, 1, 1);
        grid.add(new Label("Number of Clusters(Labels):"), 0, 2);
        grid.add(numClusters, 1, 2);


        grid.add(continuous, 1, 3);
        grid.add(done, 5, 5);

        continuous.setOnAction(event -> {
            if(continuous.getText().equals("Running Non-Continuously")) {
                continuous.setText("Running Continuously");
                isContinuous = true;
            } else
                continuous.setText("Running Non-Continuously");
            isContinuous = false;
        });

        javafx.scene.control.Dialog x = new javafx.scene.control.Dialog();
        x.setTitle("Cluster Configuration");
        x.setGraphic(grid);
        x.getDialogPane().getButtonTypes().add(close);

        x.show();

        done.setOnAction(event -> {
            Map labels = ((AppData) (applicationTemplate.getDataComponent())).getProcessor().getDataLabels();
            Map points = ((AppData) (applicationTemplate.getDataComponent())).getProcessor().getDataPoints();

            int maxIterations = Integer.parseInt(maxIters.getCharacters().toString());
            int updateInterval = Integer.parseInt(updateInt.getCharacters().toString());
            int numLabels = Integer.parseInt(numClusters.getCharacters().toString());


            // SHOULD BE DEGRADING GRACEFULLY HERE

            dataSet = new DataSet((HashMap) labels, (HashMap) points);
           // randomClusterer = new RandomClusterer(); // INSTANTIATE A RANDOM CLUSTER OBJECT HERE!!
            run.setDisable(false);
        });

    }

    public void setUpRun(VBox leftPanel) {
        run = new Button("Run");
        run.setVisible(false);
        leftPanel.getChildren().add(3, run);
        run.setDisable(true);
        run.setOnAction(event -> {
            try {
                isRunning.set(true);
                if (algType.getSelectedToggle().getUserData().toString().equals("CLASS")) {
                    for (int i = 1; i <= getRandomClassifier().getMaxIterations() && getRandomClassifier().tocontinue(); i++) {
                        ((AppData) (applicationTemplate.getDataComponent())).loadData(getCurrentText());
                        adjustAxis();
                        ((AppData) (applicationTemplate.getDataComponent())).displayData();
                        randomClassThread = new Thread(randomClassifier);
                        randomClassThread.start();
                        randomClassThread.join();
                        iterateSeriesToLineChart();

                    }

                } else if (algType.getSelectedToggle().getUserData().toString().equals("CLUSTERING")) {
                    ((AppData) (applicationTemplate.getDataComponent())).loadData(getCurrentText());
                    adjustAxis();
                    ((AppData) (applicationTemplate.getDataComponent())).displayData();
                    randomClusterThread = new Thread(randomClusterer);
                    randomClusterThread.start();
                    randomClusterThread.join();
                }
                isRunning.set(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            scrnshotButton.setDisable(isRunning.get());
        });
    }

    public void iterateSeriesToLineChart() {
        //classLines.getNode().setStyle("-fx-stroke: purple;");
        TSDProcessor aProcessor = ((AppData) (applicationTemplate.getDataComponent())).getProcessor();

        double xAvg = aProcessor.getxAvg();
        double yAvg = aProcessor.getyAvg();

        double xMax = aProcessor.getMaxX();
        double yMax = aProcessor.getMaxY();
        double xMin = aProcessor.getMinX();
        double yMin = aProcessor.getMinY();

        double updatedXINT = randomClassifier.getxIntercepts().get(0).intValue() + xAvg + Math.random();
        double updatedYINT = randomClassifier.getyIntercepts().get(0).intValue() + yAvg + Math.random();

        LineChart.Series classLines = new LineChart.Series<>();
        classLines.setName("Classification Lines");

        classLines.getData().add(new LineChart.Data<>(updatedXINT , yMin + Math.random()));
        classLines.getData().add(new LineChart.Data<>(xMin + Math.random(), updatedYINT ));

        chart.getData().add(classLines);
        /*classLines.getData().add(xcept);
        classLines.getData().add(ycept);
        classLineSeries.add(classLines);*/

        /*try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

            /*try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }

    public void adjustAxis() {
        TSDProcessor aProcessor = ((AppData) (applicationTemplate.getDataComponent())).getProcessor();
        xAxis.setAutoRanging(false);
        yAxis.setAutoRanging(false);
        xAxis.setLowerBound(aProcessor.findMinX(aProcessor.getxValues()) - 1);
        xAxis.setUpperBound(aProcessor.findMaxX(aProcessor.getxValues()) + 1);
        yAxis.setLowerBound(aProcessor.findMinY(aProcessor.getyValues()) - 1);
        yAxis.setUpperBound(aProcessor.findMaxX(aProcessor.getyValues()) + 1);

       /* xAxis.setAutoRanging(true);
        yAxis.setAutoRanging(true);
        xAxis.setForceZeroInRange(false);
        yAxis.setForceZeroInRange(false);*/


    }

    public void setTextAreaText(String text) {
        this.textArea.clear();
        this.textArea.appendText(text);
    }

    private void setWorkspaceActions() {
        setTextAreaActions();
        //setToggleButtonActions();
    }

    private void setTextAreaActions() {
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.equals(oldValue)) {
                    if (!newValue.isEmpty()) { //you changed the textArea and its not
                        ((AppActions) applicationTemplate.getActionComponent()).setIsUnsavedProperty(true);
                        newButton.setDisable(false);
                        saveButton.setDisable(false);
                    } else { //you changed the textArea to empty
                        newButton.setDisable(true);
                        saveButton.setDisable(true);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                System.err.println(newValue);
            }
        });
        textArea.setPrefRowCount(10);
        textArea.scrollTopProperty().addListener(((observable, oldValue, newValue) -> {
            textArea.setScrollTop(10);
        }));
    }

    public void setDataProperties(String str) {
        dataProperties.setText(str);
    }

    public TextArea getTextArea() {
        return textArea;
    }

    public CheckBox getToggleButton() {
        return toggleButton;
    }
/*
    private void setToggleButtonActions() {
        toggleButton.setOnAction(event -> {
            try {
                if (textArea.editableProperty().get()) {
                    textArea.setEditable(false);
                    AppData x = (AppData) (applicationTemplate.getDataComponent());
                    x.loadData(getCurrentText());
                    layoutWholeAppPane();
                } else
                    textArea.setEditable(true);
            } catch (Exception e) {
                return;
            }
        });
    }
    */
}
