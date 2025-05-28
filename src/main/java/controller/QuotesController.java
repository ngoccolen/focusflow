package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.Quote;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class QuotesController {

    @FXML private Label quoteContent, quoteAuthor, quoteDate;
    @FXML private AnchorPane mainContainer;
    
    private Stage stage;
    private double xOffset = 0;
    private double yOffset = 0;
    private final Random random = new Random();

    private final List<Quote> quotes = Arrays.asList(
        new Quote("Believe you can and you're halfway there.", "Theodore Roosevelt"),
        new Quote("The only way to do great work is to love what you do.", "Steve Jobs"),
        new Quote("Life is what happens when you're busy making other plans.", "John Lennon"),
        new Quote("Success is not final, failure is not fatal: It is the courage to continue that counts.", "Winston Churchill"),
        new Quote("The future depends on what you do today.", "Mahatma Gandhi"),
        new Quote("Dream big and dare to fail.", "Norman Vaughan")
    );

    public void setStage(Stage stage) {
        this.stage = stage;
        setupDragHandlers();
    }

    private void setupDragHandlers() {
        // Cho phép kéo từ bất kỳ đâu trên mainContainer
        mainContainer.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
            event.consume(); 
        });
        
        mainContainer.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
            event.consume();
        });
    }

    @FXML
    public void initialize() {
        mainContainer.setPickOnBounds(true); // Bắt sự kiện trên toàn bộ pane
        mainContainer.setMouseTransparent(false); // Đảm bảo pane không trong suốt với chuột
        showRandomQuote();
    }

    @FXML
    public void showRandomQuote() {
        Quote quote = quotes.get(random.nextInt(quotes.size()));
        quoteContent.setText(quote.getContent());
        quoteAuthor.setText("- " + quote.getAuthor());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        quoteDate.setText(sdf.format(new Date()));
    }

    @FXML
    private void handleCloseQuotes() {
        if (stage != null) {
            stage.close();
        }
    }
}