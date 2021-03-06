package com.vofka.simsimdrive;

import android.app.Activity;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Android Drive Quickstart activity. This activity takes a photo and saves it
 * in Google Drive. The user is prompted with a pre-made dialog which allows
 * them to choose the file location.
 */
public class ImageToDrive  extends Activity implements ConnectionCallbacks,
        OnConnectionFailedListener {

    private static final String TAG = "android-simsimdrive-1030";
    private static final int PICK_IMAGE_REQUEST = 1;
    //   private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_CREATOR = 2;
    private static final int REQUEST_CODE_RESOLUTION = 3;
    public String primary_sd = "";
    public String secondary_sd = "";

    private GoogleApiClient mGoogleApiClient;
    private Bitmap mBitmapToSave;

    /**
     * Create a new file and save it to Drive.
     */
    private void saveFileToDrive() {
        // Start by creating a new contents, and setting a callback.
        Log.i(TAG, "Creating new contents.");
        final Bitmap image = mBitmapToSave;
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(new ResultCallback<DriveContentsResult>() {

                    @Override
                    public void onResult(final DriveContentsResult result) {
                        // If the operation was not successful, we cannot do anything
                        // and must
                        // fail.
                        if (!result.getStatus().isSuccess()) {
                            Log.i(TAG, "Failed to create new contents.");
                            return;
                        }
                        // Otherwise, we can write our data to the new contents.
                        Log.i(TAG, "New contents created.");
                        // Get an output stream for the contents.
                        OutputStream outputStream = result.getDriveContents().getOutputStream();
                        // Write the bitmap data from it.
                        ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                        image.compress(Bitmap.CompressFormat.JPEG, 70, bitmapStream);


                        try {
                            outputStream.write(bitmapStream.toByteArray());
                        } catch (IOException e1) {
                            Log.i(TAG, "Unable to write file contents.");
                        }



                        // Create the initial metadata - MIME type and title.
                        // Note that the user will be able to change the title later.
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        String imageFileName="SimSim_"+timeStamp;

                        final MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                                .setMimeType("image/jpeg")
                                .setTitle(imageFileName)
                                .setStarred(true)
                                .build();



                 //   /*
                        // Perform I/O off the UI thread.
                        new Thread() {
                            @Override
                            public void run() {
                                // create a file on root folder
                                Drive.DriveApi.getRootFolder(mGoogleApiClient)
                                        .createFile(mGoogleApiClient, metadataChangeSet, result.getDriveContents())
                                        .setResultCallback(fileCallback);
                            }
                        }.start();
                 //   */
                    }
                });
    }

    final private ResultCallback<DriveFolder.DriveFileResult> fileCallback = new
            ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    if (!result.getStatus().isSuccess()) {
                        showMessage("Error while trying to create the file");
                        return;
                    }
                    showMessage("Created a file with content: " + result.getDriveFile().getDriveId());
                    finish();
                }

            };
    /**
     * Shows a toast message.
     */
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient == null) {
            // Create the API client and bind it to an instance variable.
            // We use this instance as the callback for connection and connection
            // failures.
            // Since no account name is passed, the user is prompted to choose.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        // Connect the client. Once connected, the camera is launched.
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Called whenever the API client fails to connect.
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization
        // dialog is displayed to the user.
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "API client connected.");
        if (mBitmapToSave == null) {
            // This activity has no UI of its own. Just start the camera.
            secondary_sd = System.getenv("SECONDARY_STORAGE");
            Bitmap bMap;

         if(secondary_sd!=null) {
              bMap = BitmapFactory.decodeFile(secondary_sd+"/SimSim Eye/Spyhole/spyhole.jpg");

            // bMap = BitmapFactory.decodeFile(secondary_sd + "/image.png");
         }else{
             bMap = BitmapFactory.decodeFile("/storage/sdcard0/SimSim Eye/Spyhole/spyhole.jpg");
         }


            mBitmapToSave=bMap;
        }
        saveFileToDrive();

    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }
}