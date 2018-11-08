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

package com.packheng.popularmoviesstage1;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.packheng.popularmoviesstage1.movies.Movie;
import com.packheng.popularmoviesstage1.movies.MoviesAdapter;
import com.packheng.popularmoviesstage1.movies.MoviesLoader;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.packheng.popularmoviesstage1.utils.NetworkAndRemoteDataUtils.isNetworkConnected;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<Movie>> {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int MOVIES_LOADER_ID = 0;
    static ArrayList<Movie> movies;
    private MoviesAdapter moviesAdapter;

    @BindView(R.id.movies_rv) RecyclerView moviesRecyclerView;
    @BindView(R.id.empty_tv) TextView emptyTextView;
    @BindView(R.id.swipe_refrsh) SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Number of columns in the RecyclerView
        int numberOfColumns = calculateBestSpanCount((int) getResources()
                .getDimension(R.dimen.main_movie_poster_width));

        movies = new ArrayList<Movie>();

        moviesRecyclerView.setVisibility(View.VISIBLE);
        emptyTextView.setVisibility(View.GONE);

        // Sets up the RecyclerView.
        moviesRecyclerView.setHasFixedSize(true);
        moviesAdapter = new MoviesAdapter(this, movies);
        moviesRecyclerView.setAdapter(moviesAdapter);
        moviesRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));

        // Sets up a setOnRefreshListener to  when user performs a swipe-to-refresh gesture.
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadMoviesData();
            }
        });

        loadMoviesData();
    }

    /**
     * Loads movies data by starting a {@link MoviesLoader}.
     */
    private void loadMoviesData() {
        swipeRefreshLayout.setRefreshing(true);

        // TODO: to remove
        waitFor(2000);

        if (isNetworkConnected(this)) {
            moviesRecyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.GONE);
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.restartLoader(MOVIES_LOADER_ID, null, this);
        } else {
            swipeRefreshLayout.setRefreshing(false);
            moviesRecyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText(R.string.no_internet);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_item_refresh:
                movies.clear();
                moviesAdapter.notifyDataSetChanged();
                loadMoviesData();
                return true;

            case R.id.menu_item_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(int id, Bundle args) {
        // Base URL and end points for querying TheMovieDb.org API.
        final String BASE_URL = "https://api.themoviedb.org/3";
        final String MOST_POPULAR_ENDPOINT = "/movie/popular";
        final String TOP_RATED_ENDPOINT = "/movie/top_rated";

        final String API_KEY = "api_key";
        final String API_KEY_VALUE = BuildConfig.ApiKey;

        // Gets the sort by type from SharedPreferences
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String sortByPref = sp.getString(getString(R.string.pref_sort_by_key),
                getString(R.string.pref_sort_by_most_popular));

        String url;
        if (sortByPref.equals(getString(R.string.pref_sort_by_top_rated))) {
            url = BASE_URL + TOP_RATED_ENDPOINT;
        } else {
            url = BASE_URL + MOST_POPULAR_ENDPOINT;
        }

        Uri baseUri = Uri.parse(url);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter(API_KEY, API_KEY_VALUE);

        return new MoviesLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> data) {

        swipeRefreshLayout.setRefreshing(false);

        waitFor(1000);

        if (data != null && data.size() > 0) {
            emptyTextView.setVisibility(View.GONE);
            moviesRecyclerView.setVisibility(View.VISIBLE);
            movies.clear();
            movies.addAll(data);
            moviesAdapter.notifyDataSetChanged();
        } else {
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText(R.string.no_movies_data_found);
        }


    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {
        movies.clear();
        moviesAdapter.notifyDataSetChanged();
    }

    /**
     * Calculates best number of columns in the grid view depending of the poster width
     * and the screen width.
     *
     * @param posterWidth
     * @return
     */
    private int calculateBestSpanCount(int posterWidth) {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float screenWidth = outMetrics.widthPixels;
        return Math.round(screenWidth / posterWidth);
    }

    private void waitFor(int milliseconds) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // Actions to do after: nothing
            }
        }, milliseconds);
    }

}
