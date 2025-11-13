package ui.gui.controllers;

import domain.Book;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ui.gui.services.DevShelfService;

import java.io.IOException;

public class BookDetailController {

    @FXML private ImageView largeCoverImage;
    @FXML private Label fullTitle;
    @FXML private Label authors;
    @FXML private Label category;
    @FXML private Label rating;
    @FXML private Label progLang;
    @FXML private Text descriptionText;
    @FXML private Button readButton;

    private Book book;
    private Stage stage; // The stage this view is currently in
    private Scene previousScene; // To go back

    public void setBookData(Book book, Stage stage, Scene previousScene) {
        this.book = book;
        this.stage = stage;
        this.previousScene = previousScene;

        fullTitle.setText(book.getTitle());
        authors.setText(book.getAuthor());
        category.setText("Category: " + book.getCategory());
        rating.setText(book.getRating() + " ★");
        progLang.setText(book.getProgLang());
        descriptionText.setText(book.getDescription());

        // Load Image
        String url = (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty())
                ? book.getCoverUrl()
                : "https://via.placeholder.com/150x200?text=No+Cover";
        largeCoverImage.setImage(new Image(url, true));
    }

    @FXML
    private void handleBack() {
        // Go back to the search list
        if (stage != null && previousScene != null) {
            stage.setScene(previousScene);
        }
    }

    @FXML
    private void handleRead() {
        if (book.getDownLink() == null || book.getDownLink().isEmpty()) {
            System.out.println("No download link available.");
            return;
        }

        try {
            // Open the WebView Window
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/gui/fxml/WebViewWindow.fxml"));
            Parent root = loader.load();

            WebViewController controller = loader.getController();
            controller.loadUrl(book.getDownLink());

            Stage webStage = new Stage();
            webStage.setTitle("Reading: " + book.getTitle());
            webStage.setScene(new Scene(root));
            webStage.initModality(Modality.APPLICATION_MODAL); // Block other windows until closed
            webStage.setMaximized(true); // Open full screen for reading
            webStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}