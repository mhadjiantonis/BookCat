/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.bookcat.bookcat;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "Book Cat";

    private static final String BOOK_SEARCH_URL = "https://www.googleapis.com/books/v1/volumes?q=";

    String mJsonResponse = null;
    ArrayList<Book> mBookList = null;
    BookAdapter mAdapter = null;
    ListView mBookListView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate layout for main activity
        setContentView(R.layout.activity_main);

        // Hide the virtual keyboard
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Locate the ListView in the main layout.
        mBookListView = (ListView) findViewById(R.id.book_list_view);

        // Restore previous saved state if it exists.
        // The search results are not lost when the orientation of the device is changed.
        if (savedInstanceState != null) {
            mJsonResponse = savedInstanceState.getString("JSON Response");
        }
        if (mJsonResponse != null) {
            mBookList = extractBookList();
        }
        if (mBookList != null) {
            mAdapter = new BookAdapter(getApplicationContext(), mBookList);
        }

        // Attach the BookAdapter to the list
        if (mAdapter != null) {
            mBookListView.setAdapter(mAdapter);
        }

        // Set an OnItemClickListener for the list.
        // When an item is clicked an a website describing the book is opened.
        mBookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String destinationUrl = mAdapter.getItem(i).getPreviewUrl();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(destinationUrl));
                startActivity(browserIntent);
            }
        });

        // Set an OnClickListener for the search button.
        // It generates the search URL and if all OK, it hides the virtual keyboard and executes an
        // AsyncTask that downloads Book information.
        Button searchButton = (Button) findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Generate search URL.
                URL searchUrl = makeSearchUrl();
                if (searchUrl != null) {
                    // Hide the virtual keyboard.
                    InputMethodManager imm = (InputMethodManager)
                            getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(
                            (getCurrentFocus() != null) ? getCurrentFocus().getWindowToken() : null,
                            InputMethodManager.HIDE_NOT_ALWAYS);
                    // Execute an AsyncTask to get book info.
                    QueryAsyncTask queryTask = new QueryAsyncTask();
                    queryTask.execute(searchUrl);
                }
            }
        });

        // Set an onEditorActionListener for the search query EditText.
        // It generates the search URL and if all OK, it hides the virtual keyboard and executes an
        // AsyncTask that downloads Book information.
        TextInputEditText searchText = (TextInputEditText) findViewById(R.id.search_text);
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean ret = false;
                // If the search button is pressed
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    // Generate URL
                    URL searchUrl = makeSearchUrl();
                    if (searchUrl != null) {
                        // Hide the virtual keyboard
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow((getCurrentFocus() != null) ? getCurrentFocus().getWindowToken() : null, InputMethodManager.HIDE_NOT_ALWAYS);
                        // Execute an AsyncTask to get book info.
                        QueryAsyncTask queryTask = new QueryAsyncTask();
                        queryTask.execute(searchUrl);
                    }
                    ret =  true;
                }
                return ret;
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Include the last JSON response in the saved state.
        outState.putString("JSON Response", mJsonResponse);
        super.onSaveInstanceState(outState);
    }

    /**
     * Generates a {@link URL} based on the text entered in the search {@link TextInputEditText}.
     * @return the {@link URL} that is to be used to get book information.
     */
    URL makeSearchUrl() {
        //Get the text given by the user.
        TextInputEditText searchEditText = (TextInputEditText) findViewById(R.id.search_text);
        String queryText = searchEditText.getText().toString();
        URL url;
        // If no text is given, inform the user and return early.
        if (TextUtils.isEmpty(queryText.trim())) {
            Toast.makeText(this, getString(R.string.enter_valid_query), Toast.LENGTH_SHORT).show();
            return null;
        } else {
            // Replace spaces in the search query with %20 to prevent conversion errors.
            String[] partsOfQueryText = queryText.split(" ");
            queryText = "";
            for (String partOfQueryText : partsOfQueryText) {
                queryText += partOfQueryText + "%20";
            }
            // Create the URL. In case of an exception print a message in the log and return early.
            try {
                url = new URL(BOOK_SEARCH_URL + queryText);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error while making URL", exception);
                return null;
            }
        }
        // If all Ok return the generated URL.
        return url;
    }

    /**
     * Extracts an {@link ArrayList} of {@link Book} objects from the {@link JSONObject} generated
     * by the {@link QueryAsyncTask} that will be used to populate the {@link ListView} in the
     * {@link MainActivity}
     * @return an {@link ArrayList} of {@link Book} objects
     */
    @Nullable
    private ArrayList<Book> extractBookList() {
        ArrayList<Book> bookList = new ArrayList<>();
        try {
            // Create a JSONObject from the previously obtained response.
            JSONObject jsonResponse = new JSONObject(mJsonResponse);
            // Create a JSONArray from the that includes all the books.
            JSONArray bookListJSON = jsonResponse.getJSONArray("items");
            // For each element of the array get the details needed.
            for (int i = 0; i < bookListJSON.length(); i++) {
                JSONObject currentBookJSON = bookListJSON.getJSONObject(i);
                JSONObject volumeInfo = currentBookJSON.getJSONObject("volumeInfo");
                // Get the title of the book
                String title = volumeInfo.getString("title");
                // Get the array of authors.
                JSONArray authorListJSON = volumeInfo.optJSONArray("authors");
                ArrayList<String> authorList = new ArrayList<>();
                if (authorListJSON != null) {
                    for (int j = 0; j < authorListJSON.length(); j++) {
                        String author = authorListJSON.getString(j);
                        authorList.add(author);
                    }
                }
                // Get the preview URL for the book.
                String previewUrl = volumeInfo.getString("previewLink");
                // Get the thumbnail image's URL.
                JSONObject imageLinks = volumeInfo.optJSONObject("imageLinks");
                String imageUrl;
                if (imageLinks != null) {
                    imageUrl = imageLinks.optString("smallThumbnail");
                } else {
                    imageUrl = null;
                }
                //Get the description for the book
                JSONObject searchInfo = currentBookJSON.optJSONObject("searchInfo");
                String description;
                if (searchInfo != null) {
                    description = searchInfo.optString("textSnippet");
                } else {
                    description = volumeInfo.optString("description");
                }
                // Add the book in the return ArrayList.
                bookList.add(new Book(title, authorList, description, previewUrl, imageUrl));
            }
        } catch (JSONException e) {
            // In case of an exception print a message in the log and return early.
            Log.e(LOG_TAG, "Error while handling JSON", e);
            return null;
        }
        // Return the array of books.
        return bookList;
    }

    /**
     * An {@link AsyncTask} that gets a {@link URL} form which it downloads information and
     * gives an {@link ArrayList} of {@link Book} objects.
     */
    private class QueryAsyncTask extends AsyncTask<URL, Void, ArrayList<Book>> {

        @Override
        protected ArrayList<Book> doInBackground(URL... urls) {

            String jsonResponse = "";

            try {
                jsonResponse = makeHttpRequestForJson(urls[0]);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error while making HTTP request", e);
            }

            mJsonResponse = jsonResponse;

            return extractBookList();
        }

        /**
         * Open an {@link HttpURLConnection} from where it downloads a JSON response that
         * includes information about book
         * @param url to be used for the connection
         * @return a {@link String} object with JSON data
         * @throws IOException
         */
        private String makeHttpRequestForJson(URL url) throws IOException {
            String jsonResponse = "";

            if (url == null) {
                return jsonResponse;
            }

            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;

            try {
                // Set up the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.connect();
                // If the connection is successful read the response from the InputStream if the
                // connection.
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromInputStream(inputStream);
                } else {
                    // If an error code is returned, print a message in the log and return early.
                    Log.e(MainActivity.LOG_TAG, "HTTP error " +
                            urlConnection.getResponseCode());
                    return null;
                }
            } catch (IOException e) {
                // If an exception occurs print a message in the log and return early.
                Log.e(LOG_TAG, "Error while attempting HTTP connection", e);
                return null;
            } finally {
                //Close the connection
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            // Return the obtained response.
            return jsonResponse;
        }

        /**
         * Reads the lines of the given {@link InputStream}
         * @param inputStream the {@link InputStream} from where to read.
         * @return a {@link String} with the contents of the given {@link InputStream}
         * @throws IOException
         */
        @NonNull
        private String readFromInputStream(InputStream inputStream) throws IOException {

            StringBuilder output = new StringBuilder();

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line = bufferedReader.readLine();
                while (line != null) {
                    output.append(line);
                    line = bufferedReader.readLine();
                }
                bufferedReader.close();
            }
            return output.toString();
        }

        @Override
        protected void onPostExecute(ArrayList<Book> bookArrayList) {
            super.onPostExecute(bookArrayList);
            mBookList = bookArrayList;
            mAdapter = new BookAdapter(getApplicationContext(), mBookList);
            mBookListView.setAdapter(mAdapter);
        }
    }

}
