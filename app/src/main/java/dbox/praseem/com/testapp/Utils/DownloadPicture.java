package dbox.praseem.com.testapp.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.DropboxAPI.ThumbFormat;
import com.dropbox.client2.DropboxAPI.ThumbSize;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dbox.praseem.com.testapp.Adapters.DatabaseAdapter;
import dbox.praseem.com.testapp.Adapters.UploadsAdapter;
import dbox.praseem.com.testapp.Models.Uploads;


public class DownloadPicture extends AsyncTask<Void, Long, Boolean> {
    private final static String IMAGE_FILE_NAME = "dbroulette.png";
    private final ProgressDialog mDialog;
    List<Uploads> uploads;
    ArrayList<Entry> thumbs;
    String fileName;
    DatabaseAdapter databaseAdapter;
    Geocoder geocoder;
    private Context context;
    private DropboxAPI<?> mApi;
    private String PATH;
    private ImageView imageView;
    private GridView gridView;
    private Drawable drawable;
    private FileOutputStream fileOutputStream;
    private boolean CANCELLED;
    private Long fileLength;
    private String errorMessage;
    private UploadsAdapter uploadsAdapter;

    public DownloadPicture(Context context, DropboxAPI<?> api,
                           String dropboxPath, ImageView view, GridView gridView) {
        this.context = context.getApplicationContext();
        mApi = api;
        PATH = dropboxPath;
        imageView = view;
        this.gridView = gridView;
        uploads = new ArrayList<Uploads>();
        mDialog = new ProgressDialog(context);
        mDialog.setMessage("Downloading Image");
        mDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "Cancel", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                CANCELLED = true;
                errorMessage = "Canceled";
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                    }
                }
            }
        });

        mDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            if (CANCELLED) {
                return false;
            }
            Entry dirent = mApi.metadata(PATH, 1000, null, true, null);

            if (!dirent.isDir || dirent.contents == null) {
                errorMessage = "File or empty directory";
                return false;
            }

            thumbs = new ArrayList<Entry>();
            for (Entry ent : dirent.contents) {
                if (ent.thumbExists) {
                    thumbs.add(ent);
                }
            }
            if (CANCELLED) {
                return false;
            }
            if (thumbs.size() == 0) {
                errorMessage = "No pictures in that directory";
                return false;
            }
            for (int i = 0; i < thumbs.size(); i++)
            {
                Entry ent = thumbs.get(i);
                String path = ent.path;
                fileName = ent.fileName();
                fileLength = ent.bytes;
                String cachePath = context.getCacheDir().getAbsolutePath() + "/" + IMAGE_FILE_NAME;
                try {
                    fileOutputStream = new FileOutputStream(cachePath);
                } catch (FileNotFoundException e) {
                    errorMessage = "Couldn't create a local file to store the image";
                    return false;
                }
                mApi.getThumbnail(path, fileOutputStream, ThumbSize.BESTFIT_960x640,
                        ThumbFormat.JPEG, null);
                if (CANCELLED) {
                    return false;
                }
                drawable = Drawable.createFromPath(cachePath);
                String location = getLocation(fileName);
                String latString = null;
                String lonString = null;
                if (location.contains(",")) {
                    latString = location.substring(0, location.indexOf(","));
                    lonString = location.substring(location.indexOf(",") + 1);
                    String place = getMyLocationAddress(Double.parseDouble(latString), Double.parseDouble(lonString));
                    uploads.add(i, new Uploads(drawable, place));
                } else uploads.add(i, new Uploads(drawable, location));
            }
            return true;
        } catch (DropboxUnlinkedException e) {
            // The AuthSession wasn't properly authenticated or user unlinked.
        } catch (DropboxPartialFileException e) {
            // We canceled the operation
            errorMessage = "Download canceled";
        } catch (DropboxServerException e) {
            // Server-side exception.  These are examples of what could happen,
            // but we don't do anything special with them here.
            if (e.error == DropboxServerException._304_NOT_MODIFIED) {
                // won't happen since we don't pass in revision with metadata
            } else if (e.error == DropboxServerException._401_UNAUTHORIZED) {
                // Unauthorized, so we should unlink them.  You may want to
                // automatically log the user out in this case.
            } else if (e.error == DropboxServerException._403_FORBIDDEN) {
                // Not allowed to access this
            } else if (e.error == DropboxServerException._404_NOT_FOUND) {
                // path not found (or if it was the thumbnail, can't be
                // thumbnailed)
            } else if (e.error == DropboxServerException._406_NOT_ACCEPTABLE) {
                // too many entries to return
            } else if (e.error == DropboxServerException._415_UNSUPPORTED_MEDIA) {
                // can't be thumbnailed
            } else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
                // user is over quota
            } else {
                // Something else
            }
            // This gets the Dropbox error, translated into the user's language
            errorMessage = e.body.userError;
            if (errorMessage == null) {
                errorMessage = e.body.error;
            }
        } catch (DropboxIOException e) {
            // Happens all the time, probably want to retry automatically.
            errorMessage = "Network error.  Try again.";
        } catch (DropboxParseException e) {
            // Probably due to Dropbox server restarting, should retry
            errorMessage = "Dropbox error.  Try again.";
        } catch (DropboxException e) {
            // Unknown error
            errorMessage = "Unknown error.  Try again.";
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
        int percent = (int) (100.0 * (double) progress[0] / fileLength + 0.5);
        mDialog.setProgress(percent);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mDialog.dismiss();
        if (result) {
            imageView.setImageDrawable(drawable);
            uploadsAdapter = new UploadsAdapter(context, uploads);
            gridView.setAdapter(uploadsAdapter);
        } else {
            showToast(errorMessage);
        }
    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        error.show();
    }

    public String getLocation(String filename) {
        databaseAdapter = new DatabaseAdapter(context);
        return databaseAdapter.getLocations(fileName);
    }

    public String getMyLocationAddress(double lat, double lon) {
        Geocoder geocoder = new Geocoder(context, Locale.ENGLISH);
        String location = "";
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null) {
                Address fetchedAddress = addresses.get(0);
                StringBuilder strAddress = new StringBuilder();
                strAddress.append(fetchedAddress.getAddressLine(2));
                location = strAddress.toString();
                return location;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(context, "Could not get address..!", Toast.LENGTH_LONG).show();
        }
        return location;
    }
}
