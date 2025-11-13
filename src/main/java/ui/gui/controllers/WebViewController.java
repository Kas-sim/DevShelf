package ui.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class WebViewController {

    @FXML private WebView webView;

    public void loadUrl(String url) {
        WebEngine engine = webView.getEngine();
        System.out.println("🌐 Loading URL: " + url);
        engine.load(url);
    }

    @FXML
    private void handleClose() {
        // Get the stage from the webview and close it
        Stage stage = (Stage) webView.getScene().getWindow();
        stage.close();
    }
}