package com.example.expensehelper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Location;

import android.net.Uri;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
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

// aws dynamo import
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;

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

        // initialize AWS
        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {
                Timber.d("AWSMobileClient is instantiated and you are connected to AWS!");
            }
        }).execute();

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




    // BUTTON CLICK RESPONSE ///////////////////////////////////////////////////////
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



    // INTERFACE CONTROL FUNCTIONS ///////////////////////////////
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

    }

    private void hideProgressBar() {
        // hide progress bars
        mUploadProgressTextView.setVisibility(View.INVISIBLE);
        mUploadProgress.setVisibility(View.INVISIBLE);
    }


    // CAMERA CAPTURE OPERATION FUNCTIONS //////////////////////////////////////////////////
    private void capturePhotoIfReady() {
        if (canTakePicture == true) {
            showProgressBar();
            Timber.d("taking image");
            mCameraView.captureImage();
        } else {
            Toast.makeText(MainActivity.this, "Camera initating, please try again in a few seconds.", Toast.LENGTH_SHORT).show();
        }

    }

    // CAMERA EVENT LISTENER
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


    // TEXT RECOGNITION FUNCTIONS ////////////////////////////////////////////////////
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

        // display the captured photo
        mConfirmPhotoImageView.setImageBitmap(compressedImage);

        // turn off the loading icon
        hideProgressBar();

        // begin text recognition
        textRecognition(originalImage);


    }

    private void textRecognition(Bitmap originalImage) {
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
                    Toast.makeText(MainActivity.this, "Text recognition failed", Toast.LENGTH_SHORT).show();
                    backToCameraMode();

                }
            });
    }

    // use regex to process the results and only get informationw we want
    private void processTextRecognitionResult(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();
        if (blocks.size() == 0) {
            Toast.makeText(MainActivity.this, "No text were found!", Toast.LENGTH_SHORT).show();
            return;
        }

        Timber.d(firebaseVisionText.getText());

        // initiate ABN, prices, and date
        String abnNumber = "";
        ArrayList<Double> prices = new ArrayList<Double>();
        String purchaseDate = "";

        // loop through blocks to get each lines of text
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                String lineText = lines.get(j).getText();

                // if line contains ABN >>> get abn number
                if (lineText.contains("ABN") || lineText.contains("abn")) {
                    Timber.d("abn number: " + lineText);

                    // Regex to to grab only numbers
                    Pattern numbersOnly = Pattern.compile("\\d+");
                    Matcher abnMatch = numbersOnly.matcher(lineText);

                    while(abnMatch.find()) {
                        abnNumber = abnNumber + abnMatch.group();
                    }

                }


                // Regex to get price
                Pattern priceOnly = Pattern.compile("^\\$?\\d{1,3}(,\\d{3})*(\\.\\d{1,2})?$");
                Matcher priceMatch = priceOnly.matcher(lineText);
                while(priceMatch.find()) {
                    Double price = Double.parseDouble(priceMatch.group().replace("$", ""));
                    // make sure price is a reasonable number.

                    if (price <= 200.0) {
                        prices.add(price);
                    }
                }


                // Regex to get date
                Pattern dateOnly = Pattern.compile("^(?:(?:31(\\/|-|\\.)(?:0?[13578]|1[02]))\\1|(?:(?:29|30)(\\/|-|\\.)(?:0?[13-9]|1[0-2])\\2))(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$|^(?:29(\\/|-|\\.)0?2\\3(?:(?:(?:1[6-9]|[2-9]\\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))$|^(?:0?[1-9]|1\\d|2[0-8])(\\/|-|\\.)(?:(?:0?[1-9])|(?:1[0-2]))\\4(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$");
                Matcher dateMatch = dateOnly.matcher(lineText);
                while(dateMatch.find()) {
                    purchaseDate = dateMatch.group();
                    break;
                }
            }

        }

        try {
            Timber.d(abnNumber);
            Double totalPrice = Collections.max(prices);
            Timber.d(totalPrice + "");
            Timber.d(purchaseDate);

            Toast.makeText(MainActivity.this, "ABN:" + abnNumber + ",Price:" + totalPrice + ",Date:" + purchaseDate, Toast.LENGTH_LONG).show();
        } catch (Exception e){
            Toast.makeText(MainActivity.this, "Errored Oout", Toast.LENGTH_LONG).show();
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