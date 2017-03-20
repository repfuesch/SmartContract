package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common;

/**
 * Created by flo on 16.03.17.
 */

public interface ContractErrorHandler
{
    void handleError(Throwable exception);
    void showMessage(String message);
}
