package dbox.praseem.com.testapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;


public class AddEffectsActivity extends Activity {
    Bitmap bmp;
    ImageView img;
    BitmapDrawable abmp;
    Button grayEffectButton, darkEffectButton, brightEffectButton,doneButton;
    private Bitmap operation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_effects);
        byte[] byteArray = getIntent().getByteArrayExtra("image");
        bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        img = (ImageView) findViewById(R.id.filter_preview_iv);
        darkEffectButton = (Button) findViewById(R.id.dark_effect_button);
        brightEffectButton = (Button) findViewById(R.id.bright_effect_button);
        grayEffectButton = (Button) findViewById(R.id.gray_effect_button);
        doneButton=(Button)findViewById(R.id.done_button);
        img.setImageBitmap(bmp);
        abmp = (BitmapDrawable) img.getDrawable();
        bmp = abmp.getBitmap();

        darkEffectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dark(view);
            }
        });

        brightEffectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                bright(view);
            }
        });

        grayEffectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                gray(view);
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                operation.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                Intent i = new Intent(AddEffectsActivity.this, HomeActivity.class);
                i.putExtra("image", byteArray);
                startActivity(i);
            }
        });

    }

    public void gray(View view) {
        operation = Bitmap.createBitmap(bmp.getWidth(),
                bmp.getHeight(), bmp.getConfig());

        double red = 0.33;
        double green = 0.59;
        double blue = 0.11;

        for (int i = 0; i < bmp.getWidth(); i++) {
            for (int j = 0; j < bmp.getHeight(); j++) {
                int p = bmp.getPixel(i, j);
                int r = Color.red(p);
                int g = Color.green(p);
                int b = Color.blue(p);

                r = (int) red * r;
                g = (int) green * g;
                b = (int) blue * b;

                operation.setPixel(i, j, Color.argb(Color.alpha(p), r, g, b));
            }
        }
        img.setImageBitmap(operation);
    }

    public void bright(View view) {
        operation = Bitmap.createBitmap(bmp.getWidth(),
                bmp.getHeight(), bmp.getConfig());


        for (int i = 0; i < bmp.getWidth(); i++) {
            for (int j = 0; j < bmp.getHeight(); j++) {
                int p = bmp.getPixel(i, j);
                int r = Color.red(p);
                int g = Color.green(p);
                int b = Color.blue(p);
                int alpha = Color.alpha(p);

                r = 100 + r;
                g = 100 + g;
                b = 100 + b;
                alpha = 100 + alpha;

                operation.setPixel(i, j, Color.argb(alpha, r, g, b));
            }
        }
        img.setImageBitmap(operation);
    }

    public void dark(View view) {
        operation = Bitmap.createBitmap(bmp.getWidth(),
                bmp.getHeight(), bmp.getConfig());


        for (int i = 0; i < bmp.getWidth(); i++) {
            for (int j = 0; j < bmp.getHeight(); j++) {
                int p = bmp.getPixel(i, j);
                int r = Color.red(p);
                int g = Color.green(p);
                int b = Color.blue(p);
                int alpha = Color.alpha(p);

                r = r - 50;
                g = g - 50;
                b = b - 50;
                alpha = alpha - 50;
                operation.setPixel(i, j, Color.argb(Color.alpha(p), r, g, b));


            }
        }

        img.setImageBitmap(operation);
    }

}
