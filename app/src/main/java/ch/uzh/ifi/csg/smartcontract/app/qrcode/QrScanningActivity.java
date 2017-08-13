package ch.uzh.ifi.csg.smartcontract.app.qrcode;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.TextView;
import java.util.HashMap;

import ch.uzh.ifi.csg.smartcontract.library.datamodel.ContractInfo;
import ch.uzh.ifi.csg.smartcontract.library.datamodel.UserProfile;
import ch.uzh.ifi.csg.smartcontract.library.service.serialization.GsonSerializationService;
import github.nisrulz.qreader.QRDataListener;
import github.nisrulz.qreader.QREader;
import ch.uzh.ifi.csg.smartcontract.app.R;

/**
 * Activity that uses the {@link QREader} class to read serialized {@link ContractInfo} and
 * {@link UserProfile} objects from QR-Codes.
 */
public class QrScanningActivity extends AppCompatActivity {

    public static final String ACTION_SCAN_CONTRACT = "ch.uzh.ifi.csg.smart_contract.contract.scan";
    public static final String ACTION_SCAN_PROFILE = "ch.uzh.ifi.csg.smart_contract.account.profile.scan";
    public static final String MESSAGE_CONTRACT_DATA = "ch.uzh.ifi.csg.smart_contract.data";
    public static final String MESSAGE_PROFILE_DATA = "ch.uzh.ifi.csg.smart_contract.account.profile.vcard";

    private boolean scanContract;

    // UI
    private TextView text;

    // QREader
    private SurfaceView surfaceView;
    private QREader qrEader;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanning);

        //determine if contracts or profile should be detected
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
        qrEader = new QREader.Builder(this, surfaceView, new QRDataListener() {
            @Override
            public void onDetected(final String data)
            {
                Log.d("QREader", "Value : " + data);
                if(scanContract)
                {
                    try{
                        //deserialize contract object
                        ContractInfo info = new GsonSerializationService().deserialize(data, ContractInfo.class);
                        info.setImages(new HashMap<String, String>());
                        info.setUserProfile(new UserProfile());
                        returnContractData(info);
                    }catch(Exception ex)
                    {
                    }

                }else
                {
                    try{
                        //deserialize profile
                        UserProfile profile = new GsonSerializationService().deserialize(data, UserProfile.class);
                        profile.setProfileImagePath(null);
                        returnProfile(profile);
                    }catch (Exception ex)
                    {
                    }
                }

                qrEader.initAndStart(surfaceView);
            }
        }).facing(QREader.BACK_CAM)
                .enableAutofocus(true)
                .height(surfaceView.getHeight())
                .width(surfaceView.getWidth())
                .build();

        qrEader.initAndStart(surfaceView);
    }

    /**
     * Serializes the detected profile and returns it as result of this Activity
     *
     * @param profile
     */
    private void returnProfile(UserProfile profile)
    {
        Intent result = new Intent();
        result.putExtra(MESSAGE_PROFILE_DATA, new GsonSerializationService().serialize(profile));
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    /**
     * Serializes the detected contract and returns it as result of this Activity
     *
     * @param contractInfo
     */
    private void returnContractData(ContractInfo contractInfo)
    {
        Intent result = new Intent();
        result.putExtra(MESSAGE_CONTRACT_DATA, new GsonSerializationService().serialize(contractInfo));
        setResult(Activity.RESULT_OK, result);
        finish();
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
