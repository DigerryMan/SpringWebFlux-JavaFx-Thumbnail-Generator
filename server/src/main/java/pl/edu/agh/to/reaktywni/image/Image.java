package pl.edu.agh.to.reaktywni.image;

import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Transient
    private int gridId;

    @Transient
    @Builder.Default
    private ImageState imageState = ImageState.PENDING;

    private String name;

    private String extensionType;

    private int width;

    private int height;

    @Lob
    private byte[] data;

    private String directoryPath;

    @Override
    public String toString() {
        return "Image{" +
                "id=" + id +
                ", gridId=" + gridId +
                ", imageState=" + imageState +
                ", name='" + name + '\'' +
                ", extensionType='" + extensionType + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", data_len=" + data.length +
                '}';
    }
}
