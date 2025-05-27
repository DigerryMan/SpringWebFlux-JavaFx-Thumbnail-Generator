package pl.edu.agh.to.reaktywni.util;

import pl.edu.agh.to.reaktywni.model.Image;
import java.util.Base64;


public interface Base64ImageDataCodec {

    static void encode(Image image) {
        String encodedData = Base64.getEncoder().encodeToString(image.getData());
        image.setData(encodedData.getBytes());
    }

    static void decode(Image image) {
        image.setData(Base64.getDecoder().decode(image.getData()));
    }
}
