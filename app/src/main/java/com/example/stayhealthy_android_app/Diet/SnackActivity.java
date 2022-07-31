package com.example.stayhealthy_android_app.Diet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import com.example.stayhealthy_android_app.R;

import java.util.ArrayList;

public class SnackActivity extends AppCompatActivity {
    private int protein;
    private int fat;
    private int carbs;
    private int netCal;
    private TextView proteinView;
    private TextView fatView;
    private TextView carbsView;
    private TextView netCalView;

    private ArrayList<FoodItem> foods = new ArrayList<>();
    private FoodAdapter foodAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snack);
        loadValues();
        initTextViews();
        fillValues();

        createRecyclerView();
    }

    private void createRecyclerView() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        RecyclerView recyclerView = findViewById(R.id.snack_recycler_review);
        recyclerView.setHasFixedSize(true);

        foodAdapter = new FoodAdapter(foods);
        FoodClickListener listener = position -> {
            foods.get(position).onLinkClicked(position);
            foodAdapter.notifyItemChanged(position);
        };

        foodAdapter.setFoodClickListener(listener);
        recyclerView.setAdapter(foodAdapter);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadValues();
        fillValues();
    }

    private void loadValues() {
        protein = 81;
        fat = 82;
        carbs = 83;
        netCal = 84;

        foods = new ArrayList<>();
        foods.add(new FoodItem("egg", "protein: 80 Cal; fat: 85 Cal; carbs: 80 Cal"));
        foods.add(new FoodItem("milk", "protein: 81 Cal; fat: 86 Cal; carbs: 81 Cal"));
        foods.add(new FoodItem("bread", "protein: 82 Cal; fat: 85 Cal; carbs: 80 Cal"));
    }

    private void initTextViews() {
        proteinView = findViewById(R.id.textView816);
        fatView = findViewById(R.id.textView810);
        carbsView = findViewById(R.id.textView817);
        netCalView = findViewById(R.id.textView87);
    }

    @SuppressLint("SetTextI18n")
    private void fillValues() {
        proteinView.setText("Protein: " + protein + " Cal");
        fatView.setText("Fat: " + fat + " Cal");
        carbsView.setText("Carbs: " + carbs + " Cal");
        netCalView.setText("Net Cal: " + netCal + " Cal");
    }
}