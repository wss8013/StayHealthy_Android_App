package com.example.stayhealthy_android_app;

import static com.example.stayhealthy_android_app.Period.PeriodActivity.MONTHLY_PERIOD;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.stayhealthy_android_app.Diet.DietActivity;
import com.example.stayhealthy_android_app.Period.Model.PeriodData;
import com.example.stayhealthy_android_app.Period.PeriodActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

public class HealthRecordActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private final static String TAG = "MHealthRecordActivity";
    private final static String DATE_SHORT_FORMAT = "yyyy-MM-dd";
    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private NavigationView profile_nv;
    private DatabaseReference mDatabase;
    private DatabaseReference staticDietDB;
    private DatabaseReference dieDB;
    private DatabaseReference waterDB;
    private DatabaseReference periodDB;

    private ProgressBar pbDiet;
    private ProgressBar pbWater;
    private ProgressBar pbPeriod;
    private ProgressBar pbWorkout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_record);

        initProgressBars();
        staticDietDB = FirebaseDatabase.getInstance().getReference("user").
                child("test@gmail_com").child("diets").child("20220731");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        mDatabase = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

        dieDB = mDatabase.child("diets").child(java.time.LocalDate.now().toString());
        waterDB = mDatabase;
        periodDB = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("period");


        updatePBDiet();
        updatePBWater();
        updatePBPeriod();
        updatePBWorkout();

        initWidgets();
        setBottomNavigationView();
        initProfileDrawer();

    }

    private void updatePBDiet() {
        staticDietDB.get().addOnCompleteListener(task -> {
            HashMap tempMap = (HashMap) task.getResult().getValue();
            long breakfastNet = (long) ((HashMap) tempMap.get("breakfast")).get("net");
            long lunchNet = (long) ((HashMap) tempMap.get("lunch")).get("net");
            long dinnerNet = (long) ((HashMap) tempMap.get("dinner")).get("net");
            long snackNet = (long) ((HashMap) tempMap.get("snack")).get("net");
            long netCal = breakfastNet + lunchNet + dinnerNet + snackNet;
            long targetCal = (long) tempMap.get("target");
            double v = 100 * (double) netCal / targetCal;
            this.pbDiet.setProgress((int) v);
            dieDB.child("breakfast").child("net").setValue(breakfastNet);
            dieDB.child("lunch").child("net").setValue(lunchNet);
            dieDB.child("dinner").child("net").setValue(dinnerNet);
            dieDB.child("snack").child("net").setValue(snackNet);
            dieDB.child("target").setValue(targetCal);
        });
    }

    private void updatePBWater() {
        // TODO update progress bar of water
    }

    private void updatePBPeriod() {
        // TODO update progress bar of period
        LocalDate today = LocalDate.now();
        String date = localDateToDateInStr(today);
        Query query = periodDB.orderByChild("flowAndDate").endAt("1-" + date).limitToLast(1);

        query.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.v("", "Error getting data", task.getException());
            } else {
                for (DataSnapshot ds : task.getResult().getChildren()) {
                    PeriodData periodData = ds.getValue(PeriodData.class);
                    if (periodData != null && periodData.getHadFlow()) {
                        LocalDate startDate = LocalDate.parse(periodData.getStartDate());
                        int times = (int) (calculateDaysBetween(periodData.getStartDate(), date) / MONTHLY_PERIOD + 1);
                        // Calculated PredictedDate in the format "MMM dd yyyy"
                        LocalDate predictedDate = startDate.plusDays(MONTHLY_PERIOD * times);
                        String predictedDateInStr = localDateToDateInStr(predictedDate);
                        int remainingDays = (int) calculateDaysBetween(date, predictedDateInStr) - 1;
                        this.pbPeriod.setMax((int) MONTHLY_PERIOD);
                        this.pbPeriod.setProgress((int) MONTHLY_PERIOD - remainingDays);
                    } else {
                        this.pbPeriod.setMax((int) MONTHLY_PERIOD);
                        this.pbPeriod.setProgress(0); // "No record"
                    }
                }
            }
        });

    }

    private void updatePBWorkout() {
        // TODO update progress bar of workout
    }

    private void initProgressBars() {
        this.pbDiet = findViewById(R.id.progressBarDiet);
        this.pbPeriod = findViewById(R.id.progressBarPeriod);
        this.pbWater = findViewById(R.id.progressBarWater);
        this.pbWorkout = findViewById(R.id.progressBarWorkout);
    }

    private void initProfileDrawer() {
        // Initialize profile drawer
        drawer = findViewById(R.id.drawer_layout);
        profile_nv = findViewById(R.id.nav_view_health_record);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Health Records");
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        profile_nv.setNavigationItemSelectedListener(this);

        //set up the header button listeners
        View headerView = profile_nv.getHeaderView(0);
        Button LogOutBtn = (Button) headerView.findViewById(R.id.profile_logout_btn);
        Button ChangeAvartaButton = (Button) headerView.findViewById(R.id.update_profile_image_btn);

        LogOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        ChangeAvartaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //to do:
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set home selected when going back to this activity from other activities
        bottomNavigationView.setSelectedItemId(R.id.health_record_icon);
        updatePBDiet();
        updatePBWater();
        updatePBPeriod();
        updatePBWorkout();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void openPeriodActivity(View view) {
        Intent intent = new Intent(this, PeriodActivity.class);
        startActivity(intent);
    }

    public void openWaterActivity(View view) {
        Intent intent = new Intent(this, WaterActivity.class);
        startActivity(intent);
    }

    public void openDietActivity(View view) {
        Intent intent = new Intent(this, DietActivity.class);
        startActivity(intent);
    }

    public void openWorkoutActivity(View view) {
        Intent intent = new Intent(this, WorkoutActivity.class);
        startActivity(intent);
    }


    private void initWidgets() {
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
    }

    private void setBottomNavigationView() {
        // Set Home selected
        bottomNavigationView.setSelectedItemId(R.id.health_record_icon);

        // Perform item selected listener
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int selectedId = item.getItemId();
            boolean isItemSelected = false;
            if (selectedId == R.id.award_icon) {
                startActivity(new Intent(getApplicationContext(), AwardActivity.class));
                overridePendingTransition(0, 0);
                isItemSelected = true;
            } else if (selectedId == R.id.health_record_icon) {
                isItemSelected = true;
            } else if (selectedId == R.id.journey_icon) {
                startActivity(new Intent(getApplicationContext(), JourneyActivity.class));
                overridePendingTransition(0, 0);
                isItemSelected = true;
            }
            return isItemSelected;
        });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        switch (item.getItemId()) {
            case R.id.nav_settings:
                drawer.closeDrawers();
                Intent i = new Intent(HealthRecordActivity.this, SettingsActivity.class);
                startActivity(i);
                break;
            case R.id.nav_health_records:
                drawer.closeDrawers();
                startActivity(new Intent(getApplicationContext(), HealthRecordActivity.class));
                break;
            case R.id.nav_award:
                drawer.closeDrawers();
                startActivity(new Intent(getApplicationContext(), AwardActivity.class));
                break;
            case R.id.nav_journey:
                drawer.closeDrawers();
                startActivity(new Intent(getApplicationContext(), JourneyActivity.class));
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Convert LocalDate to date in specified string format.
    private String localDateToDateInStr(LocalDate date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_SHORT_FORMAT);
        return date.format(dateTimeFormatter);
    }

    // Calculate the days between start and end, not include start or end date. Here the `start`
    // and `end` are in DATE_SHORT_FORMAT, "yyyy-mm-dd".
    private long calculateDaysBetween(String start, String end) {
        if (start.equals("") || end.equals("")) {
            return 0;
        }
        LocalDate dateBefore = LocalDate.parse(start);
        LocalDate dateAfter = LocalDate.parse(end);
        return ChronoUnit.DAYS.between(dateBefore, dateAfter);
    }
}