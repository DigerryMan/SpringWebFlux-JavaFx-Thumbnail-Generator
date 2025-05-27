package pl.edu.agh.to.reaktywni.model;

import org.springframework.stereotype.Component;
import pl.edu.agh.to.reaktywni.ServerClient;
import pl.edu.agh.to.reaktywni.util.Base64ImageDataCodec;
import pl.edu.agh.to.reaktywni.util.ZipDataExtractor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


@Component
public class ImagePipeline {
    private static final Logger logger = Logger.getLogger(ImagePipeline.class.getName());

    private final ServerClient serverClient;

    public ImagePipeline(ServerClient serverClient) {
        this.serverClient = serverClient;
    }

    public Flux<Image> sendAndReceiveImages(List<Image> images, String thumbnailSize, String directoryPath) {
        logger.info("Sending images: " + images.size());

        images.forEach(image -> logger.info("Image dir path: " + image.getDirectoryPath()));

        Flux<Image> receivedImages = serverClient.sendImages(
                Flux.fromIterable(images).doOnNext(Base64ImageDataCodec::encode),
                thumbnailSize,
                directoryPath
        );

        return receivedImages.doOnNext(Base64ImageDataCodec::decode)
                .doOnNext(this::logImageInfo);
    }

    private void logImageInfo(Image image) {
        logger.info("Received: " + image);
    }

    public Flux<Image> getThumbnails(String thumbnailSize, String directoryPath, int page, int pageSize) {
        return serverClient.getThumbnails(thumbnailSize, directoryPath, page, pageSize)
                .doOnNext(Base64ImageDataCodec::decode);
    }

    public Flux<Image> getThumbnailsExcludingList(String thumbnailSize, String directoryPath, List<Integer> ids, int elemCount) {
        return serverClient.getThumbnailsExcludingSet(thumbnailSize, directoryPath, ids, elemCount)
                .doOnNext(Base64ImageDataCodec::decode);
    }

    public Mono<Long> getThumbnailsCount(String thumbnailSize, String directoryPath) {
        return serverClient.getThumbnailsCount(thumbnailSize, directoryPath);
    }

    public Mono<Image> getFullImage(int id) {
        return serverClient.getFullImage(id)
                .doOnNext(Base64ImageDataCodec::decode)
                .doOnError(e -> logger.log(Level.SEVERE, "getFullImageError: " + e.getMessage()));
    }

    public Mono<ZipDataExtractor.Directory> getDirectoryTree() {
        return serverClient.getDirectoryTree();
    }

    public void deleteDirectoryWithImages(String directoryPath) {
        serverClient.deleteDirectoryWithImages(directoryPath);
    }

    public void deleteImagesWithId(List<Integer> imageIds) {
        serverClient.deleteImagesWithId(imageIds);
    }

    public void postDirectoryTree(ZipDataExtractor.Directory directory) {
        serverClient.postDirectoryTree(directory);
    }

    public void moveSelectedImagesToDirectory(List<Integer> imageIds, String directoryPath) {
        serverClient.moveSelectedImagesToDirectory(imageIds, directoryPath);
    }
}
