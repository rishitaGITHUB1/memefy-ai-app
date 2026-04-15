package com.example.memefy;

public class Meme {
    private int imageResource;
    private String caption;

    public Meme(int imageResource, String caption) {
        this.imageResource = imageResource;
        this.caption = caption;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
}