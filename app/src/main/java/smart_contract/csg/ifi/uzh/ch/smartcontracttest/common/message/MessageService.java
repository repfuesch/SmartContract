package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.message;

/**
 * Created by flo on 16.03.17.
 */

public interface MessageService
{
    void handleError(Throwable exception);
    void showErrorMessage(String message);
    void showMessage(String message);
    void showSnackBarMessage(String message, int length);
}
