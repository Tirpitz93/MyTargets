/*
 * MyTargets Archery
 *
 * Copyright (C) 2015 Florian Dreier
 * All rights reserved
 */

package de.dreier.mytargets.activities;

import android.media.MediaRouter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import de.dreier.mytargets.R;
import de.dreier.mytargets.fragments.EditArrowFragment;
import de.dreier.mytargets.fragments.EditBowFragment;
import de.dreier.mytargets.fragments.EditRoundFragment;
import de.dreier.mytargets.fragments.EditStandardRoundFragment;
import de.dreier.mytargets.fragments.EditTrainingFragment;
import de.dreier.mytargets.fragments.SettingsFragment;
import de.dreier.mytargets.fragments.TimerFragment;
import de.dreier.mytargets.fragments.TrainingFragment;

public abstract class SimpleFragmentActivity extends AppCompatActivity {

    protected Fragment childFragment;

    protected abstract Fragment instantiateFragment();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
        childFragment = instantiateFragment();
        Bundle bundle = getIntent() != null ? getIntent().getExtras() : null;
        childFragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content, childFragment).commit();
    }

    protected int getLayoutResource() {
        return R.layout.layout_frame;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    public static class TimerActivity extends SimpleFragmentActivity {

        @Override
        public Fragment instantiateFragment() {
            return new TimerFragment();
        }
    }

    public static class SettingsActivity extends SimpleFragmentActivity {

        @Override
        public Fragment instantiateFragment() {
            return new SettingsFragment();
        }
    }

    public static class TrainingActivity extends SimpleFragmentActivity {

        @Override
        public Fragment instantiateFragment() {
            return new TrainingFragment();
        }
    }

    public static class EditTrainingActivity extends SimpleFragmentActivity {

        @Override
        protected Fragment instantiateFragment() {
            return new EditTrainingFragment();
        }
    }
    public static class EditRoundActivity extends SimpleFragmentActivity {

        @Override
        protected Fragment instantiateFragment() {
            return new EditRoundFragment();
        }
    }

    public static class EditStandardRoundActivity extends SimpleFragmentActivity {

        @Override
        protected Fragment instantiateFragment() {
            return new EditStandardRoundFragment();
        }
    }

    public static class EditBowActivity extends SimpleFragmentActivity {

        @Override
        protected Fragment instantiateFragment() {
            return new EditBowFragment();
        }
    }

    public static class EditArrowActivity extends SimpleFragmentActivity {

        @Override
        protected Fragment instantiateFragment() {
            return new EditArrowFragment();
        }
    }
}