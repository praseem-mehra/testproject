package dbox.praseem.com.testapp.Models;

import android.graphics.drawable.Drawable;

/**
 * Created by admin on 3/19/2015.
 */
public class Uploads {
    Drawable uploadDrawable;
    String latlon;

    public Uploads(Drawable uploadDrawable, String latlon) {
        this.uploadDrawable = uploadDrawable;
        this.latlon = latlon;
    }

    public Drawable getUploadDrawable() {
        return uploadDrawable;
    }

    public void setUploadDrawable(Drawable uploadDrawable) {
        this.uploadDrawable = uploadDrawable;
    }

    public String getLatlon() {
        return latlon;
    }

    public void setLatlon(String latlon) {
        this.latlon = latlon;
    }
}
