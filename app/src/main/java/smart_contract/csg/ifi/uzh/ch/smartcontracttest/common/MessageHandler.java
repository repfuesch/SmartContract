package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common;

/**
 * Created by flo on 16.03.17.
 */

public interface MessageHandler
{
    String ACTION_SHOW_ERROR = "ch.uzh.ifi.csg.smart_contract.action_error";
    String MESSAGE_SHOW_ERROR = "ch.uzh.ifi.csg.smart_contract.message_error";
    String ACTION_SHOW_INFO = "ch.uzh.ifi.csg.smart_contract.action_info";
    String MESSAGE_SHOW_INFO = "ch.uzh.ifi.csg.smart_contract.message_info";

    void handleError(Throwable exception);
    void showErrorMessage(String message);
    void showMessage(String message);
    void showSnackBarMessage(String message, int length);
}
