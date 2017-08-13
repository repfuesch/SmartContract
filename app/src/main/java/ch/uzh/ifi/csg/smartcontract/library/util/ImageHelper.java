package ch.uzh.ifi.csg.smartcontract.library.util;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class provides helper methods for working with image Uri's and files
 */
public final class ImageHelper {

    public static final int PICK_FILE_REQUEST_CODE = 54654;
    public static final int IMAGE_CAPTURE_REQUEST_CODE = 74984;

    /***
     * Stores the bitmap in a new file in the the given iamge directory
     *
     * @param bitmap the bitmap to save
     * @param imageDir the image directory in which the file should be stored
     * @return the new image file
     */
    public static File saveBitmap(Bitmap bitmap, String imageDir)
    {
        OutputStream outStream = null;
        File file = null;
        try {
            file = createImageFile(imageDir);

            outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (IOException e) {
            Log.e("file", "Cannot save bitmap image in file", e);
        }

        return file;
    }

    /***
     * Copies an image file form filepath to a new image file in the imageDir directory.
     *
     * @param filepath the path to the old image file
     * @param imageDir the path in which the new file should be created
     * @return the new image file
     */
    public static File saveImageFile(String filepath, String imageDir)
    {
        File file = null;
        try {
            file = createImageFile(imageDir);
            FileUtil.copyFile(new File(filepath), file);

        } catch (IOException e) {
            Log.e("file", "Cannot save image file", e);
        }

        return file;
    }

    /***
     * Creates an image file with a unique name in the provided directory
     *
     * @param dir the directory in which the file should be created
     * @return The image file
     * @throws IOException
     */
    public static File createImageFile(String dir) throws IOException
    {
        File storageDir = new File(dir);
        if(!storageDir.exists())
            storageDir.mkdirs();

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    private static boolean isLocalFileUri(Uri uri)
    {
        return uri.getScheme().equalsIgnoreCase("content") || uri.getScheme().equalsIgnoreCase("file");
    }

    /**
     * This method returns the local image path for a Uri pointing to a local image
     *
     * @param context
     * @param uri Uri to be resolved
     * @return A String representig the absolute path to the file
     */
    private static String getImagePath(Context context, Uri uri){
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":")+1);
        cursor.close();

        cursor = context.getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    /**
     * This method returns the orientation in degrees for a photo Uri
     *
     * @param context
     * @param photoUri Uri to be resolved
     * @return Int representing the rotation in degrees
     */
    private static int getUriOrientation(Context context, Uri photoUri) {
    /* it's on the external media. */
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    /**
     * This method returns the orientation in degrees for a local file path
     *
     * @param filePath Path to file
     * @return Int representing the rotation in degrees
     */
    private static int getFileOrientation(String filePath) throws Exception
    {
        ExifInterface exif = new ExifInterface(filePath);
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) :  ExifInterface.ORIENTATION_NORMAL;

        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;

        return rotationAngle;
    }

    /**
     * This method returns a correctly oriented Bitmap of an image represented by the photo Uri. The uri can
     * point to a local or to a remote file
     *
     * @param context Android context
     * @param photoUri Uri pointing to the image
     * @param maxImageDimension maximum dimension the image should be scaled to
     * @return a Bitmap of the image that has the correct orientation and dimension
     */
    public static Bitmap getCorrectlyOrientedImage(Context context, Uri photoUri, int maxImageDimension) throws Exception {
        InputStream is = context.getContentResolver().openInputStream(photoUri);
        BitmapFactory.Options dbo = new BitmapFactory.Options();
        dbo.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, dbo);
        is.close();

        int rotatedWidth, rotatedHeight, orientation;
        if(isLocalFileUri(photoUri))
        {
            orientation = getFileOrientation(getImagePath(context, photoUri));
        }else{
            orientation = getUriOrientation(context, photoUri);
        }

        if (orientation == 90 || orientation == 270) {
            rotatedWidth = dbo.outHeight;
            rotatedHeight = dbo.outWidth;
        } else {
            rotatedWidth = dbo.outWidth;
            rotatedHeight = dbo.outHeight;
        }

        Bitmap srcBitmap;
        is = context.getContentResolver().openInputStream(photoUri);
        if (rotatedWidth > maxImageDimension || rotatedHeight > maxImageDimension) {
            float widthRatio = ((float) rotatedWidth) / ((float) maxImageDimension);
            float heightRatio = ((float) rotatedHeight) / ((float) maxImageDimension);
            float maxRatio = Math.max(widthRatio, heightRatio);

            // Create the bitmap from file
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = (int) maxRatio;
            srcBitmap = BitmapFactory.decodeStream(is, null, options);
        } else {
            srcBitmap = BitmapFactory.decodeStream(is);
        }
        is.close();

    /*
     * if the orientation is not 0 (or -1, which means we don't know), we
     * have to do a rotation.
     */
        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),
                    srcBitmap.getHeight(), matrix, true);
        }

        return srcBitmap;
    }

    /**
     * This method returns the SHA-256 hash of a bitmap
     *
     * @param image Bitmap of an image
     * @return String representing the hexadecimal value of the calculated hash
     */
    public static String getImageHash(Bitmap image)
    {
        byte[] bytes = getBytes(image);
        MessageDigest digest=null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e1) {
            Log.d("file", "SHA-256 digest not found", e1);
        }

        digest.reset();
        String hex =  BinaryUtil.bin2hex(digest.digest(bytes));
        return hex;
    }

    private static byte[] getBytes(Bitmap image)
    {
        int size = image.getRowBytes() * image.getHeight();
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        image.copyPixelsToBuffer(byteBuffer);
        return byteBuffer.array();
    }

    /***
     * Makes an intent for making a picture to the default photo app on the device
     *
     * @param fragment fragment that should resolve the intent
     */
    public static void makePicture(Fragment fragment)
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(fragment.getActivity().getPackageManager()) != null) {
            fragment.startActivityForResult(takePictureIntent, IMAGE_CAPTURE_REQUEST_CODE);
        }
    }

    /***
     * Makes an intent for opening an image file on the device using the media browser
     *
     * @param fragment fragment that should resolve the intent
     */
    public static void openImageFile(Fragment fragment) {

        openFile(fragment, "image/*", "*image/*");
    }

    /***
     * Makes an intent for opening a file on the device using the file browser
     *
     * @param fragment fragment that should resolve the intent
     */
    public static void openFile(Fragment fragment)
    {
        openFile(fragment, "*", "*/*");
    }

    private static void openFile(Fragment fragment, String contentType, String samsungContentType)
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(contentType);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // special intent for Samsung file manager
        Intent sIntent;
        if(samsungContentType.equals("*/*"))
        {
            sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
        }else{
            sIntent = new Intent("com.sec.android.app.myfiles.PICK_FILE");
        }
        intent.putExtra("CONTENT_TYPE", samsungContentType);
        intent.addCategory(Intent.CATEGORY_DEFAULT);

        Intent chooserIntent;
        if (fragment.getActivity().getPackageManager().resolveActivity(sIntent, 0) != null){
            // it is device with samsung file manager
            chooserIntent = Intent.createChooser(sIntent, "Select File");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { intent});
        }
        else {
            chooserIntent = Intent.createChooser(intent, "Select File");
        }
        try {
            fragment.startActivityForResult(chooserIntent, PICK_FILE_REQUEST_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(fragment.getActivity().getApplicationContext(), "No suitable File Manager was found.", Toast.LENGTH_SHORT).show();
        }
    }
}


