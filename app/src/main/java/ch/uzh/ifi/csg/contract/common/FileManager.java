package ch.uzh.ifi.csg.contract.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by flo on 16.03.17.
 */

public class FileManager
{
    public static void writeFile(String data, File file)
    {
        FileOutputStream outputStream;

        try {

            if(!file.exists())
                file.createNewFile();

            outputStream = new FileOutputStream(file);
            outputStream.write(data.getBytes());
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String readFile(File file)
    {
        FileInputStream inputStream;

        try {
            if(!file.exists())
                file.createNewFile();

            inputStream = new FileInputStream(file);
            int numBytes = inputStream.available();
            byte[] buffer = new byte[numBytes];
            inputStream.read(buffer, 0, numBytes);
            String data = new String(buffer, "UTF8");
            inputStream.close();

            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
