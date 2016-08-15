package com.example.android.bookcat.bookcat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Downloads an image to be shown in an {@link ImageView}
 */
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

    private static final String LOG_TAG = "DownloadImageTask";
    /**
     * A reference to the {@link ImageView} to be filled
     */
    private WeakReference<ImageView> mImageViewRef;

    /**
     * Public constructor for the downloader
     *
     * @param imageViewRef a reference to the {@link ImageView} to be filled
     */
    public DownloadImageTask(WeakReference<ImageView> imageViewRef) {
        mImageViewRef = imageViewRef;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        URL imageUrl = makeURL(strings[0]);
        Bitmap image;
        try {
            image = downloadImage(imageUrl);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error while downloading image", e);
            return null;
        }
        return image;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        if (mImageViewRef != null) {
            ImageView imgView = mImageViewRef.get();
            if (imgView != null) {
                if (bitmap != null) {
                    imgView.setImageBitmap(bitmap);
                } else {
                    // If no image is available use the application image
                    imgView.setImageDrawable(imgView.getContext().getResources()
                            .getDrawable(R.mipmap.book_cat_launcher));
                }
            }
        }
    }

    /**
     * Creates the URL to used for the download
     *
     * @param urlString a {@link String} containing the URL to be used
     * @return a {@link URL} object that will be used to download the image
     */
    private URL makeURL(String urlString) {
        URL outputUrl;

        if (urlString == null) {
            return null;
        } else {
            try {
                outputUrl = new URL(urlString);
            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "Error while creating URL", e);
                return null;
            }
        }

        return outputUrl;
    }

    /**
     * Downloads the image from the given URL
     *
     * @param url the {@link URL} from where the image will be downloaded
     * @return a {@link Bitmap} image
     * @throws IOException
     */
    private Bitmap downloadImage(URL url) throws IOException {
        Bitmap bitmapOutput;
        // If no available URL return early
        if (url == null) {
            return null;
        }

        HttpURLConnection httpConnection = null;
        InputStream inputStream = null;

        try {
            // Set up the connection
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("GET");
            httpConnection.setReadTimeout(10000);
            httpConnection.setConnectTimeout(15000);
            httpConnection.connect();

            if (httpConnection.getResponseCode() == 200) {
                // If all OK get the image
                inputStream = httpConnection.getInputStream();
                bitmapOutput = BitmapFactory.decodeStream(inputStream);
            } else {
                // In case of a non-normal response print a message in the log and return early
                Log.e(LOG_TAG, "Error with HTTP Connection. Error Code "
                        + httpConnection.getResponseCode());
                return null;
            }
        } catch (IOException e) {
            // In case of an exception print a message in the log and return early
            Log.e(LOG_TAG, "Error while downloading image", e);
            return null;
        } finally {
            // Close the connection and the stream
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        // Return image
        return bitmapOutput;
    }
}
