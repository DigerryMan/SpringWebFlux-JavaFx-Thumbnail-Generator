package pl.edu.agh.to.reaktywni.thumbnail;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.to.reaktywni.image.Image;
import pl.edu.agh.to.reaktywni.image.ImageState;


@Getter
@Entity
@Table(indexes = {
        @Index(name = "idx_original_image", columnList = "IMAGE_ID"),
        @Index(name = "idx_thumbnail_size", columnList = "SIZE")
})
public class Thumbnail {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Image image;

    private ThumbnailSize size;

    @Lob private byte[] data;

    @Setter
    @Enumerated(EnumType.STRING)
    private ImageState state;

    public Thumbnail() {}

    public Thumbnail(Image image, ThumbnailSize size) {
        this.image = image;
        this.size = size;
        this.data = new byte[0];
        this.state = ImageState.PENDING;
    }

    public int getImageId() {
        return image.getId();
    }

    public void setData(byte[] data) {
        this.data = data;
        this.state = ImageState.SUCCESS;
    }

    @Override
    public String toString() {
        return "Thumbnail{" +
                "id=" + id +
                ", imageId=" + getImageId() +
                ", size=" + size +
                ", data=" + getPrintingData() +
                ", state=" + state +
                '}';
    }

    private String getPrintingData() {
        if (data == null || data.length == 0) {
            return "[]";
        }
        return "[length=" + data.length + "]";
    }

    public void setFailure() {
        this.state = ImageState.FAILURE;
    }
}
