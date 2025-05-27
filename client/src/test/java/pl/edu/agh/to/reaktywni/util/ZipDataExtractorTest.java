package pl.edu.agh.to.reaktywni.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class ZipDataExtractorTest {

    File zipFile = new File("C:/Users/<username>/Desktop/photos.zip");

    @Test
    public void testExtractZipData() {
        try {
            ZipDataExtractor.ZipData data = ZipDataExtractor.extractZipData(zipFile);
            System.out.println(data.directory());
            System.out.println(data.images());

        } catch (IOException e) {
            e.printStackTrace();
            assert false;
        }
    }
}
