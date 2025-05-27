package pl.edu.agh.to.reaktywni.directory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

public class DirectoryTest {

    private Directory root;

    @BeforeEach
    void setUp() {
        root = Directory.createRoot();
    }

    @Test
    void testCreateRootDirectory() {
        assertEquals("Root", root.name());
        assertTrue(root.subdirectories().isEmpty());
    }

    @Test
    void testToJson() throws JsonProcessingException {
        String expectedJson = "{\"name\":\"Root\",\"subdirectories\":[{\"name\":\"Sub\",\"subdirectories\":[]}]}";

        Directory subDir = new Directory("Sub", new TreeSet<>());
        root.subdirectories().add(subDir);
        String json = root.toJson();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode actualJsonNode = objectMapper.readTree(json);
        JsonNode expectedJsonNode = objectMapper.readTree(expectedJson);

        assertNotNull(json);
        assertEquals(expectedJsonNode, actualJsonNode);
    }

    @Test
    void testParseFromJson() throws JsonProcessingException {
        String json = "{\"name\":\"Root\",\"subdirectories\":[{\"name\":\"Sub\",\"subdirectories\":[]}]}";
        Directory parsed = Directory.parseFromJson(json);
        assertEquals("Root", parsed.name());
        assertEquals(1, parsed.subdirectories().size());
        assertEquals("Sub", parsed.subdirectories().first().name());
    }

    @Test
    void testMergeDirectories() {
        Directory dirA = new Directory("A", new TreeSet<>());
        Directory dirB = new Directory("B", new TreeSet<>());
        root.subdirectories().add(dirA);

        Directory otherRoot = Directory.createRoot();
        otherRoot.subdirectories().add(dirB);
        otherRoot.subdirectories().add(new Directory("A", new TreeSet<>())) ;

        root.merge(otherRoot);
        assertEquals(2, root.subdirectories().size());
        assertTrue(root.subdirectories().stream().anyMatch(d -> d.name().equals("A")));
        assertTrue(root.subdirectories().stream().anyMatch(d -> d.name().equals("B")));
    }

    @Test
    void testRemoveDirectory() {
        Directory dirA = new Directory("A", new TreeSet<>());
        Directory dirB = new Directory("B", new TreeSet<>());
        dirA.subdirectories().add(dirB);
        root.subdirectories().add(dirA);

        root.removeDirectory("A/B");
        assertFalse(dirA.subdirectories().contains(dirB));
    }

    @Test
    void testRemoveNonExistingDirectory() {
        Directory dirA = new Directory("A", new TreeSet<>());
        root.subdirectories().add(dirA);

        root.removeDirectory("A/C");

        assertTrue(root.subdirectories().contains(dirA));
    }

    @Test
    void testCompareTo() {
        Directory dirA = new Directory("A", new TreeSet<>());
        Directory dirB = new Directory("B", new TreeSet<>());
        Directory secondDirA = new Directory("A", new TreeSet<>());
        assertTrue(dirA.compareTo(dirB) < 0);
        assertTrue(dirB.compareTo(dirA) > 0);
        assertEquals(0, dirA.compareTo(secondDirA));
    }
}
