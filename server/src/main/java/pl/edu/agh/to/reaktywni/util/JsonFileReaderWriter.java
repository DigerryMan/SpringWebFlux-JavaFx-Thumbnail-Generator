package pl.edu.agh.to.reaktywni.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


@Component
public class JsonFileReaderWriter {

    private final Path jsonFilePath;

    public JsonFileReaderWriter(@Value("${directory.tree.file.path}") String jsonFileResource) {
        this.jsonFilePath = Paths.get(jsonFileResource);
    }

    public String read() throws IOException {
        return new String(Files.readAllBytes(jsonFilePath));
    }

    public void write(String string) throws IOException {
        Path parentDir = jsonFilePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) Files.createDirectories(parentDir);
        Files.write(jsonFilePath, string.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
