/*
 * MyTargets Archery
 *
 * Copyright (C) 2015 Florian Dreier
 * All rights reserved
 */

package de.dreier.mytargets.activities;

import android.Manifest;

import android.content.Intent;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


import de.dreier.mytargets.R;
import de.dreier.mytargets.databinding.ActivityInputBinding;
import de.dreier.mytargets.fragments.TimerFragment;
import de.dreier.mytargets.managers.SettingsManager;
import de.dreier.mytargets.managers.WearMessageManager;
import de.dreier.mytargets.managers.dao.ArrowNumberDataSource;
import de.dreier.mytargets.managers.dao.PasseDataSource;
import de.dreier.mytargets.managers.dao.RoundDataSource;
import de.dreier.mytargets.managers.dao.RoundTemplateDataSource;
import de.dreier.mytargets.managers.dao.SightSettingDataSource;
import de.dreier.mytargets.managers.dao.StandardRoundDataSource;
import de.dreier.mytargets.managers.dao.TrainingDataSource;
import de.dreier.mytargets.models.EShowMode;
import de.dreier.mytargets.shared.models.NotificationInfo;
import de.dreier.mytargets.shared.models.Passe;
import de.dreier.mytargets.shared.models.Round;
import de.dreier.mytargets.shared.models.RoundTemplate;
import de.dreier.mytargets.shared.models.Shot;
import de.dreier.mytargets.shared.models.SightSetting;
import de.dreier.mytargets.shared.models.StandardRound;
import de.dreier.mytargets.shared.models.Training;
import de.dreier.mytargets.shared.utils.OnTargetSetListener;
import de.dreier.mytargets.shared.utils.StandardRoundFactory;
import de.dreier.mytargets.utils.ThumbnailUtils;
import de.dreier.mytargets.utils.ToolbarUtils;

import de.dreier.mytargets.utils.tUtils;


import icepick.Icepick;
import icepick.State;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;


@RuntimePermissions
public class InputActivity extends ChildActivityBase implements OnTargetSetListener {
    private static final String LOGTAG = "Tirpitz";
    public static final String ROUND_ID = "round_id";
    public static final String PASSE_IND = "passe_ind";

    /**
     * Zero-based index of the currently displayed passe.
     */
    @State
    int curPasse = 0;

    private int savedPasses = 0;
    private Round round;
    private WearMessageManager manager;
    private Training training;
    private RoundTemplate template;
    private ArrayList<Round> rounds;
    private PasseDataSource passeDataSource;
    private StandardRound standardRound;
    private File endImage;
    private ActivityInputBinding binding;
    private Boolean hasImage = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_input);

        binding.targetView.setOnTargetSetListener(this);

        RoundDataSource roundDataSource = new RoundDataSource();
        passeDataSource = new PasseDataSource();

        EasyImage.configuration(this)
                .setImagesFolderName("Ends");

        Intent intent = getIntent();
        assert intent != null;

        long roundId = intent.getLongExtra(ROUND_ID, -1);
        curPasse = intent.getIntExtra(PASSE_IND, -1);
        round = roundDataSource.get(roundId);
        template = round.info;
        training = new TrainingDataSource().get(round.trainingId);
        standardRound = new StandardRoundDataSource().get(training.standardRoundId);
        rounds = roundDataSource.getAll(round.trainingId);
        savedPasses = passeDataSource.getAllByRound(roundId).size();

        binding.targetView.setRoundTemplate(template);
        if (training.arrowNumbering) {
            binding.targetView.setArrowNumbers(new ArrowNumberDataSource().getAll(training.arrow));
        }

        // Send message to wearable app, that we are starting a passe
        new Thread(this::startWearNotification).start();

        Icepick.restoreInstanceState(this, savedInstanceState);
        if (savedInstanceState != null) {
            updatePasse();
        } else {
            setPasse(curPasse);
        }

        binding.next.setOnClickListener(view -> setPasse(curPasse + 1));
        binding.prev.setOnClickListener(view -> setPasse(curPasse - 1));


        ToolbarUtils.showHomeAsUp(this);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        InputActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
    private void startWearNotification() {
        NotificationInfo info = buildInfo();
        manager = new WearMessageManager(this, info);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (manager != null) {
            manager.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.passe, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem eye = menu.findItem(R.id.action_show_all);
        eye.setVisible(!binding.targetView.getInputMode());
        final MenuItem showSidebar = menu.findItem(R.id.action_show_sidebar);
        showSidebar.setIcon(binding.targetView.getInputMode() ? R.drawable.ic_album_24dp :
                R.drawable.ic_grain_24dp);

        showSidebar.setVisible(curPasse >= savedPasses);
        switch (SettingsManager.getShowMode()) {
            case END:
                menu.findItem(R.id.action_show_end).setChecked(true);
                break;
            case ROUND:
                menu.findItem(R.id.action_show_round).setChecked(true);
                break;
            case TRAINING:
                menu.findItem(R.id.action_show_training).setChecked(true);
                break;
        }

        if (hasImage) {
            //tUtils.dl("end has an image");
            String p = endImage.getPath();
            //tUtils.dl(p);
            Bitmap b = BitmapFactory.decodeFile(p);
            //tUtils.dl(b.toString());
            Resources res = getResources();
            //tUtils.dl(res.toString());
            BitmapDrawable icon = new BitmapDrawable(res,b);
            //tUtils.dl(icon.toString());
            menu.findItem(R.id.action_add_photo).setIcon(icon);


        }
        return true;
    }

    /**
     * loads existing end or creates new one from template.
     * @param passe : end number
     */
    private void setPasse(int passe) {
        if (passe >= template.passes) {
            if (rounds.size() > template.index + 1) {
                // Go to next round if current round is finished
                round = rounds.get(template.index + 1);
                template = round.info;
                passe = 0;
                savedPasses = passeDataSource.getAllByRound(round.getId()).size();
            } else if (savedPasses <= curPasse && standardRound.club != StandardRoundFactory.CUSTOM_PRACTICE) {
                // If standard round is over exit the input activity
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                return;
            }
        } else if (passe == -1 && template.index > 0) {
            // If we navigate backwards and go past the beginning of lets say round 2
            // -> go to the last passe of round 1
            round = rounds.get(template.index - 1);
            template = round.info;
            passe = template.passes - 1;
            savedPasses = template.passes;
        }
        if (passe < savedPasses) {
            // If the passe is already saved load it from the database
            Passe p = passeDataSource.get(round.getId(), passe);
            if (p != null) {
                binding.targetView.setPasse(p);
            } else {
                binding.targetView.reset();
                binding.targetView.setRoundId(round.getId());
            }

            hasImage = p.hasPhoto;
            tUtils.dl("pass has photo: " + p.getPhotoStatus().toString());
            if(p.getPhotoStatus()) {
                //photo has been previously linked
                LinkedList<String> u = p.getPhotoUris();
                while (!u.isEmpty() && !(new File(u.get(0)).exists())) {
                    //list is not empty, but first entry does not exist
                    Log.d(LOGTAG, "file was not found, it may have been deleted by another app or by the user.");
                    u = p.unlinkPhoto(0);
                }
                if (!u.isEmpty()){
                    //weve found a valid file in the list
                    Log.d(LOGTAG, "image found...");
                    endImage = new File(u.get(0));
                    hasImage = true;
                    //done:load image thumbnail to menu bar
                }
                else {
                    //end used to have images but they have all been removed
                    hasImage = false;

                }

            }
        } else {
            // otherwise create a new one
            if (passe != curPasse) {
                binding.targetView.reset();
                binding.targetView.setRoundId(round.getId());
            }
            if (training.timePerPasse > 0) {
                openTimer();
            }
            hasImage = false;
            endImage = null;
        }


        ArrayList<Passe> oldOnes = passeDataSource.getAllByTraining(training.getId());
        binding.targetView.setOldShoots(oldOnes);
        curPasse = passe;
        updatePasse();

        supportInvalidateOptionsMenu();
    }

    /**
     * updates end ID
     */
    private void updatePasse() {
        binding.prev.setEnabled(curPasse > 0 || template.index > 0);
        binding.next.setEnabled(curPasse < savedPasses &&
                (curPasse + 1 < template.passes || // The current round is not finished
                        rounds.size() > template.index + 1 || // We still have another round
                        standardRound.club == StandardRoundFactory.CUSTOM_PRACTICE)); // or we don't have an exit condition
        //binding.photo.setEnabled();

        ToolbarUtils.setTitle(this, getString(R.string.passe) + " " + (curPasse + 1));
        ToolbarUtils.setSubtitle(this, getString(R.string.round) + " " + (round.info.index + 1));
        invalidateOptionsMenu();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_end:
                item.setChecked(true);
                binding.targetView.setShowMode(EShowMode.END);
                return true;
            case R.id.action_show_round:
                item.setChecked(true);
                binding.targetView.setShowMode(EShowMode.ROUND);
                return true;
            case R.id.action_show_training:
                item.setChecked(true);
                binding.targetView.setShowMode(EShowMode.TRAINING);
                return true;
            case R.id.action_show_sidebar:
                binding.targetView.switchMode(!binding.targetView.getInputMode(), true);
                supportInvalidateOptionsMenu();
                return true;
            /**
             * launches camera to add photo of end
             */
            case R.id.action_add_photo:
                if (hasImage){
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    tUtils.dl(endImage.getPath());
                    intent.setDataAndType(Uri.parse("file://"+ endImage.getPath()), "image/*");
                    startActivity(intent);
                }else {
                    InputActivityPermissionsDispatcher.onCameraButtonWithCheck(this);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //todo: add end photo viewer
    private void openTimer() {
        Intent intent = new Intent(this, SimpleFragmentActivityBase.TimerActivity.class);
        intent.putExtra(TimerFragment.SHOOTING_TIME, training.timePerPasse);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    /**
     * called when end data entry is completed
     * @param passe, the current end object
     * @param remote
     * @return: returns end ID.
     */
    @Override
    public long onEndFinished(Passe passe, boolean remote) {
        // Only sort shots when all arrows are on one target face
        if (template.target.getModel().getFaceCount() == 1) {
            passe.sort();
        }

        // Change round template if passe is out of range defined in template
        if (standardRound.club == StandardRoundFactory.CUSTOM_PRACTICE && template.passes <= curPasse) {
            new RoundTemplateDataSource().addPasse(template);
        }

        passe.roundId = round.getId();


        if (curPasse >= savedPasses || remote) {
            savedPasses++;
            if (manager != null) {
                manager.sendMessageUpdate(buildInfo());
            }
            if (remote) {
                curPasse = savedPasses;
            }
        } else if (curPasse + 1 == savedPasses && manager != null) {
            manager.sendMessageUpdate(buildInfo());
        }
        //todo: update order when end is edited, order in list view is not changed to reflect High to low order

        if (endImage != null) {
            passe.linkPhoto(Uri.fromFile(endImage).getPath());

        }
        passeDataSource.update(passe);
        runOnUiThread(this::updatePasse);
        return passe.getId();
    }

    /**
     *
     * @return
     */
    private NotificationInfo buildInfo() {
        String title = getString(R.string.passe) + " " + (savedPasses + 1);
        String text = "";

        // Initialize message text
        Passe lastPasse = passeDataSource.get(round.getId(), savedPasses);
        if (lastPasse != null) {
            for (Shot shot : lastPasse.shot) {
                text += template.target.zoneToString(shot.zone, 0) + " ";
            }
            text += "\n";
        } else {
            title = getString(R.string.my_targets);
        }

        // Load bow settings
        if (training.bow > 0) {
            final SightSetting sightSetting = new SightSettingDataSource().get(training.bow,
                    template.distance);
            if (sightSetting != null) {
                text += String.format("%s: %s", template.distance, sightSetting.value);
            }
        }

        return new NotificationInfo(round, title, text);
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    public void onCameraButton() {
        EasyImage.openCamera(this, 0);
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void onSelectImage() {
        EasyImage.openGallery(this, 0);
    }

    public void onCanceled(EasyImage.ImageSource source, int type) {
        //Cancel handling, you might wanna remove taken photo if it was canceled
        if (source == EasyImage.ImageSource.CAMERA) {
            File photoFile = EasyImage.lastlyTakenButCanceledPhoto(InputActivity.this);
            if (photoFile != null) photoFile.delete();
        }
    }

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                //Handle the image
                onPhotoReturned(imageFile);
            }
            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                //Cancel handling, you might wanna remove taken photo if it was canceled
                if (source == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(InputActivity.this);
                    if (photoFile != null) photoFile.delete();
                }
            }
        });
    }

    /**
     *photo was successfully returned from the camera
     * @param image: File object of image
     */
    private void onPhotoReturned(File image){
        tUtils.dl("photo has been returned...");
        //Log.d(LOGTAG,image.getName());
        //Log.d(LOGTAG,image.getPath());
        //Log.d(LOGTAG,image.getParent());
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fName = image.getParent() + File.separator +
                timeStamp +
                "_" + training.title +
                "_" + Integer.toString(round.info.index+1) +
                "_" + Integer.toString(curPasse+1) +
                ".jpg";
        //Log.d(LOGTAG,fName);
        File nImage = new File(fName);
        image.renameTo(nImage);
        //Log.d(LOGTAG,image.getName());

        endImage = nImage;
        hasImage = true;
        invalidateOptionsMenu();
        /**
         * end is already complete - > call onEndFinished
         */
        if (isComplete()){

            onEndFinished(passeDataSource.get(round.getId(), curPasse),false);
        }
        tUtils.dl(endImage.getName());




    }

    /**
     * returns boolean whether all arrows have been entered. ie data entry is complete
     * @return
     */
    private boolean isComplete(){
        int templateArrowCount = template.arrowsPerPasse ;
        int arrowCount = passeDataSource.get(round.getId(), curPasse).shotList().size();
        tUtils.dl("there should be " + templateArrowCount + " arrows");
        tUtils.dl("there are " + arrowCount + " arrows");
        return templateArrowCount == arrowCount;
    }

}
