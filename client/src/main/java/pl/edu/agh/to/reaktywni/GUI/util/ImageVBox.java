package pl.edu.agh.to.reaktywni.GUI.util;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import lombok.Getter;
import pl.edu.agh.to.reaktywni.GUI.ImageGalleryPresenter;
import pl.edu.agh.to.reaktywni.model.Image;
import pl.edu.agh.to.reaktywni.model.ImageState;
import java.io.ByteArrayInputStream;


public class ImageVBox extends VBox {

    @Getter
    private final ImageView imageView = new ImageView();
    private final Label nameLabel = new Label();

    private final int startGridId;
    @Getter private int imageId;
    @Getter private boolean selected = false;

    public ImageVBox(int startGridId, ThumbnailSize size, String name, int imageNameHeight) {
        this.startGridId = startGridId;
        imageView.setImage(size.getPlaceholder());
        nameLabel.setText(name);
        nameLabel.setWrapText(true);
        setMinHeight(size.getImageHeight() + imageNameHeight);
        setMaxHeight(size.getImageHeight() + imageNameHeight);
        setAlignment(Pos.TOP_CENTER);
        setSpacing(5);
        getChildren().addAll(imageView, nameLabel);
        nameLabel.getStyleClass().add("image-name");
    }

    private void onSingleClickSelectionEvent(MouseEvent event, ImageGalleryPresenter presenter) {
        boolean multipleSelection = (event.isControlDown() || event.isShiftDown());
        if (!selected) {
            getStyleClass().add("image-vbox-selected");
            nameLabel.setStyle("-fx-text-fill: #ffffff;");
            presenter.addToSelection(this, multipleSelection);
            selected = true;
        }
        else {
            if (presenter.removeFromSelection(this, multipleSelection))
                disableSelection();
        }
    }

    public void disableSelection() {
        getStyleClass().remove("image-vbox-selected");
        nameLabel.setStyle("");
        selected = false;
    }

    public int getGridId() {
        return startGridId;
    }

    public void placeImage(ThumbnailSize size, Image image, ImageGalleryPresenter presenter) {
        if (image.getImageState() == ImageState.SUCCESS) {
            imageId = image.getId();
            nameLabel.setText(image.getName());
            imageView.setImage(new javafx.scene.image.Image(new ByteArrayInputStream(image.getData()), size.getImageWidth(), size.getImageHeight(), false, false));
            setOnMouseClicked(event -> {
                if (event.getButton().equals(MouseButton.PRIMARY)) {
                    if (event.getClickCount() == 1) onSingleClickSelectionEvent(event, presenter);
                    else if (event.getClickCount() == 2) presenter.openBigImageView(image.getId());
                }
            });
        } else if (image.getImageState() == ImageState.PENDING) {
            nameLabel.setText("............");
            imageView.setImage(size.getPlaceholder());
        }
        else {
            nameLabel.setText("Error: " + image.getName());
            imageView.setImage(size.getErrorImage());
        }
    }
}
