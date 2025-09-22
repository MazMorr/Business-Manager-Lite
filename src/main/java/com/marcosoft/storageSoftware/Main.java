package com.marcosoft.storageSoftware;

import com.marcosoft.storageSoftware.infrastructure.util.SpringFXMLLoader;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.JdbcClientAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication(exclude = {
        CacheAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        ValidationAutoConfiguration.class,
        TaskExecutionAutoConfiguration.class,
        TaskSchedulingAutoConfiguration.class,
        MailSenderAutoConfiguration.class,
        JmxAutoConfiguration.class,
        JdbcClientAutoConfiguration.class
})
@ComponentScan(basePackages = {
        "com.marcosoft.storageSoftware.infrastructure.util",
        "com.marcosoft.storageSoftware.infrastructure.service.impl",
        "com.marcosoft.storageSoftware.application.controller",
        "com.marcosoft.storageSoftware.application.dto",
        "com.marcosoft.storageSoftware.infrastructure.security",
        "com.marcosoft.storageSoftware.infrastructure.config"
})
@EntityScan(basePackages = "com.marcosoft.storageSoftware.domain.model")
@EnableJpaRepositories(basePackages = "com.marcosoft.storageSoftware.domain.repository")
public class Main extends Application {

    @Getter
    private static ConfigurableApplicationContext context;
    public static SpringFXMLLoader springFXMLLoader;
    public static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        try {
            Main.primaryStage = primaryStage;
            showLoadingScreen(primaryStage);
            startSpringApplicationAsync();
        } catch (Exception e) {
            handleStartupError(e);
        }
    }

    private void startSpringApplicationAsync() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                context = SpringApplication.run(Main.class);
                springFXMLLoader = context.getBean(SpringFXMLLoader.class);

                Platform.runLater(() -> {
                    try {
                        loadMainInterface();
                    } catch (Exception e) {
                        handleStartupError(e);
                    }
                });

            } catch (Exception e) {
                // Manejar errores de Spring/BD en el hilo secundario
                Platform.runLater(() -> handleSpringStartupError(e));
            }
        });
        executor.shutdown();
    }

    private void handleStartupError(Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Ha ocurrido un error al iniciar la aplicación: ");
        alert.setContentText(e.getMessage());
        alert.showAndWait();
        Platform.exit();
    }

    private void handleSpringStartupError(Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Ha ocurrido un error, seguramente haya abierto dos veces la aplicación");
        alert.setContentText(e.getMessage());
        alert.showAndWait();
        Platform.exit();
    }

    private void showLoadingScreen(Stage stage) {

        ImageView logo = new ImageView();
        try {
            String imagePath = Objects.requireNonNull(getClass().getResource("/images/lazy_compile_logo.png")).toString();
            logo.setImage(new Image(imagePath));
        } catch (Exception e) {
            System.err.println("Error al cargar el logo: " + e.getMessage());
            e.printStackTrace(); // Muestra el stack trace completo
        }
        logo.setFitWidth(200);
        logo.setPreserveRatio(true);

        VBox loadingLayout = new VBox(20, logo);
        loadingLayout.setAlignment(Pos.CENTER);

        Scene loadingScene = new Scene(loadingLayout, 400, 300);
        loadingScene.setCursor(Cursor.WAIT);
        stage.setScene(loadingScene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(false);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/images/lc_logo.png")).toString()));
        stage.show();
    }

    private void loadMainInterface() throws IOException {
        Parent root = springFXMLLoader.load("/views/loginView.fxml");
        Scene scene = new Scene(root);
        scene.setCursor(Cursor.DEFAULT);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    @Override
    public void stop() {
        if (context != null) {
            context.close();
        }
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        // Lanzar JavaFX primero
        launch(args);
    }
}