package pl.edu.agh.to.reaktywni;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import pl.edu.agh.to.reaktywni.GUI.StageInitializer;

import java.util.Objects;


public class ImageGalleryApp extends Application {

    private ConfigurableApplicationContext applicationContext;

    public static final Image icon = new Image(Objects.requireNonNull(StageInitializer.class.getResourceAsStream("/GUI/icons/gallery.png")));

    @Override
    public void init() {
        applicationContext = new SpringApplicationBuilder(ReaktywniClientApp.class).run();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/image_gallery_view.fxml"));
        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();
        stage.setTitle("Image Gallery");
        stage.getIcons().add(icon);
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Override
    public void stop() {
        applicationContext.close();
        Platform.exit();
    }
}
