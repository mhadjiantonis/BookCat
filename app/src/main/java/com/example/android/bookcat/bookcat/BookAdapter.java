package com.example.android.bookcat.bookcat;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * An {@link ArrayAdapter} that generates custom {@link View} objects from the contents of an
 * {@link ArrayList} of {@link Book} objects.
 */
public class BookAdapter extends ArrayAdapter<Book>{

    /**
     * Public constructor for the class
     * @param context the {@link Context} of the activity that creates the adapter.
     * @param bookList the {@link ArrayList} of {@link Book} objects that will be displayed.
     */
    public BookAdapter(Context context, ArrayList<Book> bookList) {
        super(context, 0, bookList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View availView = convertView;
        // If the current view is null inflate a new one from the layout.
        if (availView == null) {
            availView = LayoutInflater.from(getContext()).inflate(R.layout.book_view, parent,
                    false);
        }
        // Get current book that will be displayed.
        Book currentBook = this.getItem(position);
        // Find the different views inside the layout.
        TextView titleText = (TextView) availView.findViewById(R.id.title_text_view);
        TextView authorsText = (TextView) availView.findViewById(R.id.authors_text_view);
        TextView descriptionText = (TextView) availView.findViewById(R.id.description_text_view);
        ImageView thumbImg = (ImageView) availView.findViewById(R.id.book_image_view);
        // Set the title text
        titleText.setText(currentBook.getTitle());
        // Set the authors' text
        authorsText.setText(formatAuthorList(currentBook.getAuthors()));
        // Set the description text if available.
        if (currentBook.getDescription().isEmpty()) {
            descriptionText.setText(R.string.no_description);
        } else {
            descriptionText.setText(Html.fromHtml(currentBook.getDescription()));
        }
        // Set the image source to be used.
        thumbImg.setImageResource(R.mipmap.book_cat_launcher);

        return availView;
    }

    /**
     * Formats an {@link ArrayList} of {@link String} objects as one string.
     * The different elements are separated by commas.
     * @param authorsListArr is the input {@link ArrayList} of {@link String} objects.
     * @return the formatted String
     */
    private String formatAuthorList(ArrayList<String> authorsListArr) {
        String authList = authorsListArr.toString();
        authList = authList.substring(1,authList.length() - 1);
        return authList;
    }
}
