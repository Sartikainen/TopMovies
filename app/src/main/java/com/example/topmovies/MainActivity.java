package com.example.topmovies;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.topmovies.adapters.MovieAdapter;
import com.example.topmovies.data.MainViewModel;
import com.example.topmovies.data.Movie;
import com.example.topmovies.utils.JSONUtils;
import com.example.topmovies.utils.NetworkUtils;

import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<JSONObject> {

    private RecyclerView rvPosters;
    private MovieAdapter movieAdapter;
    private Switch switchSort;
    private TextView tvTopRated;
    private TextView tvPopularity;
    private ProgressBar progressBarLoading;

    private MainViewModel viewModel;

    private static final int LOADER_ID = 5;
    private LoaderManager loaderManager;

    private static int page = 1;
    private static int methodOfSort;
    private static boolean isLoading = false;

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

    private int getColumnCount() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels / displayMetrics.density);
        return width / 185 > 2 ? width / 185 : 2;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loaderManager = LoaderManager.getInstance(this);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        rvPosters = findViewById(R.id.rvPosters);
        switchSort = findViewById(R.id.switchSort);
        tvTopRated = findViewById(R.id.tvTopRated);
        tvPopularity = findViewById(R.id.tvPopularity);
        progressBarLoading = findViewById(R.id.progressBarLoading);
        tvTopRated.setOnClickListener(this::onClickSetTopRated);
        tvPopularity.setOnClickListener(this::onClickSetPopularity);
        rvPosters.setLayoutManager(new GridLayoutManager(this, getColumnCount()));
        movieAdapter = new MovieAdapter();
        rvPosters.setAdapter(movieAdapter);
        switchSort.setChecked(true);
        switchSort.setOnCheckedChangeListener((buttonView, isChecked) -> {
            page = 1;
            setMethodOfSort(isChecked);
        });
        switchSort.setChecked(false);
        movieAdapter.setOnPosterClickListener(position -> {
            Movie movie = movieAdapter.getMovies().get(position);
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("id", movie.getId());
            startActivity(intent);
        });
        movieAdapter.setOnReachEndListener(() -> {
            if (!isLoading) {
                downloadData(methodOfSort, page);
            }
        });
        LiveData<List<Movie>> moviesFromLiveData = viewModel.getMovies();
        moviesFromLiveData.observe(this, movies -> {
            if (page == 1) {
                movieAdapter.setMovies(movies);
            }
        });
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
        if (isTopRated) {
            tvTopRated.setTextColor(getResources().getColor(R.color.colorAccent));
            tvPopularity.setTextColor(getResources().getColor(R.color.white));
            methodOfSort = NetworkUtils.TOP_RATED;
        } else {
            tvPopularity.setTextColor(getResources().getColor(R.color.colorAccent));
            tvTopRated.setTextColor(getResources().getColor(R.color.white));
            methodOfSort = NetworkUtils.POPULARITY;
        }
        downloadData(methodOfSort, page);
    }

    private void downloadData(int methodOfSort, int page) {
        URL url = NetworkUtils.buildUrl(methodOfSort, page);
        Bundle bundle = new Bundle();
        bundle.putString("url", url.toString());
        loaderManager.restartLoader(LOADER_ID, bundle, this);
    }

    @NonNull
    @Override
    public Loader<JSONObject> onCreateLoader(int id, @Nullable Bundle bundle) {
        NetworkUtils.JSONLoader jsonLoader = new NetworkUtils.JSONLoader(this, bundle);
        jsonLoader.setOnStartLoadingListener(() -> {
            progressBarLoading.setVisibility(View.VISIBLE);
            isLoading = true;
        });
        return jsonLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<JSONObject> loader, JSONObject jsonObject) {
        ArrayList<Movie> movies = JSONUtils.getMoviesFromJSON(jsonObject);
        if (movies != null && !movies.isEmpty()) {
            if (page == 1) {
                viewModel.deleteAllMovies();
                movieAdapter.clear();
            }
            for (Movie movie : movies) {
                viewModel.insertMovie(movie);
            }
            movieAdapter.addMovies(movies);
        }
        isLoading = false;
        progressBarLoading.setVisibility(View.INVISIBLE);
        loaderManager.destroyLoader(LOADER_ID);
        page ++;
    }

    @Override
    public void onLoaderReset(@NonNull Loader<JSONObject> loader) {

    }
}