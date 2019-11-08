package com.example.expensehelper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Location;

import android.net.Uri;
import android.os.Bundle;

//import android.support.annotation.NonNull;



//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
//import com.google.firebase.ml.vision.label.FirebaseVisionLabel;
//import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.squareup.picasso.Picasso;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraView;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CameraKitUtility.CameraFunctionHandler {

    private Boolean isCameraMode;
    private Boolean canTakePicture;
    //private Bitmap mBitmap;
    private Boolean isCamFacingFront;


    @BindView(R.id.cam_activity_cameraview) CameraView mCameraView;
    @BindView(R.id.cam_activity_cam_trigger) ImageView mCameraTrigger;
    @BindView(R.id.cam_activity_confirm_layout) ConstraintLayout mConfirmPhotoLayout;
    @BindView(R.id.cam_activity_confirm_image) ImageView mConfirmPhotoImageView;
    @BindView(R.id.cam_activity_upload_progress) ProgressBar mUploadProgress;
    @BindView(R.id.cam_activity_progress_text) TextView mUploadProgressTextView;

    @BindView(R.id.cam_activity_cam_layout) ConstraintLayout mCameraLayout;
    @BindView(R.id.cam_activity_flash_off) ImageView mFlashOff;
    @BindView(R.id.cam_activity_flash_on) ImageView mFlashOn;
    @BindView(R.id.cam_activity_cam_rotate) ImageView mCamRotate;



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setTitle("Camera");

        FirebaseApp.initializeApp(this);

        // setting inital values
        isCameraMode = true;
        canTakePicture = false;
        isCamFacingFront = false;

        // setting on button clicked listener
        mCameraTrigger.setOnClickListener(this);
        mFlashOff.setOnClickListener(this);
        mFlashOn.setOnClickListener(this);
        mFlashOff.setOnClickListener(this);
        mCamRotate.setOnClickListener(this);
        //mRotateLeft.setOnClickListener(this);
        //mRotateRight.setOnClickListener(this);

        // set camera event listener
        mCameraView.addCameraKitListener(new CameraKitUtility(this));


    }



    ///////////////////////////////////////


    @Override
    protected void onResume() {
        super.onResume();

        // start camera
        if (isCameraMode) {
            mCameraView.start();
        }



    }


    @Override
    protected void onPause() {
        super.onPause();

        // stop camera
        mCameraView.stop();

    }





    // When buttons are clicked.
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cam_activity_cam_trigger:
                capturePhotoIfReady();
                break;


            case R.id.cam_activity_flash_off:
                // turn flash on
                mFlashOff.setVisibility(View.INVISIBLE);
                mFlashOn.setVisibility(View.VISIBLE);
                mCameraView.setMethod(CameraKit.Constants.METHOD_STANDARD);
                mCameraView.setFlash(CameraKit.Constants.FLASH_ON);
                Timber.d("flash off clicked");
                break;

            case R.id.cam_activity_flash_on:
                mFlashOn.setVisibility(View.INVISIBLE);
                mFlashOff.setVisibility(View.VISIBLE);
                mCameraView.setMethod(CameraKit.Constants.METHOD_STILL);
                mCameraView.setFlash(CameraKit.Constants.FLASH_OFF);
                Timber.d("flash on clicked");
                break;

            case R.id.cam_activity_cam_rotate:
                if (isCamFacingFront == false) {
                    mCameraView.setFacing(CameraKit.Constants.FACING_FRONT);
                    isCamFacingFront = true;
                } else {
                    mCameraView.setFacing(CameraKit.Constants.FACING_BACK);
                    isCamFacingFront = false;
                }
                break;



            default:
                break;
        }

    }




    private void capturePhotoIfReady() {
        if (canTakePicture == true) {
            showProgressBar();
            Timber.d("taking image");
            mCameraView.captureImage();
        } else {
            Toast.makeText(MainActivity.this, "Camera initating, please try again in a few seconds.", Toast.LENGTH_SHORT).show();
        }

    }

    private void hideUploadCancelSwitch() {
        //mUploadButton.setVisibility(View.INVISIBLE);
        //mCancelButton.setVisibility(View.INVISIBLE);
        //mPrivacySwitch.setVisibility(View.INVISIBLE);
        //mPublicText.setVisibility(View.INVISIBLE);
        //mPrivateText.setVisibility(View.INVISIBLE);
    }

    private void showProgressBar() {
        mUploadProgress.setVisibility(View.VISIBLE);
        mUploadProgressTextView.setVisibility(View.VISIBLE);
    }



    private void backToCameraMode() {
        mCameraView.start();
        isCameraMode = true;

        // show cameras
        mCameraLayout.setVisibility(View.VISIBLE);

        // hide confirm image layout
        mConfirmPhotoLayout.setVisibility(View.INVISIBLE);
        hideProgressBar();


        // show upload or cancel buttons and privacy switch
        //mUploadButton.setVisibility(View.VISIBLE);
        //mCancelButton.setVisibility(View.VISIBLE);
        //mPrivacySwitch.setVisibility(View.VISIBLE);
        //mPublicText.setVisibility(View.VISIBLE);
        //mPrivateText.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        // hide progress bars
        mUploadProgressTextView.setVisibility(View.INVISIBLE);
        mUploadProgress.setVisibility(View.INVISIBLE);
    }

    private static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }


    /////////////////////////////////////////////////////////////////////



    // Camera event listener call back /////////////////////////////////////////////////////////////////////
    @Override
    public void onIsCameraReady(Boolean isReady) {
        canTakePicture = isReady;
    }


    @Override
    public void onImageCaptured(CameraKitImage cameraKitImage) {
        goToConfirmImageMode(cameraKitImage);
        Timber.d("image capture success");

    }

    private void goToConfirmImageMode(CameraKitImage cameraKitImage) {
        Bitmap originalImage = cameraKitImage.getBitmap();
        Bitmap compressedImage = CompressBitmapUtil.getResizedBitmap(cameraKitImage.getBitmap(), 500);

        // turn off camera
        mCameraView.stop();
        isCameraMode = false;

        // hide camera layout
        mCameraLayout.setVisibility(View.INVISIBLE);

        // show confirm image layout
        mConfirmPhotoLayout.setVisibility(View.VISIBLE);

        // display captured photo
        //RequestOptions options = new RequestOptions();
        //options.dontAnimate();

        mConfirmPhotoImageView.setImageBitmap(compressedImage);

        /*
        Glide.with(MainActivity.this)
                .load(originalImage)
                //.apply(options)
                .into(mConfirmPhotoImageView);
        */

        hideProgressBar();


        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(originalImage);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();

        detector.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        processTextRecognitionResult(firebaseVisionText);
                        backToCameraMode();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Timber.d("nigga this failed");
                        backToCameraMode();

                    }
                });



    }

    private void processTextRecognitionResult(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();
        if (blocks.size() == 0) {
            Timber.d("No text found");
            return;
        }


        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();

                Timber.d(lines.get(j).getText());

            }

        }
        return;
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }
    */


    /////////////////////////////////////////////////////////////////////////////////////////////////////////



}