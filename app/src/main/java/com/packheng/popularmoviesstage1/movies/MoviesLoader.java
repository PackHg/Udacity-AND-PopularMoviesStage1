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

package com.packheng.popularmoviesstage1.movies;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

import static com.packheng.popularmoviesstage1.movies.TMDBOrgUtils.fetchMoviesData;

/**
 * Loads a list of {@link Movie}.
 */
public class MoviesLoader extends AsyncTaskLoader<List<Movie>> {
    private static final String LOG_TAG = MoviesLoader.class.getSimpleName();

    // Query url
    private String url;

    /**
     * Constructs a new {@link MoviesLoader}
     * @param context Context of the activity.
     * @param url To load the data from.
     */
    public MoviesLoader(Context context, String url) {
        super(context);
        this.url = url;
    }

    /**
     * Is called in a background thread to fetch movies' data from TheMovieDb.org API.
     * @return List of {@link Movie}, null if url is an empty String.
     */
    @Override
    public List<Movie> loadInBackground() {
        if (url.isEmpty()) {
            return null;
        }

        return fetchMoviesData(url);
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading() {
        forceLoad();
    }
}
