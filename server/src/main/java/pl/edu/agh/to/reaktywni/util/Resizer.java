package pl.edu.agh.to.reaktywni.util;

import pl.edu.agh.to.reaktywni.image.Image;
import pl.edu.agh.to.reaktywni.thumbnail.Thumbnail;
import pl.edu.agh.to.reaktywni.thumbnail.ThumbnailSize;


@FunctionalInterface
public interface Resizer {

    Thumbnail createThumbnail(Image image, ThumbnailSize size);
}
