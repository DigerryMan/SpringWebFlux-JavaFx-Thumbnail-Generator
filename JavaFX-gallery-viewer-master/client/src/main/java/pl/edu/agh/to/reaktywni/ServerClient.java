package pl.edu.agh.to.reaktywni;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pl.edu.agh.to.reaktywni.model.Image;
import pl.edu.agh.to.reaktywni.util.ZipDataExtractor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


@Component
public class ServerClient {

    private static final Logger logger = Logger.getLogger(ServerClient.class.getName());

    private final WebClient webClient;

    public ServerClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
    }

    public Mono<Image> getFullImage(int id) {
        return webClient.get()
            .uri("/images/{id}", id)
            .retrieve()
            .bodyToMono(Image.class)
            .doOnError(e -> logger.log(Level.SEVERE, "getFullImageError: " + e.getMessage()));
    }

    public Flux<Image> sendImages(Flux<Image> images, String thumbnailSize, String directoryPath) {
        return webClient.post()
                .uri("/images?size={size}&directoryPath={directoryPath}", thumbnailSize, directoryPath)
                .contentType(MediaType.APPLICATION_NDJSON)
                .body(images, Image.class)
                .retrieve()
                .bodyToFlux(Image.class)
                .doOnError(e -> logger.log(Level.SEVERE, "sendImagesError: " + e.getMessage()));
    }

    public Flux<Image> getThumbnails(String thumbnailSize, String directoryPath, int page, int pageSize) {
        return webClient.get()
                .uri("/thumbnails?size={size}&directoryPath={directoryPath}&page={page}&pageSize={pageSize}", thumbnailSize, directoryPath, page, pageSize)
                .retrieve()
                .bodyToFlux(Image.class)
                .doOnError(e -> logger.log(Level.SEVERE, "getThumbnailsError: " + e.getMessage()));
    }

    public Flux<Image> getThumbnailsExcludingSet(String thumbnailSize, String directoryPath, List<Integer> ids, int elemCount) {
        return webClient.get()
                .uri("/thumbnails/excluding?size={size}&directoryPath={directoryPath}&ids={ids}&elemCount={elemCount}", thumbnailSize, directoryPath, convertListToString(ids), elemCount)
                .retrieve()
                .bodyToFlux(Image.class)
                .doOnError(e -> logger.log(Level.SEVERE, "getThumbnailsExcludingListError: " + e.getMessage()));
    }

    private String convertListToString(List<Integer> ids) {
        return String.join(",", ids.stream().map(String::valueOf).toList());
    }

    public Mono<Long> getThumbnailsCount(String thumbnailSize, String directoryPath) {
        return webClient.get()
                .uri("/thumbnails/count?size={size}&directoryPath={directoryPath}", thumbnailSize, directoryPath)
                .retrieve()
                .bodyToMono(Long.class)
                .doOnError(e -> logger.log(Level.SEVERE, "getThumbnailsCountError: " + e.getMessage()));
    }

    public Mono<ZipDataExtractor.Directory> getDirectoryTree() {
        return webClient.get()
                .uri("/directory-tree")
                .retrieve()
                .bodyToMono(ZipDataExtractor.Directory.class)
                .doOnError(e -> logger.log(Level.SEVERE, "getDirectoryTreeError: " + e.getMessage()));
    }

    public void deleteDirectoryWithImages(String directoryPath) {
        webClient.delete()
                .uri("/directory-tree?directoryPath={directoryPath}", directoryPath)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(e -> logger.log(Level.SEVERE, "deleteDirectoryWithImagesError: " + e.getMessage()))
                .subscribe();
    }

    public void deleteImagesWithId(List<Integer> imageIds) {
        webClient.delete()
                .uri("/images?ids={ids}", convertListToString(imageIds))
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(e -> logger.log(Level.SEVERE, "deleteImagesWithIdError: " + e.getMessage()))
                .block();
    }

    public void postDirectoryTree(ZipDataExtractor.Directory directory) {
        webClient.post()
                .uri("/directory-tree")
                .bodyValue(directory)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(e -> logger.log(Level.SEVERE, "postDirectoryTreeError: " + e.getMessage()))
                .block();
    }

    public void moveSelectedImagesToDirectory(List<Integer> imageIds, String directoryPath) {
        webClient.patch()
                .uri("/images/transfer?directoryPath={directoryPath}", directoryPath)
                .bodyValue(imageIds)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(e -> logger.log(Level.SEVERE, "moveSelectedImagesToDirectoryError: " + e.getMessage()))
                .block();
    }
}



