package ui.gui.controllers;

import domain.Book;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ui.gui.services.DevShelfService;
import ui.gui.services.DevShelfService.SearchResponse;
// IMPORTANT: Import your BookFilter from utils
import utils.BookFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainViewController {

    @FXML private TextField searchField;
    @FXML private VBox resultsContainer;
    @FXML private Label statusLabel;

    // New Filter Fields
    @FXML private TextField categoryFilter;
    @FXML private TextField authorFilter;

    private DevShelfService service;

    // We store the RAW search results here so we can filter them later
    private List<Book> allCurrentResults = new ArrayList<>();

    public void setService(DevShelfService service) {
        this.service = service;
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        if (query == null || query.trim().isEmpty()) return;

        statusLabel.setText("Searching...");

        // 1. Clear filters when doing a new search
        categoryFilter.clear();
        authorFilter.clear();

        // 2. Get fresh results from the Brain
        SearchResponse response = service.search(query);

        // 3. Save them to our local list
        this.allCurrentResults = response.books;

        if (this.allCurrentResults.isEmpty()) {
            statusLabel.setText("❌ No results found.");
            resultsContainer.getChildren().clear();
        } else {
            String msg = response.isSuggestion ?
                    "💡 Showing results for: " + response.successfulQuery :
                    "✅ Found " + allCurrentResults.size() + " books.";
            statusLabel.setText(msg);

            // 4. Display everything
            displayBooks(this.allCurrentResults);
        }
    }

    @FXML
    private void handleFilter() {
        if (allCurrentResults.isEmpty()) return;

        // Start with full list
        List<Book> filtered = new ArrayList<>(allCurrentResults);

        // Apply Category Filter
        String cat = categoryFilter.getText();
        if (cat != null && !cat.isEmpty()) {
            filtered = BookFilter.filterByCategory(filtered, cat);
        }

        // Apply Author Filter
        String auth = authorFilter.getText();
        if (auth != null && !auth.isEmpty()) {
            filtered = BookFilter.filterByAuthor(filtered, auth);
        }

        statusLabel.setText("🔍 Filtered: " + filtered.size() + " books shown.");
        displayBooks(filtered);
    }

    @FXML
    private void handleClearFilter() {
        categoryFilter.clear();
        authorFilter.clear();
        statusLabel.setText("✅ Filters cleared. Showing all " + allCurrentResults.size() + " results.");
        displayBooks(allCurrentResults);
    }


    private void displayBooks(List<Book> books) {
        resultsContainer.getChildren().clear();

        int limit = Math.min(books.size(), 50);

        for (int i = 0; i < limit; i++) {
            Book book = books.get(i);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/gui/fxml/BookCard.fxml"));
                Node card = loader.load();

                BookCardController cardController = loader.getController();
                cardController.setData(book);

                // --- NEW CLICK LOGIC ---
                card.setOnMouseClicked(e -> {
                    // 1. Log the click (Keep this)
                    service.logClick(searchField.getText(), book.getBookId());

                    // 2. Open Details View
                    openDetailsView(book);
                });
                // -----------------------

                resultsContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Add this new helper method
    private void openDetailsView(Book book) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/gui/fxml/BookDetailView.fxml"));
            Parent detailRoot = loader.load();

            BookDetailController controller = loader.getController();

            // Get current stage and scene to allow "Back" functionality
            Stage currentStage = (Stage) searchField.getScene().getWindow();
            Scene currentScene = searchField.getScene();

            // Pass data to the new controller
            controller.setBookData(book, currentStage, currentScene);

            // Switch Scene
            currentStage.setScene(new Scene(detailRoot));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}