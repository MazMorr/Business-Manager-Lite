package com.marcosoft.storageSoftware.infrastructure.util;

import javafx.fxml.FXMLLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class SpringFXMLLoader {

    private final ApplicationContext context;
    private FXMLLoader loader;

    public SpringFXMLLoader(ApplicationContext context) {
        this.context = context;
    }

    public <T> T load(String fxmlPath) throws IOException {
        try (InputStream fxmlStream = getClass().getResourceAsStream(fxmlPath)) {
            if (fxmlStream == null) {
                throw new IOException("Cannot load FXML file: " + fxmlPath);
            }
            loader = new FXMLLoader();
            loader.setControllerFactory(context::getBean);
            loader.setLocation(getClass().getResource(fxmlPath));
            return loader.load(fxmlStream);
        }
    }

    public <T> T getController(Class<T> controllerClass) {
        if (loader == null) {
            throw new IllegalStateException("FXMLLoader has not been initialized. Call load() first.");
        }
        return loader.getController();
    }
}