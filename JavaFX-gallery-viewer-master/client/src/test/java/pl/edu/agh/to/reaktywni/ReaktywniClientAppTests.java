package pl.edu.agh.to.reaktywni;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import pl.edu.agh.to.reaktywni.model.Image;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = ReaktywniClientApp.class)
@ActiveProfiles("test")
class ReaktywniClientAppTests {

	@Mock
	private WebClient.Builder webClientBuilder;

	@Mock
	private WebClient webClient;

	@Mock
	private WebClient.RequestBodyUriSpec requestBodyUriSpec;

	@Mock
	private WebClient.ResponseSpec responseSpec;

	private ServerClient serverClient;

	private Image loadImage(String imageName, int id) throws IOException {
		File file = new File("src/test/resources/" + imageName);
		String extensionType = getFileExtension(file.getName());
		int[] size = getImageDimensions(file, extensionType);
		return Image.builder()
				.gridId(id)
				.name(file.getName())
				.extensionType(extensionType)
				.width(size[0])
				.height(size[1])
				.data(Files.readAllBytes(file.toPath()))
				.build();
	}

	private static String getFileExtension(String fileName) {
		int dotIndex = fileName.lastIndexOf(".");
		if (dotIndex <= 0) throw new IllegalArgumentException("No extension for file: " + fileName);
		return fileName.substring(dotIndex + 1);
	}

	private static int[] getImageDimensions(File imgFile, String extensionType) throws IOException {
		Iterator<ImageReader> it = ImageIO.getImageReadersBySuffix(extensionType);
		if (!it.hasNext()) throw new IOException("File format not supported: " + imgFile.getAbsolutePath());
		ImageReader reader = it.next();

		try (ImageInputStream stream = new FileImageInputStream(imgFile)) {
			reader.setInput(stream);
			return new int[]{reader.getWidth(reader.getMinIndex()), reader.getHeight(reader.getMinIndex())};
		} catch (IOException e) {
			throw new IOException("Error reading image dimensions for file: " + imgFile.getAbsolutePath(), e);
		} finally {
			reader.dispose();
		}
	}

	@BeforeEach
	void setUp() {
		when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
		when(webClientBuilder.build()).thenReturn(webClient);

		serverClient = new ServerClient(webClientBuilder);
	}

	@Test
	public void testSendImages() throws IOException {
		Flux<Image> images = Flux.just(
				loadImage("test-pepe.jpg", 0),
				loadImage("test-png.png", 1)
		);

		when(webClient.post()).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.uri(eq("/images?size={size}"), eq(new Object[]{"MEDIUM"}))).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.contentType(eq(MediaType.APPLICATION_NDJSON))).thenReturn(requestBodyUriSpec);
		doAnswer(invocation -> requestBodyUriSpec).when(requestBodyUriSpec).body(any(), eq(Image.class));
		when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
		when(responseSpec.bodyToFlux(eq(Image.class))).thenReturn(Flux.just(
				loadImage("test-pepe.jpg", 0),
				loadImage("test-png.png", 1)
		));

		Flux<Image> result = serverClient.sendImages(images, "MEDIUM", "/");

		StepVerifier.create(result)
				.expectNextMatches(image -> image.getName().equals("test-pepe.jpg"))
				.expectNextMatches(image -> image.getName().equals("test-png.png"))
				.verifyComplete();
	}
}
