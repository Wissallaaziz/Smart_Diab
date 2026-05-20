package com.example.smartdiab;

import android.net.Uri;

public class Message {
    private String text;
    private boolean isUser;
    private Uri imageUri;

    public Message(String text, boolean isUser) {
        this.text = text;
        this.isUser = isUser;
    }

    public Message(Uri imageUri, boolean isUser) {
        this.imageUri = imageUri;
        this.isUser = isUser;
    }

    public String getText() { return text; }
    public boolean isUser() { return isUser; }
    public Uri getImageUri() { return imageUri; }
    public boolean isImage() { return imageUri != null; }
}