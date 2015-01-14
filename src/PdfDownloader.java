import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

/**
 * Class to download PDFs
 */
public class PdfDownloader {

    // Downloads a PDF given a shop object
    public static void downloadPdf(Shop shop) throws IOException {
        System.out.println("Downloading "+shop.getLocation()+".pdf ...");
        System.out.println("opening connection");
        URL url = new URL(shop.getPdfUrl());
        InputStream in = url.openStream();
        FileOutputStream fos = new FileOutputStream(new File("C:\\Users\\user\\sassiePdfs\\"+shop.getLocation()+" 1412.pdf"));

        System.out.println("reading file...");
        int length = -1;
        byte[] buffer = new byte[1024];// buffer for portion of data from connection
        while ((length = in.read(buffer)) > -1) {
            fos.write(buffer, 0, length);
        }
        fos.close();
        in.close();
        System.out.println("file was downloaded");
    }

}
