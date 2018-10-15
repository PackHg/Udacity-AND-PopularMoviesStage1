/*
 * Copyright (c) 2018 Pack Heng
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.packheng.popularmoviesstage1.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Helper methods to check network connectivity and fetch data from a remote server.
 */
public class NetworkAndRemoteDataUtils {
    private static final String LOG_TAG = NetworkAndRemoteDataUtils.class.getSimpleName();

    private NetworkAndRemoteDataUtils() {}

    /**
     * Checks the network connectivity.
     *
     * @param context the Activity context.
     * @return true if there's a network connection.
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Loads an image from a given URL and turn it into a Bitmap.
     * Returns null if there's issue in getting the {@link Bitmap} or the srcUrl is empty.
     *
     * @param srcUrl Image source Url (a String).
     * @return Image Bitmap.
     */
    public static Bitmap getBitmapFromURL(String srcUrl) {

        if (srcUrl.isEmpty()) {
            return null;
        }

        HttpURLConnection connection = null;
        InputStream input = null;

        try {
            URL url = new URL(srcUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            input = connection.getInputStream();
            // Use Android’s BitmapFactory class to decode the input stream
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem in getting a Bitmap from the given URL string.", e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Problem in closing the inputStream.", e);
                }
            }
        }
    }

    /**
     * Creates an {@link URL} object from the the given URL string.
     *
     * @param urlString String containing the URL.
     * @return an URL object.
     */
    public static URL createUrl(String urlString) {
        URL url = null;

        try {
            url = new URL(urlString);
        }
        catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating url", e);
        }

        return url;
    }

    /**
     * Makes an HTTP request to the given URL and returns a String as the response.
     *
     * @param url an URL.
     * @return String containing the JSON response.
     * @throws IOException when making a Http connection.
     */
    public static String makeHttpRequest(URL url) throws IOException {

        final int READ_TIMEOUT = 2000; /* milliseconds */
        final int CONNECT_TIMEOUT = 3000; /* milliseconds */
        final String GET = "GET";

        String jsonResponse = "";

        // if the url argument is null then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;

        try {
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(READ_TIMEOUT);
            httpURLConnection.setConnectTimeout(CONNECT_TIMEOUT);
            httpURLConnection.setRequestMethod(GET);
            httpURLConnection.connect();

            // If the request is successful (response code HttpURLConnection.HTTP_OK)
            // then read the input stream and parse the response
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = httpURLConnection.getInputStream();
                jsonResponse = readFromInputStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + httpURLConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem in retrieving the JSON result from the url.", e);
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }

        return jsonResponse;
    }

    /**
     * Converts the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }
}
