package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import net.glxn.qrgen.android.QRCode;

import java.io.File;

import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.controls.ProportionalImageView;

/**
 * Created by flo on 31.03.17.
 */

public class ImageDialogFragment extends DialogFragment
{
    public static final String MESSAGE_IMAGE_SOURCE = "ch.uzh.ifi.csg.smart_contract.image.src";
    public static final String MESSAGE_DISPLAY_QRCODE = "ch.uzh.ifi.csg.smart_contract.image.qrcode";

    private ProportionalImageView imageView;
    private String imgSrc;
    private boolean displayQrCode;

    public ImageDialogFragment()
    {
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        imgSrc = args.getString(MESSAGE_IMAGE_SOURCE);
        displayQrCode = args.getBoolean(MESSAGE_DISPLAY_QRCODE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View contentView = inflater.inflate(R.layout.fragment_image_dialog, null);
        contentView.setBackgroundColor(Color.TRANSPARENT);
        imageView = (ProportionalImageView) contentView.findViewById(R.id.image_view);

        if(displayQrCode)
        {
            Bitmap bitmap = QRCode.from(imgSrc).bitmap();
            imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 250, 250, false));
        }else{
            imageView.setImageURI(Uri.fromFile(new File(imgSrc)));
        }

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(contentView);

        // Create the AlertDialog object and return it
        final AlertDialog diag = builder.create();
        diag.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        return diag;
    }

}
