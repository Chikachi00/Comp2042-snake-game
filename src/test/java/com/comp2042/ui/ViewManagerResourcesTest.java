package com.comp2042.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/** Loads the real menu and game with the bundled fonts, including paths containing spaces. */
class ViewManagerResourcesTest extends JavaFxTestSupport {
    @Test
    void menuAndGameKeepTheirPixelFontsWithoutFxmlVersionWarnings() throws Exception {
        runOnFxThread(() -> {
            List<String> fxmlMessages = new ArrayList<>();
            Logger logger = Logger.getLogger("");
            Handler handler = new Handler() {
                @Override public void publish(LogRecord record) { fxmlMessages.add(record.getMessage()); }
                @Override public void flush() { }
                @Override public void close() { }
            };
            logger.addHandler(handler);
            Stage menuStage = new Stage();
            Stage gameStage = null;
            try {
                new ViewManager(menuStage).initializeMainMenu();
                menuStage.getScene().getRoot().applyCss();
                menuStage.getScene().getRoot().layout();
                Label title = (Label) menuStage.getScene().lookup("#title");
                Button start = (Button) menuStage.getScene().lookup("#btn_start");
                assertAll("Original menu typography and runtime compatibility",
                        () -> assertEquals("SF Pixelate Shaded", title.getFont().getFamily()),
                        () -> assertEquals("Bold", title.getFont().getStyle()),
                        () -> assertEquals(40, title.getFont().getSize()),
                        () -> assertEquals("SF Pixelate", start.getFont().getFamily()),
                        () -> assertEquals(18, start.getFont().getSize()),
                        () -> assertTrue(fxmlMessages.stream().noneMatch(message ->
                                        message.contains("JavaFX API of version")),
                                "The FXML must load without a runtime version warning"));

                start.fire();
                gameStage = (Stage) Window.getWindows().stream()
                        .filter(window -> window != menuStage && window.isShowing())
                        .findFirst().orElseThrow();
                gameStage.getScene().getRoot().applyCss();
                Label score = (Label) gameStage.getScene().lookup("#scoreLabel");
                assertEquals("SF Pixelate", score.getFont().getFamily());
                assertEquals(20, score.getFont().getSize());
                assertEquals("Score: 0", score.getText());
            } finally {
                if (gameStage != null) gameStage.close();
                menuStage.close();
                logger.removeHandler(handler);
            }
        });
    }
}
