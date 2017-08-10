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
import android.view.MotionEvent;
import android.view.View;

import net.glxn.qrgen.android.QRCode;

import java.io.File;
import java.util.List;

import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.controls.ProportionalImageView;

/**
 * A DialogFragment that can display images from different sources.
 */
public class ImageDialogFragment extends DialogFragment implements View.OnTouchListener
{
    public static final String MESSAGE_IMAGE_SOURCE = "ch.uzh.ifi.csg.smart_contract.image.src";
    public static final String MESSAGE_IMAGE_BMPS = "ch.uzh.ifi.csg.smart_contract.image.uris";
    public static final String MESSAGE_IMAGE_INDEX = "ch.uzh.ifi.csg.smart_contract.image.index";
    public static final String MESSAGE_DISPLAY_QRCODE = "ch.uzh.ifi.csg.smart_contract.image.qrcode";

    private static final int MIN_DISTANCE = 150;
    private float x1,x2;

    private ProportionalImageView imageView;
    private String imgSrc;
    private boolean displayQrCode;
    private List<Bitmap> bitmaps;
    private int imgIndex;

    public ImageDialogFragment()
    {
    }

    /**
     * Specifies the image source as string or a List of Bitmaps to display
     *
     * @param args
     */
    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);

        imgSrc = args.getString(MESSAGE_IMAGE_SOURCE);
        displayQrCode = args.getBoolean(MESSAGE_DISPLAY_QRCODE);
        bitmaps = (List<Bitmap>) args.getSerializable(MESSAGE_IMAGE_BMPS);
        imgIndex = args.getInt(MESSAGE_IMAGE_INDEX);
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

        //Set the image source based on the arguments supplied.
        if(displayQrCode)
        {
            Bitmap bm = QRCode.from(imgSrc).withSize(250, 250).bitmap();
            imageView.setImageBitmap(bm);
        }else if(imgSrc != null){
            imageView.setImageURI(Uri.fromFile(new File(imgSrc)));
        }else{
            imageView.setImageBitmap(bitmaps.get(imgIndex));
        }


        imageView.setOnTouchListener(this);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(contentView);

        // Create the AlertDialog object and return it
        final AlertDialog diag = builder.create();
        diag.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        return diag;
    }


    /**
     * Calculates the index of the current image to display based on touch events.
     *
     * @param view
     * @param motionEvent
     * @return
     */
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent)
    {
        switch(motionEvent.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                x1 = motionEvent.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = motionEvent.getX();
                float deltaX = x2 - x1;
                if (Math.abs(deltaX) > MIN_DISTANCE && x2 > x1)
                {
                    //select previous picture
                    imageView.setImageBitmap(bitmaps.get(Math.abs(imgIndex--) % (bitmaps.size())));

                }
                else if (Math.abs(deltaX) > MIN_DISTANCE && x1 > x2)
                {
                    //select next picture
                    imageView.setImageBitmap(bitmaps.get(Math.abs(imgIndex++) % (bitmaps.size())));
                }
                break;
        }
        return true;
    }
}
