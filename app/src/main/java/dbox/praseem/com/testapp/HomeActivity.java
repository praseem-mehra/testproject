package dbox.praseem.com.testapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import dbox.praseem.com.testapp.Adapters.DatabaseAdapter;
import dbox.praseem.com.testapp.Utils.DownloadPicture;
import dbox.praseem.com.testapp.Utils.UploadPicture;


public class HomeActivity extends Activity {
    private static final String TAG = "DROPBOX";
    private static final String APP_KEY = "9xw1hjze98qix3k";
    private static final String APP_SECRET = "s1aody01r55kwzb";
    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "9xw1hjze98qix3k";
    private static final String ACCESS_SECRET_NAME = "s1aody01r55kwzb";
    private static final boolean USE_OAUTH1 = false;
    private static final int NEW_PICTURE = 1;
    private final String PHOTO_DIR = "/Photos/";

    DropboxAPI<AndroidAuthSession> mApi;

    private Button signInButton, clickButton, myUploadsButton, uploadButton, openButton, recordButton;
    private TextView logoutButton, tv, addEffects, soundRecord, soundPlay, soundStop;
    private ImageView imageView;
    private GridView gridView;
    private SlidingDrawer slidingDrawer;
    private LinearLayout linearLayout;
    private File f;
    private ImageView iv;
    private Bitmap bitmap;
    private CheckBox checkBox;

    private boolean mLoggedIn;
    private boolean gps_enabled = false;
    private boolean network_enabled = false;
    private String mCameraFileName;
    private String outputFile = null;

    private Location location;
    private MediaRecorder myAudioRecorder;
    private LocationManager locationManager;
    private DatabaseAdapter databaseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mCameraFileName = savedInstanceState.getString("mCameraFileName");
        }

        AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);
        setContentView(R.layout.activity_home);


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        signInButton = (Button) findViewById(R.id.auth_button);
        uploadButton = (Button) findViewById(R.id.upload_now_button);
        myUploadsButton = (Button) findViewById(R.id.my_uploads_button);
        openButton = (Button) findViewById(R.id.open_button);
        clickButton = (Button) findViewById(R.id.take_photo_button);
        recordButton = (Button) findViewById(R.id.record_sound_button);
        tv = (TextView) findViewById(R.id.tv);
        addEffects = (TextView) findViewById(R.id.add_effects);
        logoutButton = (TextView) findViewById(R.id.logout_button_text_view);
        gridView = (GridView) findViewById(R.id.grid_view);
        slidingDrawer = (SlidingDrawer) findViewById(R.id.bottom);
        iv = (ImageView) findViewById(R.id.filter_preview_image_view);
        imageView = (ImageView) findViewById(R.id.mImage);
        checkBox = (CheckBox) findViewById(R.id.check_box);
        linearLayout = (LinearLayout) findViewById(R.id.sound_controller_container);
        soundPlay = (TextView) findViewById(R.id.play_recording);
        soundRecord = (TextView) findViewById(R.id.start_recording);
        soundStop = (TextView) findViewById(R.id.stop_recording);

        soundStop.setEnabled(false);
        soundPlay.setEnabled(false);
        outputFile = Environment.getExternalStorageDirectory().
                getAbsolutePath() + "/myrecording.3gp";
        ;
        myAudioRecorder = new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        myAudioRecorder.setOutputFile(outputFile);

        if (getIntent().hasExtra("image")) {
            byte[] byteArray = getIntent().getByteArrayExtra("image");
            bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            iv.setImageBitmap(bitmap);
            addEffects.setVisibility(View.VISIBLE);
            tv.setVisibility(View.VISIBLE);
            checkBox.setVisibility(View.VISIBLE);
            uploadButton.setVisibility(View.VISIBLE);
        }

        signInButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                if (USE_OAUTH1) {
                    mApi.getSession().startAuthentication(HomeActivity.this);
                } else {
                    mApi.getSession().startOAuth2Authentication(HomeActivity.this);
                }
            }
        });

        recordButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayout.setVisibility(View.VISIBLE);
            }
        });

        soundRecord.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                start(view);
            }
        });

        soundStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                stop(view);
            }
        });

        soundPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    play(view);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        uploadButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                uploadPicture();
            }
        });
        addEffects.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                Intent i = new Intent(HomeActivity.this, AddEffectsActivity.class);
                i.putExtra("image", byteArray);
                startActivity(i);
            }
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    checkLocationOn();
                    location = getLocation();
                }
            }
        });

        myUploadsButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                slidingDrawer.open();
                myUploadsButton.setVisibility(View.GONE);
                DownloadPicture download = new DownloadPicture(HomeActivity.this, mApi, PHOTO_DIR, imageView, gridView);
                download.execute();
            }
        });

        openButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {

                if (slidingDrawer.isOpened()) {
                    slidingDrawer.close();
                    myUploadsButton.setVisibility(View.VISIBLE);

                } else {
                    DownloadPicture download = new DownloadPicture(HomeActivity.this, mApi, PHOTO_DIR, imageView, gridView);
                    download.execute();
                    slidingDrawer.open();
                }
            }
        });

        logoutButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                logOut();
            }
        });

        clickButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                linearLayout.setVisibility(View.GONE);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, NEW_PICTURE);
            }
        });

        setLoggedIn(mApi.getSession().isLinked());
    }

    public void uploadPicture() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        f = new File(Environment.getExternalStorageDirectory()
                + File.separator + getPath());
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream fo = null;
        try {
            fo = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fo.write(bytes.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fo.close();
            // Toast.makeText(getApplicationContext(),"weird",Toast.LENGTH_LONG).show();
            UploadPicture upload = new UploadPicture(this, mApi, PHOTO_DIR, f);
            upload.execute();
            String path = "", latitude = "", longitude = "", pictureLocation;
            path = getPath();
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());
            pictureLocation = latitude + "," + longitude;
            databaseAdapter = new DatabaseAdapter(getApplicationContext());
            databaseAdapter.insert(path, pictureLocation);
            //showToast(databaseAdapter.getallData());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String getPath() {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd-kk-mm-ss", Locale.US);
        String newPicFile = df.format(date) + ".jpg";
        return newPicFile;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("mCameraFileName", mCameraFileName);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onResume() {
        super.onResume();
        AndroidAuthSession session = mApi.getSession();
        if (session.authenticationSuccessful()) {
            try {
                // call to complete the auth
                session.finishAuthentication();
                // Store it locally in our app for later use
                storeAuth(session);
                setLoggedIn(true);
            } catch (IllegalStateException e) {
                showToast("Cant authenticate with Dropbox:" + e.getLocalizedMessage());
                Log.i(TAG, "Error authenticating", e);
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                uploadButton.setVisibility(View.VISIBLE);
                Bundle bundle = data.getExtras();
                bitmap = (Bitmap) bundle.get("data");
                clickButton.setText("Take another picture");
                iv.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.white));
                iv.setImageBitmap(bitmap);
                tv.setVisibility(View.VISIBLE);
                checkBox.setVisibility(View.VISIBLE);
                addEffects.setVisibility(View.VISIBLE);
            } else {
                Log.w(TAG, "Unknown Activity Result from mediaImport: "
                        + resultCode);
            }
        }
    }


    private void logOut() {

        mApi.getSession().unlink();
        clearKeys();
        setLoggedIn(false);
    }


    private void setLoggedIn(boolean loggedIn) {
        mLoggedIn = loggedIn;
        if (loggedIn) {
            logoutButton.setVisibility(View.VISIBLE);
            signInButton.setVisibility(View.GONE);
            clickButton.setVisibility(View.VISIBLE);
            iv.setVisibility(View.VISIBLE);
            recordButton.setVisibility(View.VISIBLE);

        } else {

            logoutButton.setVisibility(View.GONE);
            signInButton.setVisibility(View.VISIBLE);
            iv.setVisibility(View.INVISIBLE);
            clickButton.setVisibility(View.INVISIBLE);
            recordButton.setVisibility(View.INVISIBLE);
        }
    }


    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }

    private void loadAuth(AndroidAuthSession session) {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key == null || secret == null || key.length() == 0 || secret.length() == 0) return;
        if (key.equals("oauth2:")) {
            session.setOAuth2AccessToken(secret);
        } else {
            session.setAccessTokenPair(new AccessTokenPair(key, secret));
        }
    }

    private void storeAuth(AndroidAuthSession session) {
        // Store the OAuth 2 access token, if there is one.
        String oauth2AccessToken = session.getOAuth2AccessToken();
        if (oauth2AccessToken != null) {
            SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, "oauth2:");
            edit.putString(ACCESS_SECRET_NAME, oauth2AccessToken);
            edit.commit();
            return;
        }
        AccessTokenPair oauth1AccessToken = session.getAccessTokenPair();
        if (oauth1AccessToken != null) {
            SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, oauth1AccessToken.key);
            edit.putString(ACCESS_SECRET_NAME, oauth1AccessToken.secret);
            edit.commit();
            return;
        }
    }

    private void clearKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }

    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);

        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        loadAuth(session);
        return session;
    }

    public void checkLocationOn() {
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            final AlertDialog alertDialog = new AlertDialog.Builder(HomeActivity.this).create();
            alertDialog.setTitle("Location Settings");
            alertDialog.setMessage("To use this feature turn on device's Location Settings");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    }

    public Location getLocation() {
        if (null != locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER))
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        else
            return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onBackPressed() {
        if (slidingDrawer.isOpened()) {
            slidingDrawer.close();
            myUploadsButton.setVisibility(View.VISIBLE);
        } else
            super.onBackPressed();
    }

    public void start(View view) {
        try {
            myAudioRecorder.prepare();
            myAudioRecorder.start();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        soundRecord.setEnabled(false);
        soundStop.setEnabled(true);
        Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();

    }

    public void stop(View view) {
        myAudioRecorder.stop();
        myAudioRecorder.release();
        myAudioRecorder = null;
        soundStop.setEnabled(false);
        soundPlay.setEnabled(true);
        Toast.makeText(getApplicationContext(), "Audio recorded successfully",
                Toast.LENGTH_LONG).show();
    }

    public void play(View view) throws IllegalArgumentException,
            SecurityException, IllegalStateException, IOException {

        MediaPlayer m = new MediaPlayer();
        m.setDataSource(outputFile);
        m.prepare();
        m.start();
        Toast.makeText(getApplicationContext(), "Playing audio", Toast.LENGTH_LONG).show();

    }
}