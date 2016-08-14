package com.example.android.bookcat.bookcat;

import java.util.ArrayList;

/**
 * Stores information about a book
 */
public class Book {

    private String mTitle;
    private ArrayList<String> mAuthors;
    private String mDescription;
    private String mPreviewUrl;
    private String mImageUrl;

    /**
     * Public constructor for the class
     * @param title the title of the new book
     * @param authors a list of the authors of the book
     * @param description a description of the book
     * @param previewUrl a url to the preview website for the book
     * @param imageUrl a url to a thumbnail image for the book
     */
    public Book(String title, ArrayList<String> authors, String description, String previewUrl,
                String imageUrl) {
        this.setTitle(title);
        this.setAuthors(authors);
        this.setDescription(description);
        this.setPreviewUrl(previewUrl);
        this.setImageUrl(imageUrl);
    }

    /**
     * Sets the title of the book
     * @param title the new title
     */
    public void setTitle(String title) {
        this.mTitle = title;
    }

    /**
     * Sets the authors of the book
     * @param authors a list of the new authors
     */
    public void setAuthors(ArrayList<String> authors) {
        this.mAuthors = authors;
    }

    /**
     * Sets the description text for the book
     * @param description the new description
     */
    public void setDescription(String description) {
        this.mDescription = description;
    }

    /**
     * Sets the preview url of the book
     * @param previewUrl the new preview url
     */
    public void setPreviewUrl(String previewUrl) {
        this.mPreviewUrl = previewUrl;
    }

    /**
     * Sets the thumbnail url of the book
     * @param imageUrl the new thumbnail url
     */
    public void setImageUrl(String imageUrl) {
        this.mImageUrl = imageUrl;
    }

    /**
     * Returns the title of the book
     * @return the book's title
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Returns a list with the authors of the book
     * @return the list of authors
     */
    public ArrayList<String> getAuthors() {
        return mAuthors;
    }

    /**
     * Returns a description for the book
     * @return the description for the book
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * Returns the preview URL for the book
     * @return the preview URL
     */
    public String getPreviewUrl() {
        return mPreviewUrl;
    }

    /**
     * Returns the thumbnail URL for the book
     * @return the thumbnail URL
     */
    public String getImageUrl() {
        return mImageUrl;
    }

    @Override
    public String toString() {
        String bookString;
        bookString = "Title: " + this.getTitle() + "\n";
        bookString += "Authors: " + this.getAuthors() + "\n";
        bookString += "Description: " + this.getDescription() + "\n";
        bookString += "Preview URL: " + this.getPreviewUrl() + "\n";
        bookString += "Image URL: " + this.getImageUrl();
        return bookString;
    }
}
