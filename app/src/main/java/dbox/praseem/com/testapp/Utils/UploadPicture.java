package dbox.praseem.com.testapp.Utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.UploadRequest;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxFileSizeException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


public class UploadPicture extends AsyncTask<Void, Long, Boolean> {

    private final ProgressDialog dialog;
    private DropboxAPI<?> mApi;
    private String path;
    private File file;
    private long fileLength;
    private UploadRequest request;
    private Context context;
    private String errorMessage;


    public UploadPicture(Context context, DropboxAPI<?> api, String dropboxPath,
                         File file) {
        this.context = context.getApplicationContext();
        fileLength = file.length();
        mApi = api;
        path = dropboxPath;
        this.file = file;
        dialog = new ProgressDialog(context);
        dialog.setMax(100);
        dialog.setMessage("Uploading " + file.getName());
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setProgress(0);
        dialog.setButton(ProgressDialog.BUTTON_POSITIVE, "Cancel", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                request.abort();
            }
        });
        dialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            FileInputStream fis = new FileInputStream(file);
            String path = this.path + file.getName();
            request = mApi.putFileOverwriteRequest(path, fis, file.length(),
                    new ProgressListener() {
                        @Override
                        public long progressInterval() {
                            // Update the progress bar every half-second or so
                            return 500;
                        }

                        @Override
                        public void onProgress(long bytes, long total) {
                            publishProgress(bytes);
                        }
                    });

            if (request != null) {
                request.upload();
                return true;
            }

        } catch (DropboxUnlinkedException e) {
            errorMessage = "This app wasn't authenticated properly.";
        } catch (DropboxFileSizeException e) {
            errorMessage = "This file is too big to upload";
        } catch (DropboxPartialFileException e) {
            errorMessage = "Upload canceled";
        } catch (DropboxServerException e) {
            // Server-side exception.  These are examples of what could happen,
            // but we don't do anything special with them here.
            if (e.error == DropboxServerException._401_UNAUTHORIZED) {
                // Unauthorized, so we should unlink them.  You may want to
                // automatically log the user out in this case.
            } else if (e.error == DropboxServerException._403_FORBIDDEN) {
                // Not allowed to access this
            } else if (e.error == DropboxServerException._404_NOT_FOUND) {
                // path not found (or if it was the thumbnail, can't be
                // thumbnailed)
            } else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
            } else {
            }
            errorMessage = e.body.userError;
            if (errorMessage == null) {
                errorMessage = e.body.error;
            }
        } catch (DropboxIOException e) {
            errorMessage = "Network error.  Try again.";
        } catch (DropboxParseException e) {
            errorMessage = "Dropbox error.  Try again.";
        } catch (DropboxException e) {
            errorMessage = "Unknown error.  Try again.";
        } catch (FileNotFoundException e) {
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
        int percent = (int) (100.0 * (double) progress[0] / fileLength + 0.5);
        dialog.setProgress(percent);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        dialog.dismiss();
        if (result) {
            showToast("Image successfully uploaded");
        } else {
            showToast(errorMessage);
        }
    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        error.show();
    }
}
