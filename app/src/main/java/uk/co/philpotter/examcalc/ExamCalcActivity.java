/*
 * Simple activity to allow timing of exams to help invigilation.
 * Copyright Phil Potter, 2019, licensed under GPLv3, see LICENSE for details.
 */
package uk.co.philpotter.examcalc;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import java.io.File;

public class ExamCalcActivity extends FragmentActivity {

    // Instance variables
    private ExamStartFragment esm;
    private MainFragment mf;

    /**
     * This method is called on creation of the activity.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.examcalc_layout);
        esm = new ExamStartFragment();
        mf = new MainFragment();
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        File stateFile = new File(getFilesDir() + "/" + MainFragment.stateFileName);
        if (stateFile.exists())
            t.add(R.id.examcalc_fragment, mf);
        else
            t.add(R.id.examcalc_fragment, esm);
        t.commit();
    }

    /**
     * This method switches the fragment over to the main app.
     */
    public void switchToMainApp() {
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.remove(esm);
        t.add(R.id.examcalc_fragment, mf);
        t.commit();
    }

    /**
     * This method switches the fragment back to the start and wipes all exam state.
     */
    public void endExam() {
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.remove(mf);
        t.add(R.id.examcalc_fragment, esm);
        t.commit();
    }
}
