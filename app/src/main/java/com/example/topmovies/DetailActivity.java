package com.example.topmovies;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.topmovies.data.FavouriteMovie;
import com.example.topmovies.data.MainViewModel;
import com.example.topmovies.data.Movie;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    private ImageView ivBigPoster;
    private TextView tvTitle;
    private TextView tvOriginalTitle;
    private TextView tvRating;
    private TextView tvRelease;
    private TextView tvOverview;
    private ImageView ivAddToFavourite;

    private int id;
    private Movie movie;
    private FavouriteMovie favouriteMovie;

    private MainViewModel viewModel;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ivBigPoster = findViewById(R.id.ivBigPoster);
        tvTitle = findViewById(R.id.tvTitle);
        tvOriginalTitle = findViewById(R.id.tvOriginalTitle);
        tvRating = findViewById(R.id.tvRating);
        tvRelease = findViewById(R.id.tvRelease);
        tvOverview = findViewById(R.id.tvOverview);
        ivAddToFavourite = findViewById(R.id.ivAddToFavourite);
        ivAddToFavourite.setOnClickListener(this::onClickChangeFavourite);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("id")) {
            id = intent.getIntExtra("id", -1);
        } else {
            finish();
        }
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        movie = viewModel.getMovieById(id);
        Picasso.get().load(movie.getBigPosterPath()).into(ivBigPoster);
        tvTitle.setText(movie.getTitle());
        tvOriginalTitle.setText(movie.getOriginalTitle());
        tvRating.setText(Double.toString(movie.getVoteAverage()));
        tvRelease.setText(movie.getReleaseDate());
        tvOverview.setText(movie.getOverview());
        setFavourite();
    }

    private void onClickChangeFavourite(View view) {
        if (favouriteMovie == null) {
            viewModel.insertFavouriteMovie(new FavouriteMovie(movie));
            Toast.makeText(this, R.string.add_to_favourite, Toast.LENGTH_SHORT).show();
        } else {
            viewModel.deleteFavouriteMovie(favouriteMovie);
            Toast.makeText(this, R.string.delete_from_favourite, Toast.LENGTH_SHORT).show();
        }
        setFavourite();
    }

    private void setFavourite() {
        favouriteMovie = viewModel.getFavouriteMovieById(id);
        if (favouriteMovie == null) {
            ivAddToFavourite.setImageResource(R.drawable.baseline_star_empty_24);
        } else {
            ivAddToFavourite.setImageResource(R.drawable.baseline_star_24);
        }
    }
}