package dbox.praseem.com.testapp.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import dbox.praseem.com.testapp.Models.Uploads;
import dbox.praseem.com.testapp.R;


public class UploadsAdapter extends BaseAdapter {

    Context context;
    ViewHolder viewHolder;
    List<Uploads> uploads;
    Bitmap operation = null;

    public UploadsAdapter(Context context, List<Uploads> uploads) {
        this.context = context;
        this.uploads = uploads;

    }


    public int getCount() {
        return uploads.size();
    }

    public Object getItem(int i) {
        return uploads.get(i);
    }

    public long getItemId(int i) {
        return i;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.myuploads_gridview_item, null);
        viewHolder = new ViewHolder();
        viewHolder.uploadImage = (ImageView) view.findViewById(R.id.upload_image);
        viewHolder.latlon = (TextView) view.findViewById(R.id.latlon);
        view.setTag(viewHolder);
        final Uploads uploads = (Uploads) getItem(i);
        final ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.uploadImage.setImageDrawable(uploads.getUploadDrawable());
        viewHolder.latlon.setText(uploads.getLatlon());
        viewHolder.uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Drawable drawable = uploads.getUploadDrawable();
//                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//                byte[] byteArray = stream.toByteArray();
//                Intent i = new Intent(context, ImagePreviewActivity.class);
//                i.putExtra("image", byteArray);
//                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(i);
            }
        });


        return view;
    }

    private static class ViewHolder {

        ImageView uploadImage;
        TextView latlon;

    }
}
