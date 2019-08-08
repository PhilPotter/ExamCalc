/*
 * This fragment is presented upon starting a new exam.
 * Copyright Phil Potter, 2019, licensed under GPLv3, see LICENSE for details.
 */
package uk.co.philpotter.examcalc;

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.os.Bundle;
import android.widget.Button;

public class ExamStartFragment extends Fragment {

    /**
     * This creates the fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate fragment layout
        View v = inflater.inflate(R.layout.examcalc_startfragment_layout, container, false);

        // Add listener to start button
        Button startButton = (Button)v.findViewById(R.id.start_button);
        startButton.setOnClickListener(new StartButtonListener());

        return v;
    }

    private class StartButtonListener implements View.OnClickListener {
        /**
         * This code runs when the start button is pressed.
         */
        @Override
        public void onClick(View v) {
            ExamCalcActivity eca = (ExamCalcActivity)getActivity();
            eca.switchToMainApp();
        }
    }
}
