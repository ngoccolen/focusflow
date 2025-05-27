package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import model.Quote;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class QuotesController {

    @FXML private Label quoteContent;
    @FXML private Label quoteAuthor;
    @FXML private Label quoteDate;
    @FXML private AnchorPane quotesPane;

    private final List<Quote> quotes = Arrays.asList(
        new Quote("Believe you can and you're halfway there.", "Theodore Roosevelt"),
        new Quote("The only way to do great work is to love what you do.", "Steve Jobs"),
        new Quote("Life is what happens when you're busy making other plans.", "John Lennon"),
        new Quote("Success is not final, failure is not fatal: It is the courage to continue that counts.", "Winston Churchill"),
        new Quote("The future depends on what you do today.", "Mahatma Gandhi"),
        new Quote("Dream big and dare to fail.", "Norman Vaughan")
        // 👉 Bạn muốn thêm câu nào thì cứ thêm ở đây
    );

    private final Random random = new Random();

    @FXML
    public void initialize() {
        showRandomQuote();
    }

    @FXML
    public void showRandomQuote() {
        Quote quote = quotes.get(random.nextInt(quotes.size()));
        quoteContent.setText(quote.getContent());
        quoteAuthor.setText("- " + quote.getAuthor());

        // Hiển thị ngày hiện tại khi quote được hiển thị
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        quoteDate.setText(sdf.format(new Date()));
    }
    @FXML
    private void handleCloseQuotes() {
        quotesPane.setVisible(false);
    }

}
