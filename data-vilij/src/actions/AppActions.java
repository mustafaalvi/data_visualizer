package actions;

import dataprocessors.AppData;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;
import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.components.ActionComponent;
import vilij.components.ConfirmationDialog;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Path;
import static vilij.settings.PropertyTypes.SAVE_WORK_TITLE;
import static vilij.templates.UITemplate.SEPARATOR;

/**
 * This is the concrete implementation of the action handlers required by the application.
 *
 * @author Ritwik Banerjee
 */
public final class AppActions implements ActionComponent {

    /** The application to which this class of actions belongs. */
    private ApplicationTemplate applicationTemplate;

    /** Path to the data file currently active. */
    Path dataFilePath;
    Path loadFilePath;

    /** The boolean property marking whether or not there are any unsaved changes. */
     SimpleBooleanProperty isUnsaved;


    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
        this.isUnsaved = new SimpleBooleanProperty(false);
    }

    public void setIsUnsavedProperty(boolean property) { isUnsaved.set(property); }

    @Override
    public void handleNewRequest() {
        //call a method that will reset the LeftPanel (i.e. add a editable textArea and EditButton)
                applicationTemplate.getDataComponent().clear();
                applicationTemplate.getUIComponent().clear();
                isUnsaved.set(false);
                dataFilePath = null;
                loadFilePath = null;
    }

    @Override
    public void handleSaveRequest() {
        PropertyManager    manager = applicationTemplate.manager;
        ErrorDialog alreadySaved = ErrorDialog.getDialog();
        ErrorDialog saved = ErrorDialog.getDialog();

        try{

            if(isUnsaved.get() && dataFilePath == null) { //unsaved and path doesn't exist (so it's a new file)
                promptToSave();
                return;
            }
            if(isUnsaved.get() && (dataFilePath != null)) { //unsaved but loaded from a file
                save();
                errorHandlingHelper();
                return;
            }
            if((!isUnsaved.get()) && (dataFilePath != null)){ //Up to date
                alreadySaved.show(manager.getPropertyValue(AppPropertyTypes.NO_CHANGES_DETECTED_TITLE.name()),
                        manager.getPropertyValue(AppPropertyTypes.NO_CHANGES_DETECTED.name()));
            }



        }catch(IOException e) { errorHandlingHelper();}
    }

    @Override
    public void handleLoadRequest() {
        PropertyManager    manager = applicationTemplate.manager;
        FileChooser fileChooser = new FileChooser();
        String      dataDirPath = SEPARATOR + manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
        URL         dataDirURL  = getClass().getResource(dataDirPath);

        File directory = null;
        try {
            directory = new File(URLDecoder.decode(dataDirURL.getFile(), "UTF-8"));
        } catch (UnsupportedEncodingException e){
            System.out.println(e.toString());
            System.out.println();
            return;
        }
        fileChooser.setInitialDirectory(directory);
        fileChooser.setTitle(manager.getPropertyValue(SAVE_WORK_TITLE.name()));

        //String description = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name());
        //String extension   = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name());

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TSD files (*.tsd)", "*.tsd");
        // ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (.*%s)", description, extension),
        //         String.format("*.%s", extension));

        fileChooser.getExtensionFilters().add(extFilter);

        Window a = ((applicationTemplate.getUIComponent())).getPrimaryWindow();
        File selected = fileChooser.showOpenDialog(a);
        if (selected != null) {
            dataFilePath = selected.toPath();
            AppData appData = new AppData(applicationTemplate);
            applicationTemplate.getUIComponent().clear();
            appData.loadData(dataFilePath);
            ((AppUI)applicationTemplate.getUIComponent()).getTextArea().setVisible(true);
            ((AppUI)applicationTemplate.getUIComponent()).getToggleButton().setVisible(true);
            ((AppUI)applicationTemplate.getUIComponent()).getTextArea().setDisable(true);
        }
    }

    @Override
    public void handleExitRequest() {
        try {
            if (!isUnsaved.get() || promptToSave())
                System.exit(0);
        } catch (IOException e) { errorHandlingHelper(); }
    }

    @Override
    public void handlePrintRequest() { }

    public void handleScreenshotRequest() throws  IOException {
        promptToShot();
    }

    /**
     * This helper method verifies that the user really wants to save their unsaved work, which they might not want to
     * do. The user will be presented with three options:
     * <ol>
     * <li><code>yes</code>, indicating that the user wants to save the work and continue with the action,</li>
     * <li><code>no</code>, indicating that the user wants to continue with the action without saving the work, and</li>
     * <li><code>cancel</code>, to indicate that the user does not want to continue with the action, but also does not
     * want to save the work at this point.</li>
     * </ol>
     *
     * @return <code>false</code> if the user presses the <i>cancel</i>, and <code>true</code> otherwise.
     */

    private void promptToShot() throws IOException{

        AppUI x = (AppUI)(applicationTemplate.getUIComponent());
                try {
                    PropertyManager    manager = applicationTemplate.manager;
                    WritableImage image = x.getChart().snapshot(new SnapshotParameters(), null);

                    FileChooser fileChooser = new FileChooser();
                    String      dataDirPath =  SEPARATOR + manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
                    URL dataDirURL  = getClass().getResource(dataDirPath);

                    if (dataDirURL == null)
                        throw new FileNotFoundException(manager.getPropertyValue(AppPropertyTypes.RESOURCE_SUBDIR_NOT_FOUND.name()));

                    fileChooser.setInitialDirectory(new File(dataDirURL.getFile()));
                    fileChooser.setTitle(manager.getPropertyValue(SAVE_WORK_TITLE.name()));

                    File selected = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
                    File file = new File(selected.toString());
                    ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);

                } catch (IOException e) {
                    errorHandlingHelper();
                }

             }

    private boolean promptToSave() throws IOException {
        PropertyManager    manager = applicationTemplate.manager;
        ConfirmationDialog dialog  = ConfirmationDialog.getDialog();
        dialog.show(manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK_TITLE.name()),
                    manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK.name()));

        if (dialog.getSelectedOption() == null) return false; // if user closes dialog using the window's close button

        if (dialog.getSelectedOption().equals(ConfirmationDialog.Option.YES)) {
            if (dataFilePath == null) {
                FileChooser fileChooser = new FileChooser();
                String      dataDirPath =  SEPARATOR + manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
                URL         dataDirURL  = getClass().getResource(dataDirPath);

                if (dataDirURL == null)
                    throw new FileNotFoundException(manager.getPropertyValue(AppPropertyTypes.RESOURCE_SUBDIR_NOT_FOUND.name()));

                fileChooser.setInitialDirectory(new File(dataDirURL.getFile()));
                fileChooser.setTitle(manager.getPropertyValue(SAVE_WORK_TITLE.name()));

                String description = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name());
                String extension   = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name());
                ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (.*%s)", description, extension),
                                                                String.format("*.%s", extension));

                fileChooser.getExtensionFilters().add(extFilter);
                //^^ those four lines allow the user to look for TSD files explicitly
                File selected = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
                if (selected != null) {
                    dataFilePath = selected.toPath();
                    save();
                } else return false; // if user presses escape after initially selecting 'yes'
            } else
                save();
        }

        return !dialog.getSelectedOption().equals(ConfirmationDialog.Option.CANCEL);
    }

    private void save() throws IOException {
        //only gets called if the file exists aka if you HAVE saved before
        applicationTemplate.getDataComponent().saveData(dataFilePath);
        isUnsaved.set(false);
    }

    private void errorHandlingHelper() {
        ErrorDialog     dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        PropertyManager manager  = applicationTemplate.manager;
        String          errTitle = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name());
        String          errMsg   = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_MSG.name());
        dialog.show(errTitle, errMsg);
    }



}





