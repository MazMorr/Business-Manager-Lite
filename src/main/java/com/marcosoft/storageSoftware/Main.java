package com.marcosoft.storageSoftware;

import com.marcosoft.storageSoftware.infrastructure.util.SpringFXMLLoader;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
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
        "com.marcosoft.storageSoftware.application.dto"
})
@EntityScan(basePackages = "com.marcosoft.storageSoftware.domain.model")
@EnableJpaRepositories(basePackages = "com.marcosoft.storageSoftware.domain.repository")
public class Main extends Application {

    @Getter
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
        Parent root = springFXMLLoader.load("/clientView.fxml");
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
        if (context != null && context.isActive()) {
            context.close();
        }
    }

    public static void setRoot(String fxml) {
        try {
            Parent root = springFXMLLoader.load("/" + fxml + ".fxml");
            scene.setRoot(root);
            primaryStage.sizeToScene();
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("Error al cargar la vista: " + fxml);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}