package ui.gui.controllers;

import domain.Book;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.List;

public class BookCardController {

    @FXML private Label titleLabel;
    @FXML private Label authorLabel;
    @FXML private Label ratingLabel;
    @FXML private Label tagsLabel;
    @FXML private ImageView coverImage;

    // We keep a default image if the URL fails
    private static final String DEFAULT_IMG = "https://via.placeholder.com/150x200?text=No+Cover";

    public void setData(Book book) {
        titleLabel.setText(book.getTitle());
        authorLabel.setText(book.getAuthor() != null ? book.getAuthor() : "Unknown");
        ratingLabel.setText(String.format("%.1f ★", book.getRating()));

        // Handle Tags (List to String)
        List<String> tags = List.of(book.getTag());
        if (!tags.isEmpty()) {
            tagsLabel.setText("tags: " + String.join(", ", tags));
        } else {
            tagsLabel.setText("");
        }

        // Load Image Async
        String url = (book.getCoverUrl() != null && !book.getCoverUrl().isEmpty())
                ? book.getCoverUrl() : DEFAULT_IMG;

        try {
            // 'true' in the constructor means "Load in background"
            Image image = new Image(url, true);
            coverImage.setImage(image);
        } catch (Exception e) {
            // Fallback silently
        }
    }
}