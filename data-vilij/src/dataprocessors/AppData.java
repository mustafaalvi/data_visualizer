package dataprocessors;

import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * This is the concrete application-specific implementation of the data component defined by the Vilij framework.
 *
 * @author Ritwik Banerjee
 * @see DataComponent
 */
public class AppData implements DataComponent {

    private TSDProcessor        processor;
    private ApplicationTemplate applicationTemplate;

    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
    }

    public TSDProcessor getProcessor() {
        return processor;
    }

    public boolean getIsValid(){
        return processor.getIsValid();
    }

    @Override
    public void loadData(Path dataFilePath) {

        File file = dataFilePath.toFile();
        try {
            Scanner input = new Scanner(file);
            String str = input.useDelimiter("\\A").next();
            String[] what = str.split("\n");
            if(what.length >= 10){
                ErrorDialog temp = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                temp.show( "Data overflow", "The loaded file has "+what.length+" lines. The first 10 is loaded");
            }
            processor.processString(str);
            ((AppUI)(applicationTemplate.getUIComponent())).setTextAreaText(str);
           // String dataProperties = "The source path "+dataFilePath.toString();
            if(processor.getIsValid())
            ((AppUI)(applicationTemplate.getUIComponent())).setDataProperties(((AppUI)(applicationTemplate.getUIComponent())).layoutLeftLabels());
            ((AppUI)(applicationTemplate.getUIComponent())).getTextArea().setDisable(true);
            input.close();
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(Exception f){
            ErrorDialog dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            PropertyManager manager  = applicationTemplate.manager;
            String          errTitle = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name());
            String          errMsg   = manager.getPropertyValue(AppPropertyTypes.INVALID_FORMAT.name());
            String          errInput = manager.getPropertyValue(AppPropertyTypes.TEXT_AREA.name());
            String          eMsg     = f.getMessage();
            dialog.show(errTitle, errMsg + errInput+eMsg);
        }
    }

    public void loadData(String dataString) throws Exception {
            processor.processString(dataString);
    }

    @Override
    public void saveData(Path dataFilePath) {
        // NOTE: completing this method was not a part of HW 1. You may have implemented file saving from the
        // confirmation dialog elsewhere in a different way.
        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(dataFilePath))) {
            writer.write(((AppUI) applicationTemplate.getUIComponent()).getCurrentText());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void clear() {
        processor.clear();
    }

    public void displayData() { processor.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart()); }

}
