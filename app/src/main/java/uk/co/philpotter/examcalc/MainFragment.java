/*
 * This fragment is presented upon starting a new exam.
 * Copyright Phil Potter, 2019, licensed under GPLv3, see LICENSE for details.
 */
package uk.co.philpotter.examcalc;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.content.DialogInterface;
import java.util.ArrayList;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import android.text.InputType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.Activity;
import android.widget.Switch;
import android.widget.CompoundButton;
import android.text.TextWatcher;
import android.text.Editable;
import android.util.Log;

public class MainFragment extends Fragment {

    // Where we store the app state
    public static final String stateFileName = "examcalc_state.txt";

    // Instance variables
    private Activity hostActivity;
    private ArrayList<Integer> breaks;
    private EditText minutesInBreak;
    private EditText startHour;
    private EditText startMinute;
    private EditText durationHours;
    private EditText durationMinutes;
    private EditText extraHours;
    private EditText extraMinutes;
    private TextView amPmLabel;
    private Switch amPmSwitch;
    private TextView endTime;
    private boolean saveStateOnExit;

    /**
     * This creates the fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Initialise breaks list
        breaks = new ArrayList<>();

        // Inflate fragment layout
        View v = inflater.inflate(R.layout.examcalc_mainfragment_layout, container, false);

        // Get activity and set statePath
        hostActivity = this.getActivity();

        // Set default save behaviour to true
        saveStateOnExit = true;

        // Add listener to add break button
        Button addBreakButton = (Button)v.findViewById(R.id.add_break_button);
        addBreakButton.setOnClickListener(new AddBreakButtonListener());

        // Add listener to end button
        Button endButton = (Button)v.findViewById(R.id.end_button);
        endButton.setOnClickListener(new EndButtonListener());

        // Set input field parameters
        startHour = v.findViewById(R.id.start_hour);
        startMinute = v.findViewById(R.id.start_minute);
        amPmLabel = v.findViewById(R.id.am_pm_label);
        amPmLabel.setText(R.string.am_pm_am_switch);
        amPmSwitch = v.findViewById(R.id.am_pm_switch);
        durationHours = v.findViewById(R.id.duration_hours);
        durationMinutes = v.findViewById(R.id.duration_minutes);
        extraHours = v.findViewById(R.id.extra_hours);
        extraMinutes = v.findViewById(R.id.extra_minutes);
        endTime = v.findViewById(R.id.end_time_textview);
        startHour.setSelectAllOnFocus(true);
        startMinute.setSelectAllOnFocus(true);
        durationHours.setSelectAllOnFocus(true);
        durationMinutes.setSelectAllOnFocus(true);
        extraHours.setSelectAllOnFocus(true);
        extraMinutes.setSelectAllOnFocus(true);
        startHour.setImeOptions(EditorInfo.IME_ACTION_DONE);
        startMinute.setImeOptions(EditorInfo.IME_ACTION_DONE);
        durationHours.setImeOptions(EditorInfo.IME_ACTION_DONE);
        durationMinutes.setImeOptions(EditorInfo.IME_ACTION_DONE);
        extraHours.setImeOptions(EditorInfo.IME_ACTION_DONE);
        extraMinutes.setImeOptions(EditorInfo.IME_ACTION_DONE);

        // Set listeners
        amPmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            // Just update end time regardless
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    amPmLabel.setText(R.string.am_pm_pm_switch);
                else
                    amPmLabel.setText(R.string.am_pm_am_switch);
                updateEndTime();
            }
        });
        TimeWatcher timeWatcher = new TimeWatcher();
        startHour.addTextChangedListener(timeWatcher);
        startMinute.addTextChangedListener(timeWatcher);
        durationHours.addTextChangedListener(timeWatcher);
        durationMinutes.addTextChangedListener(timeWatcher);
        extraHours.addTextChangedListener(timeWatcher);
        extraMinutes.addTextChangedListener(timeWatcher);

        return v;
    }

    /**
     * Called after onCreateView, when we are starting.
     */
    @Override
    public void onStart() {
        super.onStart();

        // Restore previous state if present
        File stateFile = new File(hostActivity.getFilesDir() + "/" + MainFragment.stateFileName);

        // Open state file
        BufferedReader stateReader = null;
        try {
            stateReader = new BufferedReader(new FileReader(stateFile));
            String temp;
            while ((temp = stateReader.readLine()) != null) {
                if (temp.isEmpty())
                    break;
                String setting[] = temp.split("=", 0);
                if (setting[0].equals("start_hour")) {
                    startHour.setText(setting[1]);
                }
                else if (setting[0].equals("start_minute")) {
                    startMinute.setText(setting[1]);
                }
                else if (setting[0].equals("am_pm_switch")) {
                    if (setting[1].equals("0")) {
                        amPmLabel.setText(R.string.am_pm_am_switch);
                        amPmSwitch.setChecked(false);
                    }
                    else {
                        amPmLabel.setText(R.string.am_pm_pm_switch);
                        amPmSwitch.setChecked(true);
                    }
                }
                else if (setting[0].equals("duration_hours")) {
                    durationHours.setText(setting[1]);
                }
                else if (setting[0].equals("duration_minutes")) {
                    durationMinutes.setText(setting[1]);
                }
                else if (setting[0].equals("extra_hours")) {
                    extraHours.setText(setting[1]);
                }
                else if (setting[0].equals("extra_minutes")) {
                    extraMinutes.setText(setting[1]);
                }
                else if (setting[0].equals("break")) {
                    int minutes = Integer.parseInt(setting[1]);
                    breaks.add(minutes);

                    // Fetch host layout that stores break rows
                    LinearLayout breaksList = hostActivity.findViewById(R.id.breaks_view);

                    // Formulate breaks string and view
                    String breakStr = "Break " + breaks.size() + ": " + minutes + " minutes long";
                    TextView breakRow = (TextView)hostActivity.getLayoutInflater().inflate(R.layout.breaks_row_layout, null);
                    breakRow.setText(breakStr);
                    breaksList.addView(breakRow);
                }
            }
        }
        catch (FileNotFoundException e) {
            Log.v("MainFragment", "State file doesn't exist yet: " + e);
        }
        catch (IOException e) {
            Log.e("MainFragment", "Problem reading state file: " + e);
        }
        finally {
            // Close state file
            try {
                if (stateReader != null)
                    stateReader.close();
            }
            catch (IOException e) {
                Log.e("MainFragment", "Couldn't close state file: " + e);
            }
        }

        // Update state at end of init phase
        updateEndTime();
    }

    /**
     * Called before we exit.
     */
    @Override
    public void onStop() {
        // Save state (or delete it) here
        if (saveStateOnExit) {
            // Delete state file
            File stateFile = new File(hostActivity.getFilesDir() + "/" + MainFragment.stateFileName);
            if (stateFile.exists()) {
                stateFile.delete();
            }

            // Store current state to file
            // Open state file
            BufferedWriter stateWriter = null;
            try {
                stateWriter = new BufferedWriter(new FileWriter(stateFile));

                // Get values
                String startHourStr = startHour.getText().toString();
                String startMinuteStr = startMinute.getText().toString();
                String durationHoursStr = durationHours.getText().toString();
                String durationMinutesStr = durationMinutes.getText().toString();
                String extraHoursStr = extraHours.getText().toString();
                String extraMinutesStr = extraMinutes.getText().toString();

                // Filter empty values
                if (startHourStr.isEmpty() || startHourStr.contains("-") || startHourStr.contains("."))
                    startHourStr = "12";
                if (startMinuteStr.isEmpty() || startMinuteStr.contains("-") || startMinuteStr.contains("."))
                    startMinuteStr = "00";
                if (durationHoursStr.isEmpty() || durationHoursStr.contains("-") || durationHoursStr.contains("."))
                    durationHoursStr = "0";
                if (durationMinutesStr.isEmpty() || durationMinutesStr.contains("-") || durationMinutesStr.contains("."))
                    durationMinutesStr = "0";
                if (extraHoursStr.isEmpty() || extraHoursStr.contains("-") || extraHoursStr.contains("."))
                    extraHoursStr = "0";
                if (extraMinutesStr.isEmpty() || extraMinutesStr.contains("-") || extraMinutesStr.contains("."))
                    extraMinutesStr = "0";

                String startHourState = "start_hour=" + startHourStr;
                stateWriter.write(startHourState);
                stateWriter.newLine();

                String startMinuteState = "start_minute=" + startMinuteStr;
                stateWriter.write(startMinuteState);
                stateWriter.newLine();

                String amPmSwitchState = "am_pm_switch=" + (amPmSwitch.isChecked() ? "1" : "0");
                stateWriter.write(amPmSwitchState);
                stateWriter.newLine();

                String durationHoursState = "duration_hours=" + durationHoursStr;
                stateWriter.write(durationHoursState);
                stateWriter.newLine();

                String durationMinutesState = "duration_minutes=" + durationMinutesStr;
                stateWriter.write(durationMinutesState);
                stateWriter.newLine();

                String extraHoursState = "extra_hours=" + extraHoursStr;
                stateWriter.write(extraHoursState);
                stateWriter.newLine();

                String extraMinutesState = "extra_minutes=" + extraMinutesStr;
                stateWriter.write(extraMinutesState);
                stateWriter.newLine();

                String temp;
                for (int i = 0; i < breaks.size(); ++i) {
                    temp = "break=" + breaks.get(i);
                    stateWriter.write(temp);
                    stateWriter.newLine();
                }
            }
            catch (IOException e) {
                Log.e("MainFragment", "Problem writing state file: " + e);
            }
            finally {
                // Close state file
                try {
                    if (stateWriter != null)
                        stateWriter.close();
                }
                catch (IOException e) {
                    Log.e("MainFragment", "Couldn't close state file: " + e);
                }
            }
        }
        else {
            // Delete state file
            File stateFile = new File(hostActivity.getFilesDir() + "/" + MainFragment.stateFileName);
            if (stateFile.exists()) {
                stateFile.delete();
            }
        }

        super.onStop();
    }

    private class AddBreakButtonListener implements View.OnClickListener {
        /**
         * This code runs when the add break button is pressed.
         */
        @Override
        public void onClick(View v) {

            // Create alert dialogue box with number of minutes input inside.
            AlertDialog.Builder addBreakInput = new AlertDialog.Builder(hostActivity);
            addBreakInput.setTitle(R.string.add_break_input_title);

            minutesInBreak = new EditText(hostActivity);
            minutesInBreak.setSelectAllOnFocus(true);
            minutesInBreak.setInputType(InputType.TYPE_CLASS_NUMBER);
            minutesInBreak.setText("0");
            addBreakInput.setView(minutesInBreak);

            addBreakInput.setNegativeButton(R.string.add_break_negative_button, null);
            addBreakInput.setPositiveButton(R.string.add_break_positive_button, new AddBreakListener());
            addBreakInput.show();
        }
    }

    private class AddBreakListener implements DialogInterface.OnClickListener {
        /**
         * This code runs when we choose to definitely add a break
         */
        @Override
        public void onClick(DialogInterface d, int i) {
            // Get minutes from dialogue field and add to breaks list
            int minutes = Integer.parseInt(minutesInBreak.getText().toString());
            if (minutes > 0) {
                breaks.add(minutes);

                // Fetch host layout that stores break rows
                LinearLayout breaksList = hostActivity.findViewById(R.id.breaks_view);

                // Formulate breaks string and view
                String breakStr = "Break " + breaks.size() + ": " + minutes + " minutes long";
                TextView breakRow = (TextView) hostActivity.getLayoutInflater().inflate(R.layout.breaks_row_layout, null);
                breakRow.setText(breakStr);
                breaksList.addView(breakRow);

                updateEndTime();
            }
        }
    }

    private class EndButtonListener implements View.OnClickListener {
        /**
         * This code runs when the end button is pressed.
         */
        @Override
        public void onClick(View v) {

            // Create alert dialogue box
            AlertDialog.Builder quitChooser = new AlertDialog.Builder(hostActivity);
            quitChooser.setTitle(R.string.end_confirmation_title);
            quitChooser.setMessage(R.string.end_confirmation_message);
            quitChooser.setNegativeButton(R.string.end_negative_button, new EndExamListener());
            quitChooser.setPositiveButton(R.string.end_positive_button, null);
            quitChooser.show();
        }
    }

    private class EndExamListener implements DialogInterface.OnClickListener {
        /**
         * This code runs when we choose to definitely end the exam.
         */
        @Override
        public void onClick(DialogInterface d, int i) {
            saveStateOnExit  = false;
            ExamCalcActivity eca = (ExamCalcActivity)getActivity();
            eca.endExam();
        }
    }

    private class TimeWatcher implements TextWatcher {
        // This listener just calls the update end time routine

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //
        }
        @Override
        public void afterTextChanged(Editable s) {
            updateEndTime();
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //
        }
    }

    /**
     * This method updates the end time by calculating it from all available data.
     */
    private void updateEndTime() {
        // Get values
        String startHourStr = startHour.getText().toString();
        String startMinuteStr = startMinute.getText().toString();
        String durationHoursStr = durationHours.getText().toString();
        String durationMinutesStr = durationMinutes.getText().toString();
        String extraHoursStr = extraHours.getText().toString();
        String extraMinutesStr = extraMinutes.getText().toString();

        // Filter empty values
        if (startHourStr.isEmpty() || startHourStr.contains("-") || startHourStr.contains("."))
            startHourStr = "12";
        if (startMinuteStr.isEmpty() || startMinuteStr.contains("-") || startMinuteStr.contains("."))
            startMinuteStr = "0";
        if (startMinuteStr.length() < 2)
            startMinuteStr = "0" + startMinuteStr;
        if (durationHoursStr.isEmpty() || durationHoursStr.contains("-") || durationHoursStr.contains("."))
            durationHoursStr = "0";
        if (durationMinutesStr.isEmpty() || durationMinutesStr.contains("-") || durationMinutesStr.contains("."))
            durationMinutesStr = "0";
        if (extraHoursStr.isEmpty() || extraHoursStr.contains("-") || extraHoursStr.contains("."))
            extraHoursStr = "0";
        if (extraMinutesStr.isEmpty() || extraMinutesStr.contains("-") || extraMinutesStr.contains("."))
            extraMinutesStr = "0";

        // Parse integers and write back start time if out of range
        int startHourVal = Integer.parseInt(startHourStr);
        if (startHourVal > 12) {
            startHourVal = 12;
            startHour.setText("12");
        }
        int startMinuteVal = Integer.parseInt(startMinuteStr);
        if (startMinuteVal > 59) {
            startMinuteVal = 59;
            startMinute.setText("59");
        }
        int durationHoursVal = Integer.parseInt(durationHoursStr);
        int durationMinutesVal = Integer.parseInt(durationMinutesStr);
        if (durationMinutesVal > 59) {
            durationMinutesVal = 59;
            durationMinutes.setText("59");
        }
        int extraHoursVal = Integer.parseInt(extraHoursStr);
        int extraMinutesVal = Integer.parseInt(extraMinutesStr);
        if (extraMinutesVal > 59) {
            extraMinutesVal = 59;
            extraMinutes.setText("59");
        }

        // Convert start time to 24 hour clock format
        if (amPmSwitch.isChecked()) {
            if (startHourVal != 12)
                startHourVal += 12;
        }
        else {
            if (startHourVal == 12)
                startHourVal = 0;
        }

        // Iterate breaks list and add this extra time in
        int breaksMinutesTotal = 0;
        for (int i = 0; i < breaks.size(); ++i)
            breaksMinutesTotal += breaks.get(i);

        // Calculate start time in minutes
        int startTimeInMinutes = startHourVal * 60 + startMinuteVal;

        // Add total exam time to this
        int endTimeInMinutes = startTimeInMinutes + breaksMinutesTotal +
                (durationHoursVal * 60 + durationMinutesVal) +
                (extraHoursVal * 60 + extraMinutesVal);

        // Convert back to final time
        int endMinutesVal = endTimeInMinutes % 60;
        int endHoursVal = endTimeInMinutes / 60;
        endHoursVal %= 24;
        String amPmMarker = null;
        if (endHoursVal >= 12) {
            if (endHoursVal != 12)
                endHoursVal -= 12;
            amPmMarker = hostActivity.getString(R.string.am_pm_pm_switch);
        } else {
            if (endHoursVal == 0)
                endHoursVal += 12;
            amPmMarker = hostActivity.getString(R.string.am_pm_am_switch);
        }

        // Generate final string
        String endHoursStr = Integer.toString(endHoursVal);
        String endMinutesStr = Integer.toString(endMinutesVal);
        if (endMinutesStr.length() < 2)
            endMinutesStr = "0" + endMinutesStr;

        String finalEndTimeStr = endHoursStr + ":" + endMinutesStr + " " + amPmMarker;
        endTime.setText(finalEndTimeStr);
    }
}
