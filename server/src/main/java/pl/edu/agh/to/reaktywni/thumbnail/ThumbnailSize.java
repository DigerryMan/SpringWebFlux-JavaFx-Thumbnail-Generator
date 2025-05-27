package pl.edu.agh.to.reaktywni.thumbnail;

import lombok.Getter;

import java.util.stream.Stream;


@Getter
public enum ThumbnailSize {

    SMALL(160, 90),
    MEDIUM(320, 180),
    LARGE(480, 270);

    private final int width;
    private final int height;

    public static final int SIZES_COUNT = ThumbnailSize.values().length;

    ThumbnailSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public static boolean isValidSize(String size) {
        return Stream.of(ThumbnailSize.values())
                .map(Enum::toString)
                .anyMatch(size::equalsIgnoreCase);
    }
}
