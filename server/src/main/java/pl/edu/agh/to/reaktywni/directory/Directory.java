package pl.edu.agh.to.reaktywni.directory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.TreeSet;

public record Directory(String name, TreeSet<Directory> subdirectories) implements Comparable<Directory> {

    private Directory(String name) {
        this(name, new TreeSet<>());
    }

    public static Directory createRoot() {
        return new Directory("Root");
    }

    private Optional<Directory> findDirectoryWithName(TreeSet<Directory> directories, String name) {
        Directory foundElement = directories.floor(new Directory(name, null));
        return (foundElement != null && foundElement.name.equals(name)) ? Optional.of(foundElement) : Optional.empty();
    }

    @Override
    public String toString() {
        if (subdirectories.isEmpty()) return name;
        else return name + ": " + subdirectories;
    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
    }

    public static Directory parseFromJson(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, Directory.class);
    }

    public void merge(Directory otherDir) {
        for (Directory otherSubDir : otherDir.subdirectories) {
            Optional<Directory> existingDir = findDirectoryWithName(this.subdirectories, otherSubDir.name);
            if (existingDir.isEmpty()) this.subdirectories.add(otherSubDir);
            else existingDir.get().merge(otherSubDir);
        }
    }

    public void removeDirectory(String directoryPath) {
        TreeSet<Directory> currSubDirs = subdirectories;
        String[] directoryNames = directoryPath.split("/");
        int lastIndex = directoryNames.length - 1;
        for (int i = 0; i <= lastIndex; i++) {
            if (directoryNames[i].isBlank()) continue;
            Optional<Directory> existingDir = findDirectoryWithName(currSubDirs, directoryNames[i]);
            if (existingDir.isEmpty()) break;
            if (i == lastIndex) {
                currSubDirs.remove(existingDir.get());
                break;
            } else currSubDirs = existingDir.get().subdirectories;
        }
    }

    @Override
    public int compareTo(Directory o) {
        return name.compareTo(o.name);
    }
}