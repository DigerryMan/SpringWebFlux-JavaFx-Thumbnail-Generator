package pl.edu.agh.to.reaktywni.image;


import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.edu.agh.to.reaktywni.thumbnail.Thumbnail;
import pl.edu.agh.to.reaktywni.thumbnail.ThumbnailRepository;
import pl.edu.agh.to.reaktywni.thumbnail.ThumbnailSize;
import pl.edu.agh.to.reaktywni.util.Base64ImageDataCodec;
import pl.edu.agh.to.reaktywni.directory.Directory;
import pl.edu.agh.to.reaktywni.util.JsonFileReaderWriter;
import pl.edu.agh.to.reaktywni.util.Resizer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;


@Service
public class ImageService {

    private final static Logger logger = Logger.getLogger(ImageService.class.getName());

    private final ImageRepository imageRepository;
    private final ThumbnailRepository thumbnailRepository;
    private final Resizer imageResizer;
    private final JsonFileReaderWriter jsonFileReaderWriter;

    private final Directory directoryTree;

    public ImageService(ImageRepository imageRepository, ThumbnailRepository thumbnailRepository, Resizer imageResizer, JsonFileReaderWriter jsonFileReaderWriter) {
        this.imageRepository = imageRepository;
        this.thumbnailRepository = thumbnailRepository;
        this.imageResizer = imageResizer;
        this.jsonFileReaderWriter = jsonFileReaderWriter;

        Directory tempDirectoryTree;
        try {
            String json = jsonFileReaderWriter.read();
            tempDirectoryTree = Directory.parseFromJson(json);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error reading directory tree from file: " + e.getMessage());
            tempDirectoryTree = Directory.createRoot();
            try {
                jsonFileReaderWriter.write(tempDirectoryTree.toJson());
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Failed to save empty Root to file: " + ex.getMessage());
            }
        }
        this.directoryTree = tempDirectoryTree;
    }

    public Mono<Image> getImage(int id) {
        return Mono.fromCallable(() -> imageRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(Mono::justOrEmpty)
                .doOnNext(Base64ImageDataCodec::encode);
    }

    public Flux<Image> getThumbnails(String size, String directoryPath, Pageable pageable) {
        return Mono.fromCallable(() -> thumbnailRepository.findBySizeAndPath(ThumbnailSize.valueOf(size), directoryPath, pageable))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(thumbnail -> createImageFromThumbnail(thumbnail, directoryPath))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .doOnNext(Base64ImageDataCodec::encode);
    }

    public Flux<Image> getThumbnailsExcludingList(String size, String directoryPath, List<Integer> ids, int elemCount) {
        return Mono.fromCallable(() -> thumbnailRepository.findBySizeAndPathAndImageIdNotIn(ThumbnailSize.valueOf(size), directoryPath, ids, elemCount))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(thumbnail -> createImageFromThumbnail(thumbnail, directoryPath))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .doOnNext(Base64ImageDataCodec::encode);
    }

    public Mono<Long> getThumbnailsCount(String size, String directoryPath) {
        return Mono.fromCallable(() -> thumbnailRepository.countBySizeAndPath(ThumbnailSize.valueOf(size), directoryPath))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(Mono::justOrEmpty);
    }

    public Flux<Image> processImages(Flux<Image> images, String size, String directoryPath) {
        return images
                .doOnNext(Base64ImageDataCodec::decode)
                .doOnNext(this::logImageData)
                .map(imageRepository::save)
                .doOnNext(image -> logger.log(Level.INFO, "Image saved: " + image.getName() + " | Directory Path: " + image.getDirectoryPath()))
                .doOnNext(this::saveEmptyThumbnails)
                .doOnNext(image -> Mono.fromRunnable(() -> generateAndSaveOtherThumbnails(image, ThumbnailSize.valueOf(size)))
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe())
                .flatMap(image -> processThumbnailAndReturnImage(image, ThumbnailSize.valueOf(size)))
                .doOnNext(this::logProcessedData)
                .filter(image -> image.getDirectoryPath().equals(directoryPath))
                .doOnNext(Base64ImageDataCodec::encode);
    }

    @Transactional
    public void deleteDirectoryWithImages(String directoryPath) {
        List<Integer> ids = imageRepository.findAllIdsByDirectoryPath(directoryPath);
        deleteImagesWithId(ids);
        removeDirectoryFromTree(directoryPath);
    }

    @Transactional
    public void deleteImagesWithId(List<Integer> imageIds) {
        thumbnailRepository.deleteAllByImageIdIn(imageIds);
        imageRepository.deleteAllById(imageIds);
    }

    private void removeDirectoryFromTree(String directoryPath) {
        directoryTree.removeDirectory(directoryPath);
        try {
            jsonFileReaderWriter.write(directoryTree.toJson());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error writing directory tree to file: " + e.getMessage());
        }
    }

    private void saveEmptyThumbnails(Image image) {
        thumbnailRepository.saveAll(
                Arrays.stream(ThumbnailSize.values())
                        .map(size -> new Thumbnail(image, size))
                        .toList()
        );
    }

    private Mono<Image> processThumbnailAndReturnImage(Image image, ThumbnailSize thumbnailSize) {
        return generateAndUpdateThumbnail(image, thumbnailSize)
                .map(thumbnail -> createImageFromThumbnail(thumbnail, image.getName(), image.getExtensionType(), image.getDirectoryPath(), image.getGridId()));
    }

    private void generateAndSaveOtherThumbnails(Image image, ThumbnailSize thumbnailSize) {
        Flux.fromStream(Arrays.stream(ThumbnailSize.values())
                        .filter(size -> !size.equals(thumbnailSize)))
                .flatMap(size -> generateAndUpdateThumbnail(image, size))
                .subscribe();
    }

    private Mono<Thumbnail> generateAndUpdateThumbnail(Image image, ThumbnailSize thumbnailSize) {
        return Mono.fromCallable(() -> imageResizer.createThumbnail(image, thumbnailSize))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(thumbnail -> updateEmptyThumbnail(thumbnail, image, thumbnailSize))
                .doOnError(error -> updateEmptyThumbnailOnError(image, thumbnailSize)
                        .subscribe());
    }

    private Mono<Thumbnail> updateEmptyThumbnail(Thumbnail readyThumbnail, Image image, ThumbnailSize thumbnailSize) {
        return Mono.fromCallable(() -> {
            Thumbnail emptyThumbnail = thumbnailRepository.findByImageIdAndSize(image.getId(), thumbnailSize);
            if (readyThumbnail.getState().equals(ImageState.SUCCESS)) {
                emptyThumbnail.setData(readyThumbnail.getData());
            } else {
                emptyThumbnail.setFailure();
                logger.log(Level.WARNING, "Resizing image failure: " + image.getName());
            }
            thumbnailRepository.save(emptyThumbnail);
            return emptyThumbnail;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<Thumbnail> updateEmptyThumbnailOnError(Image image, ThumbnailSize thumbnailSize) {
        return Mono.fromCallable(() -> {
            Thumbnail emptyThumbnail = thumbnailRepository.findByImageIdAndSize(image.getId(), thumbnailSize);
            emptyThumbnail.setFailure();
            thumbnailRepository.save(emptyThumbnail);
            return emptyThumbnail;
        }).subscribeOn(Schedulers.boundedElastic());
    }


    private Optional<Image> createImageFromThumbnail(Thumbnail thumbnail, String directoryPath) {
        return imageRepository.findImageMetaDataById(thumbnail.getImageId())
                .map(image -> createImageFromThumbnail(thumbnail, image.getName(), image.getExtensionType(), directoryPath, -1));
    }

    public void createEmptyThumbnailsIfMissing() {
        long imagesCount = imageRepository.count();
        long thumbnailsCount = thumbnailRepository.count();
        if (imagesCount * ThumbnailSize.SIZES_COUNT == thumbnailsCount) {
            return;
        }
        logger.info("Creating missing thumbnails");
        Flux.fromIterable(imageRepository.findAllIdsWithMissingThumbnails(ThumbnailSize.SIZES_COUNT))
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(this::saveMissingThumbnails)
                .blockLast();
    }

    private Image createEmptyImageWithId(int id) {
        Image image = new Image();
        image.setId(id);
        return image;
    }

    private void saveMissingThumbnails(int imageId) {
        List<ThumbnailSize> existingSizes = thumbnailRepository.findByImageId(imageId).stream()
                .map(Thumbnail::getSize)
                .toList();
        List<Thumbnail> missingThumbnails = Arrays.stream(ThumbnailSize.values())
                .filter(size -> !existingSizes.contains(size))
                .map(size -> new Thumbnail(createEmptyImageWithId(imageId), size))
                .toList();
        thumbnailRepository.saveAll(missingThumbnails);
    }

    public void reprocessPendingThumbnails() {
        for (ImageState state : new ImageState[] {ImageState.PENDING, ImageState.FAILURE}) {
            reprocessPendingThumbnails(state);
        }
    }

    private void reprocessPendingThumbnails(ImageState state) {
        Flux.fromIterable(thumbnailRepository.findByStateWithImages(state))
                .doOnNext(this::logReprocessing)
                .flatMap(thumbnail -> Mono.fromCallable(thumbnail::getImage)
                        .subscribeOn(Schedulers.boundedElastic())
                        .flatMap(Mono::justOrEmpty)
                        .flatMap(image -> generateAndUpdateThumbnail(image, thumbnail.getSize()))
                        .doOnNext(this::logSuccessfulReprocessing)
                )
                .subscribe();
    }

    private Image createImageFromThumbnail(Thumbnail thumbnail, String imageName, String extensionType, String directoryPath, int gridId) {
        return Image.builder()
                .id(thumbnail.getImageId())
                .gridId(gridId)
                .imageState(thumbnail.getState())
                .name(imageName)
                .extensionType(extensionType)
                .width(thumbnail.getSize().getWidth())
                .height(thumbnail.getSize().getHeight())
                .data(thumbnail.getData())
                .directoryPath(directoryPath)
                .build();
    }

    private void logReprocessing(Thumbnail thumbnail) {
        logger.log(Level.INFO, "Reprocessing: " + thumbnail + " | Size: " + thumbnail.getSize());
    }

    private void logSuccessfulReprocessing(Thumbnail thumbnail) {
        logger.info("Processed Thumbnail: " + thumbnail);
    }

    private void logImageData(Image image) {
        logger.log(Level.INFO, "Received: " + image);
    }

    private void logProcessedData(Image image) {
        logger.log(Level.INFO, "To be sent: " + image);
    }

    public Mono<Directory> getDirectoryTree() {
        return Mono.just(directoryTree);
    }

    public void mergeToDirectoryTree(Directory directory) {
        logger.info("Received directory: " + directory);
        directoryTree.merge(directory);
        try {
            jsonFileReaderWriter.write(directoryTree.toJson());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error writing directory tree to file: " + e.getMessage());
        }
    }

    @Transactional
    public void moveImagesToDirectory(List<Integer> imageIds, String directoryPath) {
        imageRepository.updateDirectoryPathForIds(imageIds, directoryPath);
    }
}
