package com.example.stayhealthy_android_app;

import static com.example.stayhealthy_android_app.Period.PeriodActivity.MONTHLY_PERIOD;
import static com.example.stayhealthy_android_app.Water.WaterIntakeModel.DAILY_WATER_TARGET_OZ;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.HashMap;

public class HealthRecordActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private final static String TAG = "MHealthRecordActivity";
    private final static String DATE_FULL_FORMAT = "EEEE, MMMM d, yyyy";
    private final static String DATE_SHORT_FORMAT = "yyyy-MM-dd";
    private final static String DATE_LONG_FORMAT = "MMM dd";
    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private NavigationView profile_nv;
    private DatabaseReference mDatabase;
    private DatabaseReference staticDietDB;
    private DatabaseReference dieDB;
    private DatabaseReference waterDB;
    private DatabaseReference periodDB;
    private DatabaseReference workoutDB;

    private ProgressBar pbDiet;
    private ProgressBar pbWater;
    private ProgressBar pbPeriod;
    private ProgressBar pbWorkout;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    FirebaseStorage fStorage;
    FirebaseUser user;
    StorageReference storageReference;

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

        mDatabase.child("notification_settings").child("Work-Out Notification").setValue(true);
        mDatabase.child("notification_settings").child("Water Notification").setValue(true);
        mDatabase.child("notification_settings").child("Diet Notification").setValue(true);
        mDatabase.child("notification_settings").child("Period Notification").setValue(true);

        dieDB = mDatabase.child("diets").child(java.time.LocalDate.now().toString());
        waterDB = mDatabase.child("water_intake").child(java.time.LocalDate.now().toString());
        periodDB = mDatabase.child("period");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("LLLL dd yyyy");
        workoutDB = mDatabase.child("work-out").child(LocalDate.now().format(formatter));

        updatePBDiet();
        updatePBWater();
        updatePBPeriod();
        updatePBWorkout();

        initWidgets();
        setBottomNavigationView();
        initProfileDrawer();

        // Set date and welcome user text
        setDateAndWelcomeUserTextView();

        //set up the notification
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR_OF_DAY, 10);
//        calendar.set(Calendar.MINUTE, 30);
//        calendar.set(Calendar.SECOND, 0);
//        System.out.println("here 1");
//        Intent intent1 = new Intent(HealthRecordActivity.this, AlarmReceiver.class);
//        System.out.println("here 2");
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(HealthRecordActivity.this, 0 ,intent1, PendingIntent.FLAG_UPDATE_CURRENT);
//        System.out.println("here 3");
//        AlarmManager alarmManager = (AlarmManager) HealthRecordActivity.this.getSystemService(HealthRecordActivity.this.ALARM_SERVICE);
//        System.out.println("here 4");
//        if (alarmManager != null) {
//            System.out.println("here in side if statement");
//            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
//        }
//        System.out.println("here 5");
    }




    private void updatePBDiet() {
        staticDietDB.get().addOnCompleteListener(task -> {
            TextView dietProgressBarTV = findViewById(R.id.dietProgressBarTV);
            TextView dietDetailsTV = findViewById(R.id.dietDetailsTV);
            try {
                HashMap tempMap = (HashMap) task.getResult().getValue();
                long breakfastNet = (long) ((HashMap) tempMap.get("breakfast")).get("net");
                long lunchNet = (long) ((HashMap) tempMap.get("lunch")).get("net");
                long dinnerNet = (long) ((HashMap) tempMap.get("dinner")).get("net");
                long snackNet = (long) ((HashMap) tempMap.get("snack")).get("net");
                long netCal = breakfastNet + lunchNet + dinnerNet + snackNet;
                long targetCal = (long) tempMap.get("target");
                double v = 100 * (double) netCal / targetCal;
                this.pbDiet.setProgress((int) v);
                String percentInStr = ((int) v) + "%";
                dietProgressBarTV.setText(percentInStr);
                String goalStr = "Goal " + ((int) targetCal) + " Cal";
                dietDetailsTV.setText(goalStr);
                dieDB.child("breakfast").child("net").setValue(breakfastNet);
                dieDB.child("lunch").child("net").setValue(lunchNet);
                dieDB.child("dinner").child("net").setValue(dinnerNet);
                dieDB.child("snack").child("net").setValue(snackNet);
                dieDB.child("target").setValue(targetCal);
            } catch (Exception err) {
                this.pbDiet.setProgress(0);
                dietProgressBarTV.setText(R.string._0_percent_string);
                dietDetailsTV.setText(R.string.goal_string);
            }
        });
    }

    private void updatePBWater() {
        waterDB.get().addOnCompleteListener(task -> {
            TextView waterProgressBarTV = findViewById(R.id.waterProgressBarTV);
            TextView waterDetailsTV = findViewById(R.id.waterDetailsTV);
            try {
                HashMap tempMap = (HashMap) task.getResult().getValue();
                long taken = (long) tempMap.get("waterOz");
                double v = 100 * (double) taken / DAILY_WATER_TARGET_OZ;
                this.pbWater.setProgress(v > 100 ? 100 : (int) v);
                String percentInStr = ((int) v) + "%";
                waterProgressBarTV.setText(percentInStr);
            } catch (Exception err) {
                this.pbWater.setProgress(0);
                waterProgressBarTV.setText(R.string._0_percent_string);
            }
            String goal = "Goal " + DAILY_WATER_TARGET_OZ + " Oz";
            waterDetailsTV.setText(goal);
        });
    }

    private void updatePBPeriod() {
        LocalDate today = LocalDate.now();
        String date = localDateToDateInStr(today, DATE_SHORT_FORMAT);
        Query query = periodDB.orderByChild("flowAndDate").endAt("1-" + date).limitToLast(1);

        query.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.v("", "Error getting data", task.getException());
            } else {
                TextView periodProgressBarTV = findViewById(R.id.periodProgressBarTV);
                TextView periodDetailsTV = findViewById(R.id.periodDetailsTV);
                for (DataSnapshot ds : task.getResult().getChildren()) {
                    PeriodData periodData = ds.getValue(PeriodData.class);
                    if (periodData != null && periodData.getHadFlow()) {
                        LocalDate startDate = LocalDate.parse(periodData.getStartDate());
                        int times = (int) (calculateDaysBetween(periodData.getStartDate(), date) / MONTHLY_PERIOD + 1);
                        // Calculated PredictedDate in the format "MMM dd yyyy"
                        LocalDate predictedDate = startDate.plusDays(MONTHLY_PERIOD * times);
                        String predictedDateInStr = localDateToDateInStr(predictedDate, DATE_SHORT_FORMAT);
                        int remainingDays = (int) calculateDaysBetween(date, predictedDateInStr);
                        // Set progress bar
                        pbPeriod.setMax((int) MONTHLY_PERIOD);
                        pbPeriod.setProgress((int) MONTHLY_PERIOD - remainingDays);
                        // Set progress bar text
                        String remainingDaysInStr = remainingDays + " Days";
                        periodProgressBarTV.setText(remainingDaysInStr);
                        // Set period details
                        String prediction = "Likely to start on " + localDateToDateInStr(predictedDate, DATE_LONG_FORMAT);
                        periodDetailsTV.setText(prediction);
                    } else {
                        pbPeriod.setMax((int) MONTHLY_PERIOD);
                        pbPeriod.setProgress(0); // "No record"
                        periodProgressBarTV.setText(R.string.no_record_string);
                        periodDetailsTV.setText(R.string.likely_to_start_on_string);
                    }
                }
            }
        });

    }

    private void updatePBWorkout() {
        workoutDB.get().addOnCompleteListener(task -> {
            TextView workoutProgressBarTV = findViewById(R.id.workoutProgressBarTV);
            TextView workoutDetailsTV = findViewById(R.id.workoutDetailsTV);
            HashMap tempMap = (HashMap) task.getResult().getValue();
            try {
                boolean oneFinished = (boolean) ((HashMap) tempMap.get("Activity_one")).get("goal_finished_status");
                boolean twoFinished = (boolean) ((HashMap) tempMap.get("Activity_two")).get("goal_finished_status");
                boolean threeFinished = (boolean) ((HashMap) tempMap.get("Activity_three")).get("goal_finished_status");
                boolean fourFinished = (boolean) ((HashMap) tempMap.get("Activity_four")).get("goal_finished_status");
                int count = 0;
                if (oneFinished) count++;
                if (twoFinished) count++;
                if (threeFinished) count++;
                if (fourFinished) count++;
                int percent = 25 * count;
                this.pbWorkout.setProgress(percent);
                String percentInStr = percent + "%";
                workoutProgressBarTV.setText(percentInStr);
            } catch (Exception err) {
                this.pbWorkout.setProgress(0);
                workoutProgressBarTV.setText(R.string._0_percent_string);
            }
        });
    }

    private void initProgressBars() {
        this.pbDiet = findViewById(R.id.dietProgressBar);
        this.pbPeriod = findViewById(R.id.periodProgressBar);
        this.pbWater = findViewById(R.id.waterProgressBar);
        this.pbWorkout = findViewById(R.id.workoutProgressBar);
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
        TextView userNameText = (TextView) headerView.findViewById(R.id.user_name_show);
        ImageView user_image = (ImageView) headerView.findViewById(R.id.image_avatar);
        fStorage = FirebaseStorage.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = fStorage.getReference("users").child(user.getUid());

        // calling add value event listener method
        // for getting the values from database.
        DatabaseReference email_ref = mDatabase.child("email");

        email_ref.get().addOnCompleteListener(task -> {
            try {
                String email = (String) task.getResult().getValue();
                userNameText.setText(email);
            } catch (Exception err) {
                System.out.println("error retreive data from database");
            }
        });

        LogOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        ChangeAvartaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddPicturePressed(v);
            }
        });
    }

    public void onAddPicturePressed(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // simply return to the last activity.
            onBackPressed();
        }
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

    private void setDateAndWelcomeUserTextView() {
        // Display Today in string.
        String today = localDateToDateInStr(LocalDate.now(), DATE_FULL_FORMAT);
        TextView todayTV = findViewById(R.id.todayTV);
        todayTV.setText(today);

        // Display welcome user
        TextView welcomeTV = findViewById(R.id.welcomeTV);
        DatabaseReference emailRef = mDatabase.child("email");
        emailRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.v(TAG, "Error getting data", task.getException());
            } else {
                String email = task.getResult().getValue(String.class);
                String welcome = "Hi, " + email;
                 welcomeTV.setText(welcome);
            }
        });
    }

    // Convert LocalDate to date in specified string format.
    private String localDateToDateInStr(LocalDate date, String dateFormat) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat);
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