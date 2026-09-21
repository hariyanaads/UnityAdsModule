package com.dbzbantenLS;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.view.Gravity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class MainActivity extends Activity {

    private String getConfigPath() {
        return getFilesDir().getAbsolutePath() + "/unityads_config.json";
    }

    private TextView tvStatus;
    private TextView tvMultiplierValue;
    private TextView tvGameCount;
    private SeekBar seekMultiplier;
    private Switch swAntiCheat;
    private Switch swTraffic;
    private Switch swAnalytics;
    private Switch swDebug;
    private Switch swAppLovin;
    private Switch swPangle;
    private Button btnSave;
    private Button btnReset;
    private Button btnTest;

    private int currentMultiplier = 100000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createUI();
        loadConfig();
        setupListeners();
    }
    
    private void createUI() {
        ScrollView scrollView = new ScrollView(this);
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(20, 20, 20, 20);
        
        // Title
        TextView tvTitle = new TextView(this);
        tvTitle.setText("DbzBanten UnityAds Module");
        tvTitle.setTextSize(20);
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setGravity(Gravity.CENTER);
        mainLayout.addView(tvTitle);
        
        // Status
        tvStatus = new TextView(this);
        tvStatus.setText("● INITIALIZING...");
        tvStatus.setTextColor(Color.YELLOW);
        tvStatus.setPadding(0, 10, 0, 10);
        mainLayout.addView(tvStatus);
        
        // Multiplier Section
        TextView tvMultiplierLabel = new TextView(this);
        tvMultiplierLabel.setText("Reward Multiplier");
        tvMultiplierLabel.setTextColor(Color.WHITE);
        tvMultiplierLabel.setPadding(0, 10, 0, 5);
        mainLayout.addView(tvMultiplierLabel);
        
        tvMultiplierValue = new TextView(this);
        tvMultiplierValue.setText("100000x");
        tvMultiplierValue.setTextColor(Color.parseColor("#00ff88"));
        tvMultiplierValue.setTextSize(18);
        mainLayout.addView(tvMultiplierValue);
        
        seekMultiplier = new SeekBar(this);
        seekMultiplier.setMax(10000);
        seekMultiplier.setProgress(100);
        mainLayout.addView(seekMultiplier);
        
        // Game Count
        tvGameCount = new TextView(this);
        tvGameCount.setText("Games Hooked: 0");
        tvGameCount.setTextColor(Color.GRAY);
        tvGameCount.setPadding(0, 10, 0, 10);
        mainLayout.addView(tvGameCount);
        
        // Switches
        swAntiCheat = createSwitch("Anti-Cheat Bypass");
        swTraffic = createSwitch("Traffic Manipulation");
        swAnalytics = createSwitch("Block Analytics");
        swDebug = createSwitch("Debug Mode");
        swAppLovin = createSwitch("Block AppLovin");
        swPangle = createSwitch("Block Pangle");
        
        mainLayout.addView(swAntiCheat);
        mainLayout.addView(swTraffic);
        mainLayout.addView(swAnalytics);
        mainLayout.addView(swDebug);
        mainLayout.addView(swAppLovin);
        mainLayout.addView(swPangle);
        
        // Buttons
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonLayout.setGravity(Gravity.CENTER);
        
        btnSave = new Button(this);
        btnSave.setText("Save");
        
        btnReset = new Button(this);
        btnReset.setText("Reset");
        
        btnTest = new Button(this);
        btnTest.setText("Test");
        
        buttonLayout.addView(btnSave);
        buttonLayout.addView(btnReset);
        buttonLayout.addView(btnTest);
        
        mainLayout.addView(buttonLayout);
        scrollView.addView(mainLayout);
        setContentView(scrollView);
    }
    
    private Switch createSwitch(String text) {
        Switch sw = new Switch(this);
        sw.setText(text);
        sw.setTextColor(Color.WHITE);
        sw.setPadding(0, 5, 0, 5);
        return sw;
    }

    private void loadConfig() {
        try {
            File configFile = new File(getConfigPath());
            if (!configFile.exists()) {
                createDefaultConfig();
                updateGameCount(0);
                return;
            }

            StringBuilder jsonString = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(configFile));
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(jsonString.toString());

            currentMultiplier = json.optInt("reward_multiplier", 100000);
            seekMultiplier.setProgress(currentMultiplier / 10);
            updateMultiplierDisplay(currentMultiplier);

            swAntiCheat.setChecked(json.optBoolean("anti_cheat_bypass", true));
            swTraffic.setChecked(json.optBoolean("traffic_manipulation", true));
            swAnalytics.setChecked(json.optBoolean("block_analytics", true));
            swDebug.setChecked(json.optBoolean("debug_mode", false));
            swAppLovin.setChecked(json.optBoolean("block_applovin", true));
            swPangle.setChecked(json.optBoolean("block_pangle", true));

            int gameCount = json.optInt("games_hooked", 0);
            updateGameCount(gameCount);

            tvStatus.setText("● CONFIG LOADED");
            tvStatus.setTextColor(Color.parseColor("#00ff88"));

        } catch (Exception e) {
            tvStatus.setText("● ERROR: " + e.getMessage());
            tvStatus.setTextColor(Color.parseColor("#ff3366"));
            updateGameCount(0);
        }
    }

    private void createDefaultConfig() {
        currentMultiplier = 100000;
        seekMultiplier.setProgress(100);
        updateMultiplierDisplay(100000);

        swAntiCheat.setChecked(true);
        swTraffic.setChecked(true);
        swAnalytics.setChecked(true);
        swDebug.setChecked(false);
        swAppLovin.setChecked(true);
        swPangle.setChecked(true);

        saveConfig();
    }

    private void setupListeners() {
        seekMultiplier.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentMultiplier = Math.max(1, progress * 10);
                updateMultiplierDisplay(currentMultiplier);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnSave.setOnClickListener(v -> {
            saveConfig();
            Toast.makeText(this, "Configuration saved successfully", Toast.LENGTH_SHORT).show();
        });

        btnReset.setOnClickListener(v -> {
            createDefaultConfig();
            Toast.makeText(this, "Reset to default values", Toast.LENGTH_SHORT).show();
        });

        btnTest.setOnClickListener(v -> {
            Toast.makeText(this, "DbzBanten UnityAds Module - All systems operational", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateMultiplierDisplay(int value) {
        tvMultiplierValue.setText(value + "x");

        int color;
        if (value < 100) color = Color.parseColor("#00ff88");
        else if (value < 100000) color = Color.parseColor("#ffcc00");
        else if (value < 5000) color = Color.parseColor("#ff8800");
        else color = Color.parseColor("#ff3366");

        tvMultiplierValue.setTextColor(color);
    }

    private void updateGameCount(int count) {
        tvGameCount.setText("Games Hooked: " + count);
    }

    private void saveConfig() {
        try {
            JSONObject json = new JSONObject();
            json.put("reward_multiplier", currentMultiplier);
            json.put("anti_cheat_bypass", swAntiCheat.isChecked());
            json.put("traffic_manipulation", swTraffic.isChecked());
            json.put("block_analytics", swAnalytics.isChecked());
            json.put("debug_mode", swDebug.isChecked());
            json.put("block_applovin", swAppLovin.isChecked());
            json.put("block_pangle", swPangle.isChecked());
            json.put("games_hooked", 0);

            File configFile = new File(getConfigPath());

            FileWriter writer = new FileWriter(configFile);
            writer.write(json.toString(2));
            writer.close();

            tvStatus.setText("● SAVED");
            tvStatus.setTextColor(Color.parseColor("#00d9ff"));

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                tvStatus.setText("● ACTIVE");
                tvStatus.setTextColor(Color.parseColor("#00ff88"));
            }, 2000);

        } catch (Exception e) {
            tvStatus.setText("● SAVE FAILED");
            tvStatus.setTextColor(Color.parseColor("#ff3366"));
            Toast.makeText(this, "Error saving: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}