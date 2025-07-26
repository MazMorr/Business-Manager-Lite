package com.marcosoft.storageSoftware.application.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

@Lazy
@Controller
public class ChangeProductNameViewController {

    @Lazy
    public ChangeProductNameViewController(){

    }

    @FXML
    private TextField tfActualName;
    @FXML
    private TextField tfNewName;
    @FXML
    private MenuButton mbActualName;

    @FXML
    public void updateProductName(ActionEvent actionEvent) {
    }

    @FXML
    public void goOut(ActionEvent actionEvent) {
    }
}
