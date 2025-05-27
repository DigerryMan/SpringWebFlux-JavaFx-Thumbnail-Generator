package pl.edu.agh.to.reaktywni.image;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import pl.edu.agh.to.reaktywni.thumbnail.Thumbnail;
import pl.edu.agh.to.reaktywni.thumbnail.ThumbnailRepository;
import pl.edu.agh.to.reaktywni.thumbnail.ThumbnailSize;
import reactor.core.publisher.Flux;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ImageServiceTest {

    @Autowired
    private ImageService imageService;

    @Autowired
    private ThumbnailRepository thumbnailRepository;

    @Autowired
    private ImageRepository imageRepository;

    private static Image getTestImage(int width, int height, int size) {
        return Image.builder()
                .gridId(1)
                .name("test-image")
                .extensionType("jpg")
                .width(width)
                .height(height)
                .data(new byte[size])
                .build();
    }

    private static Image getExistingImage(ByteArrayOutputStream byteArrayOutputStream, BufferedImage originalImage) {
        byte[] imageData = byteArrayOutputStream.toByteArray();

        return Image.builder()
                .name("test-image")
                .extensionType("jpg")
                .data(Base64.getEncoder().encodeToString(imageData).getBytes())
                .width(originalImage.getWidth())
                .height(originalImage.getHeight())
                .build();
    }

    @Test
    public void testProcessImages_Success() throws IOException {
        File imageFile = new File("src/test/resources/test-tiger.jpg");
        if (!imageFile.exists()) {
            throw new IOException("Image doesn't exist at path: " + imageFile.getAbsolutePath());
        }

        BufferedImage originalImage = ImageIO.read(imageFile);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(originalImage, "jpg", byteArrayOutputStream);
        Image image = getExistingImage(byteArrayOutputStream, originalImage);
        imageRepository.save(image);

        Flux<Image> processedImages = imageService.processImages(Flux.just(image), "MEDIUM", "/");

        StepVerifier.create(processedImages)
                .expectNextMatches(i -> {
                    assertNotNull(i);
                    assertEquals(320, i.getWidth());
                    assertEquals(180, i.getHeight());
                    assertEquals("test-image", i.getName());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    public void testGetThumbnails() {
        thumbnailRepository.deleteAll();
        imageRepository.deleteAll();

        ThumbnailSize size = ThumbnailSize.MEDIUM;
        Image image = getTestImage(500, 300, 100);
        imageRepository.save(image);

        Thumbnail thumbnail = new Thumbnail(image, size);
        thumbnail.setData(new byte[50]);
        thumbnailRepository.save(thumbnail);

        Flux<Image> thumbnails = imageService.getThumbnails(String.valueOf(size), "/", Pageable.unpaged());

        StepVerifier.create(thumbnails)
                .expectNextMatches(t -> {
                    assertEquals(size.getWidth(), t.getWidth());
                    assertEquals(size.getHeight(), t.getHeight());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    public void getThumbnailsCountTest() {
        Image image = imageRepository.save(getTestImage(500, 300, 100));

        List<Thumbnail> thumbnails = List.of(
                new Thumbnail(image, ThumbnailSize.SMALL),
                new Thumbnail(image, ThumbnailSize.SMALL),
                new Thumbnail(image, ThumbnailSize.MEDIUM),
                new Thumbnail(image, ThumbnailSize.LARGE),
                new Thumbnail(image, ThumbnailSize.LARGE),
                new Thumbnail(image, ThumbnailSize.LARGE)
        );
        thumbnailRepository.saveAll(thumbnails);
        String directoryPath = "/";
        Optional<Long> smallCount = imageService.getThumbnailsCount(String.valueOf(ThumbnailSize.SMALL), directoryPath).blockOptional();
        if (smallCount.isEmpty()) fail("smallCount is empty");
        Optional<Long> mediumCount = imageService.getThumbnailsCount(String.valueOf(ThumbnailSize.MEDIUM), directoryPath).blockOptional();
        if (mediumCount.isEmpty()) fail("mediumCount is empty");
        Optional<Long> largeCount = imageService.getThumbnailsCount(String.valueOf(ThumbnailSize.LARGE), directoryPath).blockOptional();
        if (largeCount.isEmpty()) fail("largeCount is empty");

        assertEquals(2L, smallCount.get());
        assertEquals(1L, mediumCount.get());
        assertEquals(3L, largeCount.get());
    }
}
