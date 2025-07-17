package com.marcosoft.storageSoftware;

import com.marcosoft.storageSoftware.util.SpringFXMLLoader;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

@SpringBootApplication(scanBasePackages = {
        "com.marcosoft.storageSoftware.controller",
        "com.marcosoft.storageSoftware.service.impl",
        "com.marcosoft.storageSoftware.repository",
        "com.marcosoft.storageSoftware.util",
        "com.marcosoft.storagesoftware.model"
})
public class Main extends Application {

    private static ConfigurableApplicationContext context;
    private static SpringFXMLLoader springFXMLLoader;
    private static Stage primaryStage;
    private static Scene scene;

    @Override
    public void init() throws Exception {
        context = SpringApplication.run(Main.class);
        springFXMLLoader = context.getBean(SpringFXMLLoader.class);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Main.primaryStage = primaryStage;
        Parent root = (Parent) springFXMLLoader.load("/clientView.fxml");
        scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image(getClass().getResource("/images/RTS_logo.png").toString()));
        primaryStage.setResizable(false);
        primaryStage.setTitle("Sistema de cuentas");
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        context.close();
    }

    public static void setRoot(String fxml) {
        try {
            Parent root = (Parent) springFXMLLoader.load("/" + fxml + ".fxml");
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            // Puedes agregar un manejo de errores más sofisticado aquí si es necesario
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
