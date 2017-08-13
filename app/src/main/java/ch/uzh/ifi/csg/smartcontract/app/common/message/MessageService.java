package ch.uzh.ifi.csg.smartcontract.app.common.message;

import ch.uzh.ifi.csg.smartcontract.app.common.ActivityChangedListener;

/**
 * Interface to show error- and info messages to the user
 *
 */
public interface MessageService extends ActivityChangedListener
{
    /**
     * Displays the specified error message in a Dialog
     *
     * @param message
     */
    void showErrorMessage(String message);
    /**
     * Displays the specified info message in a Dialog
     *
     * @param message
     */
    void showMessage(String message);

    /**
     * Displays the specified message in a SnackBar for the specified time
     *
     * @param message
     * @param length
     */
    void showSnackBarMessage(String message, int length);
}
