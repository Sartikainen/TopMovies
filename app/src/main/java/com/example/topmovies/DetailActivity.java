package com.example.topmovies;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

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

    private int id;

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
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("id")) {
            id = intent.getIntExtra("id", -1);
        } else {
            finish();
        }
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        Movie movie = viewModel.getMovieById(id);
        Picasso.get().load(movie.getBigPosterPath()).into(ivBigPoster);
        tvTitle.setText(movie.getTitle());
        tvOriginalTitle.setText(movie.getOriginalTitle());
        tvRating.setText(Double.toString(movie.getVoteAverage()));
        tvRelease.setText(movie.getReleaseDate());
        tvOverview.setText(movie.getOverview());
    }
}