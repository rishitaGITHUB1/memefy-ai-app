package com.example.memefy;

public class Feature {
    private String emoji;
    private String title;
    private String description;

    public Feature(String emoji, String title, String description) {
        this.emoji = emoji;
        this.title = title;
        this.description = description;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}