package shubham.com.sentimental_analysis;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Shubham on 10/28/2017.
 */

public class AnyImagePosition extends Activity {

    ImageView imageView, imageView1;

    int imageCount = 0;
    int max;
    SeekBar seekBar;
    ArrayList<Paint> paintList = new ArrayList<>();
    ArrayList<Rect> rectList = new ArrayList<>();
    ArrayList<Bitmap> bitmapList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize);
        imageView = (ImageView) findViewById(R.id.selectedImage);
        imageView1 = (ImageView) findViewById(R.id.selectedImageNew);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        File[] files = new File(Environment.getExternalStorageDirectory() + "/hackpics").listFiles();
        max = files.length;
        seekBar.setMax(max);
        imageView1.setVisibility(View.GONE);
        Bundle bundle = getIntent().getExtras();
        // bitmapList = bundle.get("list_bitmap");
        paintList = (ArrayList<Paint>) bundle.getSerializable("list_paint");
        //        paintList = (ArrayList<Paint>) getIntent().getSerializableExtra("list_paint");
        //        rectList = (ArrayList<Rect>) getIntent().getSerializableExtra("list_rect");
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser == true) {
                    try {
                        //   imageView.setImageBitmap(bitmapList.get(progress - 1));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

}
