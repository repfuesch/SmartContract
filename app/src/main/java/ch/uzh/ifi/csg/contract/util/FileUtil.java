package ch.uzh.ifi.csg.contract.util;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by flo on 16.03.17.
 */

public class FileUtil
{
    public static boolean writeFile(String data, File file) throws IOException {
        FileOutputStream outputStream;

        if(!file.exists())
            file.createNewFile();

        outputStream = new FileOutputStream(file);
        outputStream.write(data.getBytes());
        outputStream.close();

        return true;
    }

    // Copy an InputStream to a File.
    public static void copyInputStreamToFile(InputStream in, File file) throws IOException
    {
        if(!file.exists())
            file.createNewFile();

        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
        }
        catch(IOException ex)
        {
            throw ex;
        }
        finally {
            // Ensure that the InputStreams are closed even if there's an exception.
            if ( out != null ) {
                out.close();
            }

            // If you want to close the "in" InputStream yourself then remove this
            // from here but ensure that you close it yourself eventually.
            in.close();
        }
    }

    public static String readFile(File file) throws IOException {
        FileInputStream inputStream;

        if(!file.exists())
            file.createNewFile();

        inputStream = new FileInputStream(file);
        int numBytes = inputStream.available();
        byte[] buffer = new byte[numBytes];
        inputStream.read(buffer, 0, numBytes);
        String data = new String(buffer, "UTF8");
        inputStream.close();

        return data;
    }

    public static File createTemporaryFile(String part, String ext) throws IOException
    {
        File tempDir= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        tempDir=new File(tempDir.getAbsolutePath());
        if(!tempDir.exists())
        {
            tempDir.mkdirs();
        }

        return File.createTempFile(part, ext, tempDir);
    }

    public static void copyFile(File inputFile, File outputFile) throws IOException
    {
        InputStream is = new FileInputStream(inputFile);
        copyInputStreamToFile(is, outputFile);
    }
}
