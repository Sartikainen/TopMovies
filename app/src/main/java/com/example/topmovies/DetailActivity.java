package com.example.topmovies;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.topmovies.adapters.ReviewAdapter;
import com.example.topmovies.adapters.TrailerAdapter;
import com.example.topmovies.data.FavouriteMovie;
import com.example.topmovies.data.MainViewModel;
import com.example.topmovies.data.Movie;
import com.example.topmovies.data.Review;
import com.example.topmovies.data.Trailer;
import com.example.topmovies.utils.JSONUtils;
import com.example.topmovies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private ImageView ivBigPoster;
    private TextView tvTitle;
    private TextView tvOriginalTitle;
    private TextView tvRating;
    private TextView tvRelease;
    private TextView tvOverview;
    private ImageView ivAddToFavourite;
    private ScrollView scrollViewInfo;

    private RecyclerView rvTrailers;
    private RecyclerView rvReviews;
    private ReviewAdapter reviewAdapter;
    private TrailerAdapter trailerAdapter;

    private int id;
    private Movie movie;
    private FavouriteMovie favouriteMovie;

    private MainViewModel viewModel;

    private static String language;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.itemMain) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.itemFavourite) {
            Intent intentToFavourite = new Intent(this, FavouriteActivity.class);
            startActivity(intentToFavourite);
        }
        return super.onOptionsItemSelected(item);
    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        language = Locale.getDefault().getLanguage();
        scrollViewInfo = findViewById(R.id.scrollViewInfo);
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
        Picasso.get().load(movie.getBigPosterPath()).placeholder(R.drawable.placeholder_item).into(ivBigPoster);
        tvTitle.setText(movie.getTitle());
        tvOriginalTitle.setText(movie.getOriginalTitle());
        tvRating.setText(Double.toString(movie.getVoteAverage()));
        tvRelease.setText(movie.getReleaseDate());
        tvOverview.setText(movie.getOverview());
        setFavourite();
        rvTrailers = findViewById(R.id.rvTrailers);
        rvReviews = findViewById(R.id.rvReviews);
        reviewAdapter = new ReviewAdapter();
        trailerAdapter = new TrailerAdapter();
        trailerAdapter.setOnTrailerClickListener(url -> {
            Intent intentToTrailer = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intentToTrailer);
        });
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvTrailers.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);
        rvTrailers.setAdapter(trailerAdapter);
        JSONObject jsonObjectTrailers = NetworkUtils.getJSONForVideos(movie.getId(), language);
        JSONObject jsonObjectReviews = NetworkUtils.getJSONForReviews(movie.getId(), language);
        ArrayList<Trailer> trailers = JSONUtils.getTrailersFromJSON(jsonObjectTrailers);
        ArrayList<Review> reviews = JSONUtils.getReviewsFromJSON(jsonObjectReviews);
        reviewAdapter.setReviews(reviews);
        trailerAdapter.setTrailers(trailers);
        scrollViewInfo.smoothScrollTo(0,0);
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