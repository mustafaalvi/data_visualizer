package settings;

/**
 * This enumerable type lists the various application-specific property types listed in the initial set of properties to
 * be loaded from the workspace properties <code>xml</code> file specified by the initialization parameters.
 *
 * @author Ritwik Banerjee
 * @see vilij.settings.InitializationParams
 */
public enum AppPropertyTypes {

    /* resource files and folders */
    DATA_RESOURCE_PATH,

    /* user interface icon file names */
    SCREENSHOT_ICON,

    /* tooltips for user interface buttons */
    SCREENSHOT_TOOLTIP,

    /* error messages */
    RESOURCE_SUBDIR_NOT_FOUND,
    NO_CHANGES_DETECTED,

    /* application-specific message titles */
    SAVE_UNSAVED_WORK_TITLE,
    NO_CHANGES_DETECTED_TITLE,
    SAVED_TITLE,
    LOAD_TITLE,

    /* application-specific messages */
    SAVE_UNSAVED_WORK,
    LOAD_WORK,
    DUPLICATE,
    INVALID_FORMAT,


    /* application-specific parameters */
    SAVED,
    DATAVILIG_CSS_PATH,
    DATA_FILE_EXT,
    DATA_FILE_EXT_DESC,
    TEXT_AREA,
    SPECIFIED_FILE,
    LEFT_PANE_TITLE,
    LEFT_PANE_TITLEFONT,
    LEFT_PANE_TITLESIZE,
    CHART_TITLE,
    TOGGLE_BUTTON_TEXT,

    // Labels
    NUMBER_OF_INSTANCES,
    NUMBER_OF_LABELS,
    LABEL_NAMES,
    SOURCE_LABEL,


}
