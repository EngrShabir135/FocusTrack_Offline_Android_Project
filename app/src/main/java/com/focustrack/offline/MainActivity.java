package com.focustrack.offline;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    final int BG = Color.rgb(7, 21, 43);
    final int CARD = Color.rgb(12, 34, 63);
    final int GREEN = Color.rgb(53, 208, 127);
    final int TEXT = Color.rgb(239, 247, 255);
    final int MUTED = Color.rgb(157, 181, 207);

    LinearLayout root, sessionsBox, skillsBox;
    TextView timer, current, todayTotal, streak;
    Spinner skillSpinner;
    EditText topicInput;
    SeekBar goalBar;
    TextView goalLabel;
    Button startBtn, finishBtn;

    SharedPreferences sp;
    Handler handler = new Handler(Looper.getMainLooper());

    long elapsed = 0;
    long startedAt = 0;
    boolean running = false;

    String selectedSkill = "Python";
    ArrayList<String> skills = new ArrayList<>();

    Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (running) {
                elapsed = (System.currentTimeMillis() - startedAt) / 1000;
                renderTimer();
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = getSharedPreferences("data", MODE_PRIVATE);
        loadSkills();
        buildUI();
        refresh();
    }

    void loadSkills() {
        skills.clear();
        String s = sp.getString("skills", "");
        if (s == null || s.isEmpty()) {
            Collections.addAll(skills,
                    "Python",
                    "FastAPI",
                    "Django",
                    "Machine Learning",
                    "Deep Learning",
                    "Mathematics",
                    "Physics",
                    "Chemistry",
                    "Biology",
                    "Web Dev"
            );
            saveSkills();
            return;
        }

        try {
            JSONArray a = new JSONArray(s);
            for (int i = 0; i < a.length(); i++) {
                String skill = a.optString(i, "");
                if (!skill.isEmpty()) {
                    skills.add(skill);
                }
            }
        } catch (Exception e) {
            skills.clear();
            Collections.addAll(skills, "Python", "Reading", "Math");
        }

        if (skills.isEmpty()) {
            skills.add("Python");
        }
    }

    void saveSkills() {
        try {
            JSONArray a = new JSONArray();
            for (String s : skills) {
                a.put(s);
            }
            sp.edit().putString("skills", a.toString()).apply();
        } catch (Exception ignored) {
        }
    }

    TextView tv(String text, int size, int color) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextSize(size);
        v.setTextColor(color);
        v.setPadding(0, 4, 0, 4);
        return v;
    }

    GradientDrawable bg(int color, float radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(radius);
        return g;
    }

    LinearLayout card() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(18, 16, 18, 16);
        l.setBackground(bg(CARD, 24));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        p.setMargins(0, 0, 0, 14);
        l.setLayoutParams(p);
        return l;
    }

    Button btn(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(Color.WHITE);
        b.setAllCaps(false);
        b.setTextSize(15);
        b.setBackground(bg(GREEN, 40));
        b.setPadding(18, 8, 18, 8);
        return b;
    }

    @SuppressWarnings("unchecked")
    void buildUI() {
        ScrollView sv = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(18, 18, 18, 30);
        root.setBackgroundColor(BG);
        sv.addView(root);

        setContentView(sv);

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setPadding(0, 0, 0, 10);

        TextView title = tv("FocusTrack", 28, TEXT);
        title.setTypeface(null, Typeface.BOLD); // if you want bold style
        head.addView(title);

        root.addView(head);
        root.addView(tv("Your private study tracker — no internet required.", 14, MUTED));

        LinearLayout todayCard = card();
        todayCard.addView(tv("TODAY", 12, GREEN));
        todayTotal = tv("0m focused", 30, TEXT);
        todayTotal.setTypeface(null, Typeface.BOLD);
        todayCard.addView(todayTotal);

        streak = tv("🔥 0 day streak", 14, MUTED);
        todayCard.addView(streak);

        root.addView(todayCard);

        LinearLayout timerCard = card();
        timerCard.addView(tv("STUDY TIMER", 12, GREEN));
        timer = tv("00:00:00", 42, TEXT);
        timer.setGravity(Gravity.CENTER);
        timer.setTypeface(null, Typeface.BOLD);
        timerCard.addView(timer);

        current = tv("Choose a skill and start.", 14, MUTED);
        current.setGravity(Gravity.CENTER);
        timerCard.addView(current);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        skillSpinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                skills
        );
        skillSpinner.setAdapter(adapter);

        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        spinnerParams.setMargins(0, 0, 12, 0);
        skillSpinner.setLayoutParams(spinnerParams);

        row.addView(skillSpinner);

        topicInput = new EditText(this);
        topicInput.setHint("Topic (optional)");
        topicInput.setHintTextColor(MUTED);
        topicInput.setTextColor(TEXT);
        topicInput.setSingleLine(true);
        topicInput.setBackgroundColor(Color.TRANSPARENT);
        row.addView(topicInput);

        timerCard.addView(row);

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);

        startBtn = btn("Start");
        finishBtn = btn("Finish");
        finishBtn.setEnabled(false);
        finishBtn.setAlpha(0.5f);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        btnParams.setMargins(0, 0, 10, 0);

        startBtn.setLayoutParams(btnParams);
        finishBtn.setLayoutParams(btnParams);

        btnRow.addView(startBtn);
        btnRow.addView(finishBtn);

        timerCard.addView(btnRow);

        startBtn.setOnClickListener(v -> toggle());
        finishBtn.setOnClickListener(v -> finishSession());

        skillSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                selectedSkill = skills.get(position);
                current.setText("Selected: " + selectedSkill);
                goalLabel.setText(formatGoal(getGoal(selectedSkill)));
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        LinearLayout goal = card();
        goal.addView(tv("DAILY GOAL", 12, GREEN));
        goalLabel = tv("1.0 hour", 20, TEXT);
        goalLabel.setTypeface(null, Typeface.BOLD);
        goal.addView(goalLabel);

        goalBar = new SeekBar(this);
        goalBar.setMax(480);
        goalBar.setProgress(60);
        goalBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    String g = formatGoal(progress / 60f);
                    goalLabel.setText(g);
                    sp.edit().putFloat("goal_" + selectedSkill, progress / 60f).apply();
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        goal.addView(goalBar);

        TextView skillTitle = tv("SKILL PROGRESS", 18, TEXT);
        skillTitle.setTypeface(null, Typeface.BOLD);
        root.addView(skillTitle);

        skillsBox = new LinearLayout(this);
        skillsBox.setOrientation(LinearLayout.VERTICAL);
        root.addView(skillsBox);

        Button add = btn("＋ Add a skill");
        add.setOnClickListener(v -> addSkill());
        root.addView(add);

        TextView recentTitle = tv("RECENT SESSIONS", 18, TEXT);
        recentTitle.setTypeface(null, Typeface.BOLD);
        root.addView(recentTitle);

        sessionsBox = new LinearLayout(this);
        sessionsBox.setOrientation(LinearLayout.VERTICAL);
        root.addView(sessionsBox);

        TextView note = tv("All data stays on this phone. Turn off Wi‑Fi/mobile data and keep studying.", 12, MUTED);
        note.setPadding(4, 18, 4, 0);
        root.addView(note);
    }

    double getGoal(String skill) {
        return sp.getFloat("goal_" + skill, 1f);
    }

    long todaySeconds() {
        String day = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        long total = 0;

        try {
            JSONArray a = new JSONArray(sp.getString("sessions", "[]"));
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                String date = o.optString("date", "");
                if (day.equals(date)) {
                    total += o.optLong("seconds", 0);
                }
            }
        } catch (Exception ignored) {
        }

        return total;
    }

    void refresh() {
        long sec = todaySeconds();
        todayTotal.setText(format(sec) + " focused");
        int days = sp.getInt("streak", 0);
        streak.setText("🔥 " + days + " day streak");
        renderSkills();
        renderSessions();
    }

    void renderTimer() {
        timer.setText(format(elapsed));
    }

    String format(long s) {
        return String.format(Locale.US, "%02d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60);
    }

    String formatGoal(double hours) {
        return String.format(Locale.US, "%.1f hour", hours);
    }

    void toggle() {
        if (!running) {
            selectedSkill = skillSpinner.getSelectedItem().toString();
            running = true;
            startedAt = System.currentTimeMillis() - elapsed * 1000;
            startBtn.setText("Pause");
            finishBtn.setEnabled(true);
            finishBtn.setAlpha(1f);
            current.setText("Studying: " + selectedSkill);
            handler.post(tick);
        } else {
            running = false;
            startBtn.setText("Resume");
            handler.removeCallbacks(tick);
        }
    }

    void finishSession() {
        if (elapsed < 5) {
            Toast.makeText(this, "Study a little longer before saving.", Toast.LENGTH_SHORT).show();
            return;
        }

        running = false;
        handler.removeCallbacks(tick);
        startBtn.setText("Start");
        finishBtn.setEnabled(false);
        finishBtn.setAlpha(0.5f);

        String topic = topicInput.getText().toString().trim();
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());

        try {
            JSONArray a = new JSONArray(sp.getString("sessions", "[]"));
            JSONObject o = new JSONObject();
            o.put("date", date);
            o.put("skill", selectedSkill);
            o.put("topic", topic);
            o.put("seconds", elapsed);
            a.put(o);
            sp.edit().putString("sessions", a.toString()).apply();
        } catch (Exception ignored) {
        }

        long saved = elapsed;
        elapsed = 0;
        renderTimer();
        refresh();

        if (saved >= 1800) {
            int streakValue = sp.getInt("streak", 0);
            sp.edit().putInt("streak", streakValue + 1).apply();
        }

        String msg = "Saved " + format(saved) + " for " + selectedSkill;
        if (!topic.isEmpty()) {
            msg += " • " + topic;
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    void updateStreak() {
        sp.edit().putInt("streak", Math.min(999, sp.getInt("streak", 0) + 1)).apply();
    }

    void renderSkills() {
        skillsBox.removeAllViews();

        long today = todaySeconds();
        for (String s : skills) {
            long skillSec = skillSeconds(s);
            double goal = getGoal(s) * 3600;
            float pct = (float) Math.min(1.0, skillSec / goal);

            if (goal <= 0) {
                pct = 0f;
            }

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 8, 0, 8);

            TextView name = tv(s, 14, TEXT);
            name.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
            ));

            TextView value = tv(formatShort(skillSec) + " / " + formatShort((long) goal), 12, MUTED);

            row.addView(name);
            row.addView(value);

            skillsBox.addView(row);

            LinearLayout barWrap = new LinearLayout(this);
            barWrap.setBackground(bg(CARD, 18));
            barWrap.setPadding(6, 6, 6, 6);

            ViewGroup.LayoutParams wrapParams = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            barWrap.setLayoutParams(wrapParams);

            android.widget.ProgressBar bar = new android.widget.ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            bar.setMax(100);
            bar.setProgress((int) (pct * 100));
            bar.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    18
            ));
            barWrap.addView(bar);
            skillsBox.addView(barWrap);
        }
    }

    long skillSeconds(String skill) {
        long total = 0;

        try {
            JSONArray a = new JSONArray(sp.getString("sessions", "[]"));
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                if (skill.equals(o.optString("skill", ""))) {
                    total += o.optLong("seconds", 0);
                }
            }
        } catch (Exception ignored) {
        }

        return total;
    }

    String formatShort(long s) {
        if (s < 60) return s + "s";
        if (s < 3600) return (s / 60) + "m";
        return String.format(Locale.US, "%.1fh", s / 3600.0);
    }

    void renderSessions() {
        sessionsBox.removeAllViews();

        try {
            JSONArray a = new JSONArray(sp.getString("sessions", "[]"));
            int count = 0;

            for (int i = a.length() - 1; i >= 0 && count < 8; i--) {
                JSONObject o = a.getJSONObject(i);

                LinearLayout item = card();
                String skill = o.optString("skill", "Study");
                String topic = o.optString("topic", "");
                String date = o.optString("date", "");
                long seconds = o.optLong("seconds", 0);

                String text = skill + " • " + format(seconds) + " • " + date;
                if (!topic.isEmpty()) {
                    text += " • " + topic;
                }

                TextView sessionText = tv(text, 13, TEXT);
                item.addView(sessionText);
                sessionsBox.addView(item);
                count++;
            }

            if (a.length() == 0) {
                sessionsBox.addView(tv("No sessions yet.", 13, MUTED));
            }
        } catch (Exception ignored) {
            sessionsBox.addView(tv("No sessions yet.", 13, MUTED));
        }
    }

    void addSkill() {
        final EditText input = new EditText(this);
        input.setHint("Skill name");
        input.setSingleLine(true);

        new AlertDialog.Builder(this)
                .setTitle("Add skill")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty() && !skills.contains(name)) {
                        skills.add(name);
                        saveSkills();
                        ArrayAdapter<String> adapter = (ArrayAdapter<String>) skillSpinner.getAdapter();
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                        refresh();
                    } else {
                        Toast.makeText(this, "Skill already exists or is empty.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(tick);
        super.onDestroy();
    }
}
