package ch.uzh.ifi.csg.contract.util;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility class that provides methods to read and write files
 */
public class FileUtil
{
    /**
     * Writes the specified String to the specified File
     *
     * @param data
     * @param file
     * @return
     * @throws IOException
     */
    public static void writeFile(String data, File file) throws IOException {
        FileOutputStream outputStream = null;

        try{
            if(!file.exists())
                file.createNewFile();

            outputStream = new FileOutputStream(file);
            outputStream.write(data.getBytes());

        }finally
        {
            if(outputStream != null)
                outputStream.close();
        }
    }

    /**
     * Writes the specified InputStream to the specifed file
     *
     * @param in
     * @param file
     * @throws IOException
     */
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
        finally {
            if ( out != null )
                out.close();
            if(in != null)
                in.close();
        }
    }

    /**
     * Reads the content of a file as 'UTF8' String
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static String readFile(File file) throws IOException {
        FileInputStream inputStream = null;

        try{
            if(!file.exists())
                file.createNewFile();

            inputStream = new FileInputStream(file);
            int numBytes = inputStream.available();
            byte[] buffer = new byte[numBytes];
            inputStream.read(buffer, 0, numBytes);
            String data = new String(buffer, "UTF8");
            return data;
        }finally {
            if(inputStream != null)
                inputStream.close();
        }
    }

    /**
     * Creates a temporary file in the external directory of the storage that contains the string
     * 'part' and has the extension 'ext'.
     *
     * @param part
     * @param ext
     * @return
     * @throws IOException
     */
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

    /**
     * Copies the contents of the specified inputFile to the specified outputFile
     *
     * @param inputFile
     * @param outputFile
     * @throws IOException
     */
    public static void copyFile(File inputFile, File outputFile) throws IOException
    {
        InputStream is = new FileInputStream(inputFile);
        copyInputStreamToFile(is, outputFile);
    }
}
