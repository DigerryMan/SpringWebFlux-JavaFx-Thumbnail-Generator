package pl.edu.agh.to.reaktywni.thumbnail;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import pl.edu.agh.to.reaktywni.image.Image;
import pl.edu.agh.to.reaktywni.image.ImageService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;


@RestController
@RequestMapping("/thumbnails")
public class ThumbnailController {

    private final ImageService imageService;

    public ThumbnailController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping()
    public Flux<Image> getThumbnails(@RequestParam String size, @RequestParam String directoryPath, @RequestParam int page, @RequestParam int pageSize) {
        if (!ThumbnailSize.isValidSize(size)) {
            return Flux.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid thumbnail size"));
        }
        return imageService.getThumbnails(size, directoryPath, PageRequest.of(page, pageSize));
    }

    @GetMapping("/excluding")
    public Flux<Image> getThumbnailsExcludingList(@RequestParam String size, @RequestParam String directoryPath, @RequestParam List<Integer> ids, @RequestParam int elemCount) {
        if (!ThumbnailSize.isValidSize(size)) {
            return Flux.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid thumbnail size"));
        }
        return imageService.getThumbnailsExcludingList(size, directoryPath, ids, elemCount);
    }

    @GetMapping("/count")
    public Mono<Long> getThumbnailsCount(@RequestParam String size, @RequestParam String directoryPath) {
        if (!ThumbnailSize.isValidSize(size)) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid thumbnail size"));
        }
        return imageService.getThumbnailsCount(size, directoryPath)
                .switchIfEmpty(Mono.error(new IllegalStateException("Cannot get thumbnails count")));
    }
}
