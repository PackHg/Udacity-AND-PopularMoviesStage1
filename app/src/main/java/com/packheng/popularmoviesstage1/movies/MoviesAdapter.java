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

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.packheng.popularmoviesstage1.DetailActivity;
import com.packheng.popularmoviesstage1.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Provides binding from a data set to views that are displayed within a RecyclerView.
 */
public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MovieViewHolder> {
    private static final String LOG_TAG = MovieViewHolder.class.getSimpleName();

    public static final String MOVIE_POSITION_KEY = "movie_position_key";
    private final Context context;
    private List<Movie> movies;

    public MoviesAdapter(Context context, List<Movie> movies) {
        this.context = context;
        this.movies = movies;
    }

    class MovieViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.movie_item_iv) ImageView movieImageView;

        public MovieViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            movieImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Intent intent = new Intent(context, DetailActivity.class);
                    intent.putExtra(MOVIE_POSITION_KEY, position);
                    context.startActivity(intent);
                }
            });
        }
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(LOG_TAG, "TAG: Start of onCreateViewHolder()");

        View view = LayoutInflater.from(context).inflate(R.layout.movie_item, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Log.d(LOG_TAG, "TAG: Start of onBindViewHolder()");

        Movie movie = movies.get(position);
        String posterUrl = movie.getPosterUrl();
        Picasso.with(context).load(posterUrl).into(holder.movieImageView);
    }

    @Override
    public int getItemCount() {
        Log.d(LOG_TAG, "TAG: getItemCount()");

        return movies.size();
    }
}
