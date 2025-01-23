package com.example.topmovies;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.topmovies.data.MainViewModel;
import com.example.topmovies.data.Movie;
import com.example.topmovies.utils.JSONUtils;
import com.example.topmovies.utils.NetworkUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvPosters;
    private MovieAdapter movieAdapter;
    private Switch switchSort;
    private TextView tvTopRated;
    private TextView tvPopularity;

    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        rvPosters = findViewById(R.id.rvPosters);
        switchSort = findViewById(R.id.switchSort);
        tvTopRated = findViewById(R.id.tvTopRated);
        tvPopularity = findViewById(R.id.tvPopularity);
        tvTopRated.setOnClickListener(this::onClickSetTopRated);
        tvPopularity.setOnClickListener(this::onClickSetPopularity);
        rvPosters.setLayoutManager(new GridLayoutManager(this, 2));
        movieAdapter = new MovieAdapter();
        rvPosters.setAdapter(movieAdapter);
        switchSort.setChecked(true);
        switchSort.setOnCheckedChangeListener((buttonView, isChecked) -> setMethodOfSort(isChecked));
        switchSort.setChecked(false);
        movieAdapter.setOnPosterClickListener(position -> {
            Movie movie = movieAdapter.getMovies().get(position);
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("id", movie.getId());
            startActivity(intent);
        });
        movieAdapter.setOnReachEndListener(() -> Toast.makeText(MainActivity.this, "End", Toast.LENGTH_SHORT).show());
        LiveData<List<Movie>> moviesFromLiveData = viewModel.getMovies();
        moviesFromLiveData.observe(this, movies -> movieAdapter.setMovies(movies));
    }

    private void onClickSetPopularity(View view) {
        setMethodOfSort(false);
        switchSort.setChecked(false);
    }

    private void onClickSetTopRated(View view) {
        setMethodOfSort(true);
        switchSort.setChecked(true);
    }

    private void setMethodOfSort(boolean isTopRated) {
        int methodOfSort;
        if (isTopRated) {
            tvTopRated.setTextColor(getResources().getColor(R.color.colorAccent));
            tvPopularity.setTextColor(getResources().getColor(R.color.white));
            methodOfSort = NetworkUtils.TOP_RATED;
        } else {
            tvPopularity.setTextColor(getResources().getColor(R.color.colorAccent));
            tvTopRated.setTextColor(getResources().getColor(R.color.white));
            methodOfSort = NetworkUtils.POPULARITY;
        }
        downloadData(methodOfSort, 1);
    }

    private void downloadData(int methodOfSort, int page) {
        JSONObject jsonObject = NetworkUtils.getJSONFromNetwork(methodOfSort, 1);
        ArrayList<Movie> movies = JSONUtils.getMoviesFromJSON(jsonObject);
        if (movies != null && !movies.isEmpty()) {
            viewModel.deleteAllMovies();
            for (Movie movie : movies) {
                viewModel.insertMovie(movie);
            }
        }
    }
}