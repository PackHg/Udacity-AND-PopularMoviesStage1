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

import android.util.Log;

import com.packheng.popularmoviesstage1.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.packheng.popularmoviesstage1.utils.NetworkAndRemoteDataUtils.createUrl;
import static com.packheng.popularmoviesstage1.utils.NetworkAndRemoteDataUtils.makeHttpRequest;

/**
 * Helper class with methods to fetch and parse data from TheMovieDb.org API.
 */
public class TMDBOrgUtils {
    private static final String LOG_TAG = TMDBOrgUtils.class.getSimpleName();

    private TMDBOrgUtils() {}

    /**
     * Queries TheMovieDb.org API and returns a {@link List<Movie>}.
     * Returns null if {@param urlString} is empty, there is an issue with making HTTP request or
     * extracting a {@link List<Movie>} from a JSON string.
     *
     * @param urlString String containing the URL.
     * @return {@link List<Movie>} or null.
     */
    public static List<Movie> fetchMoviesData(String urlString) {

        if (urlString.isEmpty()) {
            return null;
        }

        URL url = createUrl(urlString);

        // Performs HTTP request to the URL and receives a JSON response.
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem with making HTTP request.", e);
            return null;
        }

        //  Return the list of News extracted from the JSON response.
        return extractMoviesfromJsonString(jsonResponse);
    }

    /**
     * Returns an {@link ArrayList<Movie>} from a JSON string obtained from
     * TheMovieDb.org API.
     * Returns null if the {@param jsonString} is null or there is an issue with parsing
     * this string.
     *
     * @param jsonString a JSON string.
     * @return {@link ArrayList<Movie>} or null.
     */
    private static ArrayList<Movie> extractMoviesfromJsonString(String jsonString) {

        if (jsonString.isEmpty()) {
            return null;
        }

        // Keys for JSON parsing.
        final String RESULTS_KEY = "results";
        final String ORIGINAL_TITLE_KEY = "original_title";
        final String POSTER_PATH_KEY = "poster_path";
        final String OVERVIEW_KEY = "overview";
        final String VOTE_AVERAGE_KEY = "vote_average";
        final String RELEASE_DATE_KEY = "release_date";

        final String baseUrl = "https://image.tmdb.org/t/p";
        final String imageSize = "/w185";

        ArrayList<Movie> movies = new ArrayList<Movie>();

        try {
            JSONObject base = new JSONObject(jsonString);

            JSONArray results = base.optJSONArray(RESULTS_KEY);
            if (results == null) {
                return null;
            }

            for (int i = 0; i < results.length(); i++) {
                JSONObject resultsItem = results.optJSONObject(i);
                if (resultsItem != null) {
                    Movie movie = new Movie();

                    movie.setTitle(resultsItem.optString(ORIGINAL_TITLE_KEY));
                    movie.setOverview(resultsItem.optString(OVERVIEW_KEY));
                    movie.setUserRating(resultsItem.optDouble(VOTE_AVERAGE_KEY));
                    movie.setReleaseDate(resultsItem.optString(RELEASE_DATE_KEY));

                    // Builds the poster url string
                    String posterUrl = "";
                    String posterPath = resultsItem.optString(POSTER_PATH_KEY);
                    if (!posterPath.isEmpty()) {
                        posterUrl = baseUrl + imageSize + posterPath;
                    }
                    movie.setPosterUrl(posterUrl);
                    movies.add(movie);
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem in parsing the JSON string", e);
            return null;
        }

        return movies;
    }
}
