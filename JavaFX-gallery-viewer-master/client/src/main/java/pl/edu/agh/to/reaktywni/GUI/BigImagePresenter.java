package pl.edu.agh.to.reaktywni.GUI;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.springframework.stereotype.Component;
import java.util.Objects;


@Component
public class BigImagePresenter {

    @FXML
    private ImageView imageView;

    @FXML
    private Label nameLabel;

    @FXML
    private Label sizeLabel;

    private static final Image loadingDots = new Image(
            Objects.requireNonNull(BigImagePresenter.class.getResourceAsStream("/GUI/icons/loading.gif")),
            620, 349, true, true
    );

    private static final javafx.scene.image.Image errorImage = new javafx.scene.image.Image(
            Objects.requireNonNull(ImageGalleryPresenter.class.getResourceAsStream("/GUI/icons/error.png")),
            620, 349, true, true
    );

    public void initialize() {
        imageView.setImage(loadingDots);
    }

    public void setImage(Image image) {
        imageView.setImage(image);
    }

    public void setName(String name) {
        nameLabel.setText(name);
    }

    public void setSize(int width, int height) {
        sizeLabel.setText(width + "x" + height);
    }

    public void setErrorImage(String errorMessage) {
        nameLabel.setText("Error: " + errorMessage);
        sizeLabel.setText("..............");
        imageView.setImage(errorImage);
    }
}
