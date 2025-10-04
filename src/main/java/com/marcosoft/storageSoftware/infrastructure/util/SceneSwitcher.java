package com.marcosoft.storageSoftware.infrastructure.util;

import com.marcosoft.storageSoftware.Main;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.beans.BeansException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
public class SceneSwitcher {
    private final SpringFXMLLoader springFXMLLoader;
    private final DisplayAlerts displayAlerts;

    public SceneSwitcher(DisplayAlerts displayAlerts, SpringFXMLLoader springFXMLLoader) {
        this.springFXMLLoader = springFXMLLoader;
        this.displayAlerts = displayAlerts;
    }

    /**
     * Cambia la raíz de la escena usando un ActionEvent.
     *
     * @param event    El evento que disparó el cambio
     * @param fxmlFile Ruta del archivo FXML a cargar
     * @throws ViewLoadException Si no se puede cargar la vista
     */
    public void setRootWithEvent(ActionEvent event, String fxmlFile) throws ViewLoadException {
        try {
            setRoot((Node) event.getSource(), fxmlFile, null);
        } catch (NullPointerException e) {
            System.err.println("ERROR: El evento no contiene una fuente válida");
            e.printStackTrace();
            throw new ViewLoadException("El evento no contiene una fuente válida", e);
        } catch (ViewLoadException e) {
            System.err.println("ERROR: Fallo al cargar la vista: " + fxmlFile);
            e.printStackTrace();
            throw new ViewLoadException("Error al cargar la vista: " + fxmlFile, e);
        }
    }

    /**
     * Cambia la raíz de la escena y opcionalmente el título de la ventana.
     *
     * @param node        Nodo de referencia para obtener la escena
     * @param fxmlFile    Ruta del archio FXML a cargar
     * @param windowTitle Título opcional para la ventana
     * @throws ViewLoadException Si ocurre algún error al cargar la vista
     */
    public void setRoot(Node node, String fxmlFile, String windowTitle) throws ViewLoadException {
        Objects.requireNonNull(node, "El nodo de referencia no puede ser nulo");
        Objects.requireNonNull(fxmlFile, "La ruta del FXML no puede ser nula");

        try {
            Parent root = springFXMLLoader.load(fxmlFile);
            Scene scene = node.getScene();

            if (scene == null) {
                System.err.println("ERROR: El nodo no está asociado a ninguna escena");
                throw new ViewLoadException("El nodo no está asociado a ninguna escena");
            }

            scene.setRoot(root);

            if (windowTitle != null) {
                Stage stage = (Stage) scene.getWindow();
                if (stage != null) {
                    stage.setTitle(windowTitle);
                }
            }
        } catch (IOException e) {
            System.err.println("ERROR: No se pudo cargar el archivo FXML: " + fxmlFile);
            e.printStackTrace();
            throw new ViewLoadException("Error al cargar el archivo FXML: " + fxmlFile, e);
        } catch (IllegalStateException e) {
            System.err.println("ERROR: Problema al acceder a la ventana principal");
            e.printStackTrace();
            throw new ViewLoadException("Error al acceder a la ventana principal", e);
        }
    }

    /**
     * Cambia la vista mostrando un mensaje de error al usuario si falla
     *
     * @param actionEvent Evento que disparó el cambio
     * @param fxmlPath    Ruta relativa del archivo FXML (ej. "/views/sellView.fxml")
     */
    public void switchView(ActionEvent actionEvent, String fxmlPath) {
        try {
            setRootWithEvent(actionEvent, fxmlPath);
        } catch (ViewLoadException e) {
            String errorMessage = String.format(
                    "No se pudo cargar la vista %s. Razón: %s",
                    fxmlPath,
                    getRootCauseMessage(e)
            );

            System.err.println("ERROR: " + errorMessage);
            e.printStackTrace();

            displayAlerts.showAlert(
                    "Error al cambiar pantalla" +
                            "\nNo se pudo cargar la pantalla solicitada\n" +
                            errorMessage
            );
        }
    }

    /**
     * Obtiene el mensaje de la causa raíz de la excepción
     */
    private String getRootCauseMessage(Throwable e) {
        Throwable rootCause = e;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        return rootCause.getMessage();
    }

    /**
     * Muestra una nueva ventana con título, logo y contenido especificados.
     *
     * @param title    Título de la ventana
     * @param logoPath Ruta del logo (puede ser null)
     * @param fxmlPath Ruta del archivo FXML
     * @throws WindowLoadException Si no se puede cargar la ventana
     */
    public void displayWindow(String title, String logoPath, String fxmlPath) throws WindowLoadException {
        Objects.requireNonNull(title, "El título no puede ser nulo");
        Objects.requireNonNull(fxmlPath, "La ruta FXML no puede ser nula");

        ConfigurableApplicationContext context = Main.getContext();
        try {
            Stage stage = createStage(
                    context.getBean(SpringFXMLLoader.class).load(fxmlPath),
                    title,
                    logoPath
            );

            stage.setOnCloseRequest(event -> {
                // Lógica para manejar el cierre si es necesario
            });

            stage.show();
        } catch (IOException e) {
            System.err.println("ERROR: No se pudo cargar el archivo FXML: " + fxmlPath);
            e.printStackTrace();
            throw new WindowLoadException("Error al cargar el archivo FXML: " + fxmlPath, e);
        } catch (BeansException e) {
            System.err.println("ERROR: Fallo al obtener el SpringFXMLLoader del contexto");
            e.printStackTrace();
            throw new WindowLoadException("Error al obtener el SpringFXMLLoader del contexto", e);
        } catch (IllegalStateException e) {
            System.err.println("ERROR: Problema al configurar la ventana");
            e.printStackTrace();
            throw new WindowLoadException("Error al configurar la ventana", e);
        }
    }

    /**
     * Utility method to create and configure a new stage.
     */
    private Stage createStage(Parent root, String title, String iconPath) {
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle(title);

        if (iconPath != null) {
            try {
                stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource(iconPath)).toString()));
            } catch (Exception e) {
                System.err.println("ADVERTENCIA: No se pudo cargar el icono: " + iconPath);
                e.printStackTrace();
            }
        }

        stage.setResizable(false);
        stage.centerOnScreen();
        return stage;
    }

    // Clases de excepción personalizadas
    public static class ViewLoadException extends Exception {
        public ViewLoadException(String message) {
            super(message);
        }

        public ViewLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class WindowLoadException extends Exception {
        public WindowLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public void closeWindow(TextField tf){
        Stage stage = (Stage) tf.getScene().getWindow();
        stage.close();
    }

    /**
     * Switch to configuration.
     */
    public void switchToConfiguration(ActionEvent actionEvent) {
        switchView(actionEvent, "/views/configurationView.fxml");
    }

    public void switchToBuy(ActionEvent actionEvent) {
        switchView(actionEvent, "/views/buyView.fxml");
    }

    /**
     * Switch to support.
     */
    public void switchToSupport(ActionEvent actionEvent) {
        switchView(actionEvent, "/views/supportView.fxml");
    }

    /**
     * Switch to registry.
     */
    public void switchToRegistry(ActionEvent actionEvent) {
        switchView(actionEvent, "/views/logsView.fxml");
    }

    /**
     * Switch to warehouse.
     */
    public void switchToWarehouse(ActionEvent actionEvent) {
        switchView(actionEvent, "/views/warehouseView.fxml");
    }

    /**
     * Switch to balance.
     */
    public void switchToBalance(ActionEvent actionEvent) {
        switchView(actionEvent, "/views/balanceView.fxml");
    }

    /**
     * Switch to sell.
     */
    public void switchToSell(ActionEvent actionEvent) {
        switchView(actionEvent, "/views/sellView.fxml");
    }

    public void switchToExpense(ActionEvent actionEvent) {
        switchView(actionEvent, "/views/expenseView.fxml");
    }
}