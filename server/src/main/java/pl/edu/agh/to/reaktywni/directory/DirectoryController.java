package pl.edu.agh.to.reaktywni.directory;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pl.edu.agh.to.reaktywni.image.ImageService;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/directory-tree")
public class DirectoryController {

    private final ImageService imageService;

    public DirectoryController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping
    public Mono<Directory> getDirectoryTree() {
        return imageService.getDirectoryTree()
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "No directory tree found")));
    }

    @PostMapping
    public void postDirectoryTree(@RequestBody Directory directory) {
        imageService.mergeToDirectoryTree(directory);
    }

    @DeleteMapping
    public void deleteDirectoryWithImages(@RequestParam String directoryPath) {
        imageService.deleteDirectoryWithImages(directoryPath);
    }
}
