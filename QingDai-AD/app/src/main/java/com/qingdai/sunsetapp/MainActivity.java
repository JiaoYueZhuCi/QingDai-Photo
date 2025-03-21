package com.qingdai.sunsetapp;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private TextView eventTimeTextView, dataSourceTextView, aerosolTextView, qualityTextView, errorTextView;
    private ProgressBar progressBar;
    private Button refreshButton;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化视图
        eventTimeTextView = findViewById(R.id.eventTimeTextView);
        dataSourceTextView = findViewById(R.id.dataSourceTextView);
        aerosolTextView = findViewById(R.id.aerosolTextView);
        qualityTextView = findViewById(R.id.qualityTextView);
        errorTextView = findViewById(R.id.errorTextView);
        progressBar = findViewById(R.id.progressBar);
        refreshButton = findViewById(R.id.refreshButton);

        // 初始化API服务
        apiService = ApiClient.getClient().create(ApiService.class);

        // 设置刷新按钮点击事件
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchData();
            }
        });

        // 首次加载数据
        fetchData();
    }

    private void fetchData() {
        showLoading();

        // 使用示例参数调用API
        Call<SunsetResponse> call = apiService.getSunsetData(
                "5754827",
                "select_city",
                "天津市-天津",
                "None",
                "rise_1",
                "None"
        );

        call.enqueue(new Callback<SunsetResponse>() {
            @Override
            public void onResponse(Call<SunsetResponse> call, Response<SunsetResponse> response) {
                hideLoading();
                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body());
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(Call<SunsetResponse> call, Throwable t) {
                hideLoading();
                showError();
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(SunsetResponse data) {
        eventTimeTextView.setText(data.getCleanTbEventTime());
        dataSourceTextView.setText(data.getCleanImgSummary());
        aerosolTextView.setText(data.getCleanTbAod());
        qualityTextView.setText(data.getCleanTbQuality());
        
        errorTextView.setVisibility(View.GONE);
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        errorTextView.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showError() {
        errorTextView.setVisibility(View.VISIBLE);
    }
} 