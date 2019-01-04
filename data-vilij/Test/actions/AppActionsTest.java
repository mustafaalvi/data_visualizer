package actions;

import org.junit.Test;
import ui.AppUITester;
import ui.DataVisualizerTester;
import vilij.templates.ApplicationTemplate;

import static org.junit.Assert.*;

public class AppActionsTest{

    DataVisualizerTester dataVisualizer;
    AppUITester appUI;
    ApplicationTemplate applicationTemplate;
    AppActions actions;

    @Test
    public void initialize(){
        dataVisualizer = new DataVisualizerTester();
        applicationTemplate = dataVisualizer.applicationTemplate;
        appUI = new AppUITester(dataVisualizer.primaryStage, applicationTemplate);
        actions = new AppActions(new ApplicationTemplate());
    }

    @Test
    public void saveRequest() {
        DataVisualizerTester.launch(DataVisualizerTester.class);
        System.out.println("at the end");
        initialize();


        String testerString = "@Instance1\tlabel1\t1.5,2.2\n" +
                            "@Instance2\tlabel1\t1.8,3\n" +
                            "@Instance3\tlabel1\t2.1,2.9\n" +
                            "@Instance4\tlabel2\t10,9.4";
        //appUI.setTextAreaText(testerString);
        actions.handleSaveRequest();

    }
}