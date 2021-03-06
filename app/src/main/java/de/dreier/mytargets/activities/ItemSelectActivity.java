/*
 * MyTargets Archery
 *
 * Copyright (C) 2015 Florian Dreier
 * All rights reserved
 */
package de.dreier.mytargets.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.View;

import de.dreier.mytargets.R;
import de.dreier.mytargets.databinding.LayoutFrameFabBinding;
import de.dreier.mytargets.fragments.ArrowFragment;
import de.dreier.mytargets.fragments.BowFragment;
import de.dreier.mytargets.fragments.DistanceFragment;
import de.dreier.mytargets.fragments.EnvironmentFragment;
import de.dreier.mytargets.fragments.FragmentBase;
import de.dreier.mytargets.fragments.TargetFragment;
import de.dreier.mytargets.fragments.WindDirectionFragment;
import de.dreier.mytargets.fragments.WindSpeedFragment;

public abstract class ItemSelectActivity extends SimpleFragmentActivityBase
        implements FragmentBase.OnItemSelectedListener, FragmentBase.ContentListener {

    public static final String ITEM = "item";
    public static final String INTENT = "intent";
    private LayoutFrameFabBinding fabBinding;

    @Override
    protected int getLayoutResource() {
        return R.layout.layout_frame_fab;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO Move the fab logic to EditableFragmentBase

        fabBinding = (LayoutFrameFabBinding) binding;

        if (childFragment instanceof View.OnClickListener) {
            fabBinding.fabLayout.fab.setOnClickListener(((View.OnClickListener) childFragment));
        }
        onContentChanged(true, 0);
    }

    @Override
    public void onContentChanged(boolean empty, int stringRes) {
        if (stringRes != 0 && fabBinding.fabLayout.newText != null && fabBinding.fabLayout.newLayout != null) {
            fabBinding.fabLayout.newText.setVisibility(empty ? View.VISIBLE : View.GONE);
            fabBinding.fabLayout.newText.setText(stringRes);
        }
        if (this instanceof View.OnClickListener) {
            fabBinding.fabLayout.fab.setVisibility(View.VISIBLE);
        } else {
            fabBinding.fabLayout.fab.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemSelected(Parcelable item) {
        Intent data = new Intent();
        data.putExtra(ITEM, item);
        data.putExtra(INTENT, getIntent() != null ? getIntent().getExtras() : null);
        setResult(RESULT_OK, data);
        onBackPressed();
    }

    public static class ArrowActivity extends ItemSelectActivity {

        @Override
        public Fragment instantiateFragment() {
            return new ArrowFragment();
        }
    }

    public static class BowActivity extends ItemSelectActivity {

        @Override
        public Fragment instantiateFragment() {
            return new BowFragment();
        }
    }

    public static class DistanceActivity extends ItemSelectActivity {
        @Override
        protected Fragment instantiateFragment() {
            return new DistanceFragment();
        }
    }

    public static class EnvironmentActivity extends ItemSelectActivity {
        @Override
        protected Fragment instantiateFragment() {
            return new EnvironmentFragment();
        }
    }

    public static class TargetActivity extends ItemSelectActivity {
        @Override
        protected Fragment instantiateFragment() {
            return new TargetFragment();
        }
    }

    public static class WindDirectionActivity extends ItemSelectActivity {
        @Override
        protected Fragment instantiateFragment() {
            return new WindDirectionFragment();
        }
    }

    public static class WindSpeedActivity extends ItemSelectActivity {
        @Override
        protected Fragment instantiateFragment() {
            return new WindSpeedFragment();
        }
    }
}
