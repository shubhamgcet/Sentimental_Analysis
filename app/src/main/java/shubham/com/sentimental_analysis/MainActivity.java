package shubham.com.sentimental_analysis;

import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    VideoView videoView;
    boolean running;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    void init() {
        videoView = (VideoView) findViewById(R.id.videoView);
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/media1.mp4");
        videoView.setVideoURI(uri);
        videoView.start();
        takeScreenshot();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                running = true;
                final int duration = videoView.getDuration();

                new Thread(new Runnable() {
                    public void run() {
                        do {
                            new Runnable() {
                                public void run() {
                                    int time = (duration - videoView.getCurrentPosition()) / 1000;
                                    takeScreenshot();
                                }
                            };
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (!running) break;
                        }
                        while (videoView.getCurrentPosition() < duration);
                    }
                }).start();
            }
        });


    }

    private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            v1.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            v1.layout(0, 0, v1.getWidth(), v1.getHeight());

            v1.buildDrawingCache(true);
            // = Bitmap.createBitmap(v1.getDrawingCache());
            bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            try {
                if (bitmap != null)
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                            v1.getWidth(), v1.getHeight());
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
            v1.setDrawingCacheEnabled(false);
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            openScreenshot(imageFile);
            Log.d("Saved","file saved");
        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            Log.d("Error","no file saved");
            e.printStackTrace();
        }
    }

    private void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }
    /*class UploadVideo extends AsyncTask<String, Void, String> {
        String s;
        HttpResponse response;

        @Override
        protected String doInBackground(String... params) {
            HttpClient httpclient = new DefaultHttpClient();

            try {

                URI uri = new URI("https://api.projectoxford.ai/video/v1.0/trackface");

                HttpPost request = new HttpPost(uri);
                request.setHeader("Content-Type", "application/json");
                request.setHeader("Ocp-Apim-Subscription-Key", "3dcb172065d241a0aa8c9666b53e1999");


                 DownloadManager.Request body
                StringEntity reqEntity = new StringEntity("{\n" +
                        "\t\"url\":\"https://shubhamgcet.github.io/media1.mp4\"\n" +
                        "}");
                request.setEntity(reqEntity);

                response = httpclient.execute(request);
                return null;
            } catch (Exception e) {
                e.printStackTrace();
            }
           return null;
        }

        @Override
        protected void onPostExecute(String s) {
            HttpEntity entity = response.getEntity();
            try {

                if (entity != null) {
                    System.out.println(EntityUtils.toString(entity));
                    s = EntityUtils.toString(entity);
                    System.out.println("Out" + s);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
    }