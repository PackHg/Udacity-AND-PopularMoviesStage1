package com.packheng.popularmoviesstage1;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
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
    @BindView(R.id.loading_spinner) ProgressBar loadingSpinner;
    @BindView(R.id.empty_tv) TextView emptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Number of columns in the RecyclerView
        final int NUMBER_OF_COLUMNS = 2;

        movies = new ArrayList<Movie>();

        moviesRecyclerView.setVisibility(View.VISIBLE);
        loadingSpinner.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.GONE);

        // Sets up the RecyclerView.
        moviesRecyclerView.setHasFixedSize(true);
        moviesAdapter = new MoviesAdapter(this, movies);
        moviesRecyclerView.setAdapter(moviesAdapter);
        moviesRecyclerView.setLayoutManager(new GridLayoutManager(this, NUMBER_OF_COLUMNS));

        loadMoviesData();
    }

    /**
     * Loads movies data by starting a {@link MoviesLoader}.
     */
    private void loadMoviesData() {
        if (isNetworkConnected(this)) {
            loadingSpinner.setVisibility(View.VISIBLE);
            moviesRecyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.GONE);
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.restartLoader(MOVIES_LOADER_ID, null, this);
        } else {
            loadingSpinner.setVisibility(View.GONE);
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
        // Base URL for querying TheMovieDb.org API.
        final String BASE_URL = "https://api.themoviedb.org/3/discover/movie?";
        final String API_KEY = BuildConfig.ApiKey;
        final String POPULARITY_DESC = "popularity.desc";
        final String VOTE_AVERAGE_DESC = "vote_average.desc";

        // Gets the sort by type from SharedPreferences
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String sortByPref = sp.getString(getString(R.string.pref_sort_by_key),
                getString(R.string.pref_sort_by_most_popular));

        String sortBy;
        if (sortByPref.equals(getString(R.string.pref_sort_by_top_rated))) {
            sortBy = VOTE_AVERAGE_DESC;
        } else {
            sortBy = POPULARITY_DESC;
        }

        Uri baseUri = Uri.parse(BASE_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter("api_key", API_KEY);
        uriBuilder.appendQueryParameter("sort_by", sortBy);

        return new MoviesLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> data) {
        loadingSpinner.setVisibility(View.GONE);

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
}
