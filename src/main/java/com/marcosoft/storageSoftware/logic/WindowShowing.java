package com.marcosoft.storageSoftware.logic;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author MazMorr
 */
@Component
public class WindowShowing {

    private static WindowShowing instance;

    //ExistencyView booleans
    @Getter
    @Setter
    private boolean sellViewShowing;
    @Getter
    @Setter
    private boolean buyViewShowing;
    @Getter
    @Setter
    private boolean filterViewShowing;
    @Setter
    @Getter
    private boolean configurationShowing;
    @Getter
    @Setter
    private boolean registryFilterViewShowing;

    private final List<Stage> openStages = new ArrayList<>();

    public WindowShowing() {
    }

    public WindowShowing(boolean sellViewShowing, boolean buyViewShowing, boolean filterViewShowing, boolean configurationShowing, boolean registryFilterViewShowing) {
        this.sellViewShowing = sellViewShowing;
        this.buyViewShowing = buyViewShowing;
        this.filterViewShowing = filterViewShowing;
        this.configurationShowing = configurationShowing;
        this.registryFilterViewShowing = registryFilterViewShowing;
    }

    public static WindowShowing getInstance() {
        if (instance == null) {
            instance = new WindowShowing();
        }
        return instance;
    }

    public void displayAssistance(boolean viewShowing, String fxmlPath, String errorMessage, int auxView) throws IOException {
        if (!viewShowing) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = fxmlLoader.load();

            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.initStyle(StageStyle.DECORATED);
            stage.setResizable(false);
            stage.setScene(scene);
            stage.getIcons().add(new Image("file:resources/images/RTS_logo.png"));
            stage.show();

            // Configura el estado de la ventana según auxView
            switch (auxView) {
                case 0:
                    setSellViewShowing(true);
                    stage.setTitle("Ventas");
                    stage.setOnCloseRequest(eh -> setSellViewShowing(false));
                    break;
                case 1:
                    setBuyViewShowing(true);
                    stage.setTitle("Compras");
                    stage.setOnCloseRequest(eh -> setBuyViewShowing(false));
                    break;
                case 2:
                    setFilterViewShowing(true);
                    stage.setTitle("Filtro de Existencias");
                    stage.setOnCloseRequest(eh -> setFilterViewShowing(false));
                    break;
                case 3:
                    setConfigurationShowing(true);
                    stage.setTitle("Configuración");
                    stage.setOnCloseRequest(eh -> setConfigurationShowing(false));
                    break;
                case 4:
                    setRegistryFilterViewShowing(true);
                    stage.setTitle("Filtro de Registros");
                    stage.setOnCloseRequest(eh -> setRegistryFilterViewShowing(false));
                    break;
                default:
                    throw new IllegalArgumentException("Valor de auxView no válido: " + auxView);
            }
        } else {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Información");
            alert.setHeaderText(null);
            alert.setContentText(errorMessage);
            alert.showAndWait();
        }
    }

    public void closeAllWindows() {
        try {
            if (!openStages.isEmpty()) {
                for (Stage stage : openStages) {
                    stage.close();
                }
                openStages.clear();
            } else {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setContentText("openStages está vacía, hay un problema tanke");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
