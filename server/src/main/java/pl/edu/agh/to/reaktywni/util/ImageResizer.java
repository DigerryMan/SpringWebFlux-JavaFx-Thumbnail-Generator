package pl.edu.agh.to.reaktywni.util;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;
import pl.edu.agh.to.reaktywni.image.Image;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import pl.edu.agh.to.reaktywni.thumbnail.Thumbnail;
import pl.edu.agh.to.reaktywni.thumbnail.ThumbnailSize;


@Component
public class ImageResizer implements Resizer {

    private static final Logger logger = Logger.getLogger(ImageResizer.class.getName());

    @Override
    public Thumbnail createThumbnail(Image image, ThumbnailSize size) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(image.getData());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnail thumbnail = new Thumbnail(image, size);

        try {
            BufferedImage originalImage = ImageIO.read(inputStream);
            BufferedImage bufferedThumbnail = Thumbnails.of(originalImage)
                    .size(size.getWidth(), size.getHeight())
                    .keepAspectRatio(true)
                    .asBufferedImage();
            ImageIO.write(bufferedThumbnail, image.getExtensionType(), outputStream);

        } catch (IOException | NullPointerException e) {
            logger.warning("Error while resizing image: " + e.getMessage());
            thumbnail.setFailure();
            return thumbnail;
        }
        thumbnail.setData(outputStream.toByteArray());
        return thumbnail;
    }
}
