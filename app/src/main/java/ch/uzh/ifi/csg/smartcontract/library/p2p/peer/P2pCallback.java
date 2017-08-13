package ch.uzh.ifi.csg.smartcontract.library.p2p.peer;


import ch.uzh.ifi.csg.smartcontract.app.p2p.dialog.P2pExportDialog;
import ch.uzh.ifi.csg.smartcontract.app.p2p.dialog.P2pImportDialog;
import ch.uzh.ifi.csg.smartcontract.app.p2p.service.P2PService;

/**
 * General callback interface implemented by UI components that can receive or transmit data
 * over a wireless P2P channel.
 *
 * See {@link P2pExportDialog}
 * See {@link P2pImportDialog}
 * See {@link Peer}
 */
public interface P2pCallback {
    /**
     * Callback method invoked by the {@link Peer} implementation when the transmission state
     * changes or by a {@link P2PService} implementation when the connection state changes.
     *
     * @param message
     */
    void onP2pInfoMessage(String message);
    /**
     * Callback method invoked by the {@link Peer} or {@link P2PService} impleemntations
     * when an error occured during the transmission or connection setup.
     *
     * @param message
     */
    void onP2pErrorMessage(String message);

    /**
     * Callback method invoked by a {@link Peer} implementation when the data transmission
     * is complete.
     */
    void onTransmissionComplete();
}
