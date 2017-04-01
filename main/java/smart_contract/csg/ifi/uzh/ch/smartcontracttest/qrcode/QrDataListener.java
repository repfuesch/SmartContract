package smart_contract.csg.ifi.uzh.ch.smartcontracttest.qrcode;

/**
 * The interface Qr data listener.
 */
public interface QrDataListener {

    /**
     * On detected.
     *
     * @param data
     *     the data
     */
    // Called from not main thread. Be careful
    void onDetected(final String data);
}
