package pl.edu.agh.to.reaktywni;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
@Profile({"dev", "prod"})
public class ReaktywniClientApp {

	public static void main(String[] args) {
		Application.launch(ImageGalleryApp.class, args);
	}
}