package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common;

/**
 * Created by flo on 16.03.17.
 */

public interface MessageHandler
{
    public static final String ACTION_SHOW_ERROR = "ch.uzh.ifi.csg.smart_contract.action_error";
    public static final String MESSAGE_SHOW_ERROR = "ch.uzh.ifi.csg.smart_contract.message_error";

    void handleError(Throwable exception);
    void showMessage(String message);
    void showSnackBarMessage(String message, int length);
}
