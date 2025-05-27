package model;

public class Quote {
    private String content;
    private String author;

    public Quote(String content, String author) {
        this.content = content;
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }
}
