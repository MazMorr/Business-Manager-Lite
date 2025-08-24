package com.marcosoft.storageSoftware.infrastructure.util;

import javafx.scene.control.TextField;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Lazy
@Component
public class CleanHelper {
    public void cleanTextFields(List<TextField> textFields){
        for(TextField tf: textFields){
            if (tf != null) {
                tf.clear();
            }
        }
    }
}
