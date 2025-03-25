package com.qingdai.photo;

import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.jsoup.Jsoup;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://www.sunsetbot.top/";
    
    private TextView cityTextView;
    private TextView timeTextView;
    private TextView qualityTextView;
    private TextView aodTextView;
    private ImageView weatherImageView;
    private ProgressBar loadingProgressBar;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupRetrofit();
        fetchWeatherData();
    }

    private void initViews() {
        cityTextView = findViewById(R.id.cityTextView);
        timeTextView = findViewById(R.id.timeTextView);
        qualityTextView = findViewById(R.id.qualityTextView);
        aodTextView = findViewById(R.id.aodTextView);
        weatherImageView = findViewById(R.id.weatherImageView);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
    }


    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        apiService = retrofit.create(ApiService.class);
    }
    
    private void fetchWeatherData() {
        Call<WeatherData> call = apiService.getWeatherData(
                "1877020",
                "select_city",
                "天津",
                "None",
                "rise_1",
                "None"
        );
        
        call.enqueue(new Callback<WeatherData>() {
            @Override
            public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {
                loadingProgressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    WeatherData weatherData = response.body();
                    updateUI(weatherData);
                } else {
                    Toast.makeText(MainActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<WeatherData> call, Throwable t) {
                loadingProgressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateUI(WeatherData data) {
        if (data == null) {
            return;
        }
        
        // 更新城市信息
        String placeHolder = data.getPlaceHolder();
        if (!TextUtils.isEmpty(placeHolder)) {
            cityTextView.setText(String.format(getString(R.string.city_label), placeHolder));
        } else {
            cityTextView.setText(String.format(getString(R.string.city_label), "未知"));
        }
        
        // 更新北京时间 (从img_summary中提取)
        String imgSummary = data.getImgSummary();
        if (!TextUtils.isEmpty(imgSummary)) {
            // 去除HTML标签
            String plainText = Html.fromHtml(imgSummary, Html.FROM_HTML_MODE_LEGACY).toString();
            timeTextView.setText(String.format(getString(R.string.time_label), plainText));
        } else {
            timeTextView.setText(String.format(getString(R.string.time_label), "未知"));
        }
        
        // 更新鲜艳度 (tb_quality)
        String quality = data.getTbQuality();
        if (!TextUtils.isEmpty(quality)) {
            String plainQuality = Html.fromHtml(quality, Html.FROM_HTML_MODE_LEGACY).toString().trim();
            qualityTextView.setText(String.format(getString(R.string.quality_label), plainQuality));
        } else {
            qualityTextView.setText(String.format(getString(R.string.quality_label), "未知"));
        }
        
        // 更新气溶胶 (tb_aod)
        String aod = data.getTbAod();
        if (!TextUtils.isEmpty(aod)) {
            String plainAod = Html.fromHtml(aod, Html.FROM_HTML_MODE_LEGACY).toString().trim();
            aodTextView.setText(String.format(getString(R.string.aod_label), plainAod));
        } else {
            aodTextView.setText(String.format(getString(R.string.aod_label), "未知"));
        }
        
        // 加载图片
        String imageUrl = data.getImgHref();
        if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(this)
                    .load(BASE_URL + imageUrl)
                    .centerCrop()
                    .into(weatherImageView);
        }
    }
} 