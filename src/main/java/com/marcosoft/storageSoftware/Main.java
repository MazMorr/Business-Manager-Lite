package com.marcosoft.storageSoftware;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class Main extends Application {
    private static Scene scene;

    public static void main(String[] args) {
        System.out.println("Spring Boot Iniciado");
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        var context = SpringApplication.run(Main.class);
        var fxml = new FXMLLoader(getClass().getResource("/accountView.fxml"));
        String title = "Sistema de cuentas";

        Parent root = fxml.load();
        scene = new Scene(root); // Inicializa la variable scene aquí
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResource("/images/RTS_logo.png").toString()));
        stage.setResizable(false);
        stage.setTitle(title);
        stage.centerOnScreen();
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(com.marcosoft.storageSoftware.Main.class.getResource("/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

}
