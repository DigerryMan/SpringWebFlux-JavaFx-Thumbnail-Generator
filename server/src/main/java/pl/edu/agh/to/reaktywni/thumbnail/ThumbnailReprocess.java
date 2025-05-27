package pl.edu.agh.to.reaktywni.thumbnail;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import pl.edu.agh.to.reaktywni.image.ImageService;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class ThumbnailReprocess {

    private final static Logger logger = Logger.getLogger(ThumbnailReprocess.class.getName());

    private final ImageService imageService;

    public ThumbnailReprocess(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostConstruct
    public void init() {
        logger.log(Level.INFO, "Looking for thumbnails to reprocess...");
        imageService.createEmptyThumbnailsIfMissing();
        imageService.reprocessPendingThumbnails();
    }
}
