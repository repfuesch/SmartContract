package smart_contract.csg.ifi.uzh.ch.smartcontracttest.qrcode;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.TextView;
import ch.uzh.ifi.csg.contract.common.Web3Util;
import ch.uzh.ifi.csg.contract.contract.ContractType;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import github.nisrulz.qreader.QREader;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;

public class QrScanningActivity extends AppCompatActivity {

    public static final String ACTION_SCAN_CONTRACT = "ch.uzh.ifi.csg.smart_contract.contract.scan";
    public static final String ACTION_SCAN_PROFILE = "ch.uzh.ifi.csg.smart_contract.account.profile.scan";
    public static final String MESSAGE_CONTRACT_ADDRESS = "ch.uzh.ifi.csg.smart_contract.address";
    public static final String MESSAGE_CONTRACT_TYPE = "ch.uzh.ifi.csg.smart_contract.type";
    public static final String MESSAGE_PROFILE_DATA = "ch.uzh.ifi.csg.smart_contract.account.profile.vcard";

    private boolean scanContract;

    // UI
    private TextView text;

    // QREader
    private SurfaceView surfaceView;
    private QReader qrEader;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanning);

        Intent intent = getIntent();
        if(intent.getAction().equals(ACTION_SCAN_CONTRACT))
        {
            scanContract = true;
        }else{
            scanContract = false;
        }

        text = (TextView) findViewById(R.id.code_info);

        // Setup SurfaceView
        // -----------------
        surfaceView = (SurfaceView) findViewById(R.id.camera_view);

        // Init QREader
        // ------------
        qrEader = new QReader.Builder(this, surfaceView, new QrDataListener() {
            @Override
            public void onDetected(final String data) {
                Log.d("QREader", "Value : " + data);
                text.post(new Runnable() {
                    @Override
                    public void run() {
                        text.setText(data);
                    }
                });

                if(scanContract)
                {
                    //validate contract data
                    String[] tokens = data.split(",");
                    if(tokens.length != 2)
                        return;

                    String address = tokens[0].trim();
                    String type = tokens[1].trim();
                    if(!validateContractAddress(address))
                        return;
                    if(!validateContractType(type))
                        return;

                    returnContractData(address, ContractType.valueOf(type));

                }else if (validateVCard(data))
                {
                    returnProfile(data);
                }else{
                    qrEader.initAndStart(surfaceView);
                }
            }
        }).facing(QREader.BACK_CAM)
                .enableAutofocus(true)
                .height(surfaceView.getHeight())
                .width(surfaceView.getWidth())
                .build();

        qrEader.initAndStart(surfaceView);
    }

    private void returnProfile(String data)
    {
        Intent result = new Intent();
        result.putExtra(MESSAGE_PROFILE_DATA, data);
        setResult(Activity.RESULT_OK, result);
        finish();
    }
    private void returnContractData(String address, ContractType type)
    {
        Intent result = new Intent();
        result.putExtra(MESSAGE_CONTRACT_ADDRESS, address);
        result.putExtra(MESSAGE_CONTRACT_TYPE, type);
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    private boolean validateContractAddress(String address)
    {
        return Web3Util.isAddress(address);
    }

    private boolean validateContractType(String value)
    {
        try{
            Enum.valueOf(ContractType.class, value);
        }catch(IllegalArgumentException ex)
        {
            return false;
        }
        return true;
    }

    private boolean validateVCard(String data)
    {
        VCard card = Ezvcard.parse(data).first();
        if(card != null)
            return true;

        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Init and Start with SurfaceView
        // -------------------------------
        qrEader.initAndStart(surfaceView);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Cleanup in onPause()
        // --------------------
        qrEader.releaseAndCleanup();
    }
}
