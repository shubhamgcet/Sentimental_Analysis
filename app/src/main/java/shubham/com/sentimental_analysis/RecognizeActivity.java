package shubham.com.sentimental_analysis;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.FaceRectangle;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;
import com.microsoft.projectoxford.emotion.rest.EmotionServiceException;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shubham on 10/28/2017.
 */

public class RecognizeActivity extends ActionBarActivity {

    // Flag to indicate which task is to be performed.
    private static final int REQUEST_SELECT_IMAGE = 0;

    // The button to select an image


    // The URI of the image selected to detect.
    private Uri mImageUri;
    ImageView imageView, imageView1;
    // The image selected to detect.
    private Bitmap mBitmap, mBitmap1;
    Bitmap[] bitmapArray;
    int bitmapCount = -1;
    // The edit to show status and result.
    private EditText mEditText;

    private EmotionServiceClient client;
    int imageCount = 0;
    int max;
    SeekBar seekBar;
    ArrayList<Paint> paintList = new ArrayList<>();
    ArrayList<Rect> rectList = new ArrayList<>();
    ArrayList<Bitmap> bitmapList = new ArrayList<>();
    Button button;
    float anger = .9f, contempt, disgust, fear, happiness = .9f, neutral, sadness, surprise, count = 0;
    TextView textView, textViewWait;
    String[] array;
    File[] files;
    MyCount counter = new MyCount(60000, 100);
    RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_recognize);

        if (client == null) {
            client = new EmotionServiceRestClient(getString(R.string.subscription_key));
        }

        mEditText = (EditText) findViewById(R.id.editTextResult);
        imageView = (ImageView) findViewById(R.id.selectedImage);
        imageView1 = (ImageView) findViewById(R.id.selectedImageNew);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        button = (Button) findViewById(R.id.button);
        //    listView = (ListView) findViewById(R.id.listView);
        textView = (TextView) findViewById(R.id.textView);
        textViewWait = (TextView) findViewById(R.id.textViewWaiting);
        textViewWait.setVisibility(View.VISIBLE);
        relativeLayout = (RelativeLayout) findViewById(R.id.relativelayout);
        files = new File(Environment.getExternalStorageDirectory() + "/hackpics").listFiles();
//        max = files.length;

        seekBar.setMax(max);

        bitmapArray = new Bitmap[max];
        selectImage(++imageCount);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser == true) {
                    try {
                        imageView1.setImageBitmap(bitmapArray[progress - 1]);
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

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = seekBar.getProgress();
                Bitmap bmp = bitmapList.get(pos - 1).copy(Bitmap.Config.ARGB_8888, true);

//               Canvas cnvs = new Canvas(bmp);
                Canvas cnvs = new Canvas(bmp);
                //img.setImageBitmap(bmp);
                cnvs.drawBitmap(bmp, 0, 0, null);
                for (int i = 0; i < paintList.size(); i++) {
                    Rect rect = rectList.get(i);
                    Paint paint = paintList.get(i);
                    cnvs.drawRect(rect, paint);
                }
                imageView.setImageBitmap(bmp);

            }
        });
    }

    int current = 0;
    List<String> checkOutCondition = new ArrayList<>();

    void selectImage(int i) {
        if (i >= max) {
            button.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);
            textViewWait.setText("Choose Image for Calibration");
            Float[] checkMaxArray = {anger, contempt, disgust, fear, happiness, neutral, sadness, surprise};
            String[] checkMaxArrayString = {"anger", " contempt", " disgust", " fear", "happiness", " neutral", " sadness", " surprise"};
            float maxpos = anger;
            int posMax = 0, o = 0;
            for (Float f : checkMaxArray) {
                if (f > maxpos) {
                    maxpos = f;
                    posMax = o;
                }
                o++;
            }
            array = new String[]{
                    "anger" + anger, "contempt" + contempt, "disgust" + disgust, "fear" + fear, "happiness" + happiness, "neutral" + neutral, "sadness" + sadness, "surprise" + surprise,
                    "count" + count,
                    "max" + checkMaxArrayString[posMax] + checkMaxArray[posMax]

            };
            for (String s : array) {
                textView.append(s + "\n");
            }
            textViewWait.setTextColor(Color.WHITE);

            if (checkMaxArrayString[posMax].equals("neutral"))
                textViewWait.setText("Lecture can be more awesome");
            else if((checkMaxArrayString[posMax].equals("happiness")) || (checkMaxArrayString[posMax].equals("surprise")))
                textViewWait.setText("Lecture was awesome");
            else
                textViewWait.setText("Lets make lecture awesome");

            return;
        }

        if ((i % 20 == 0) && (i != current)) {
            Log.d("Wait", "Begins");
            current = i;

            textViewWait.setText("Waiting....");
            counter.start();
            Log.d("Wait", "On");
            return;

        }
//        float a = anger + contempt + fear;
//        float b = happiness + surprise;
//
//        float c = neutral;

        relativeLayout.setBackgroundColor(Color.rgb(
                (int) Math.floor((anger + contempt + fear + (neutral * .33)) / (i) * 255),
                (int) Math.floor((happiness + surprise + (neutral * .33)) / (i) * 255),
                (int) Math.floor((neutral * .33) / (i) * 255)
        ));


        seekBar.setProgress(i);

        mImageUri = Uri.fromFile(files[i]);

        mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                mImageUri, getContentResolver());
        textViewWait.setVisibility(View.VISIBLE);
        if (mBitmap != null) {
            // Show the image on screen.
            if (i == 1)
                mBitmap1 = mBitmap;
            imageView.setImageBitmap(mBitmap1);
            textViewWait.setText("Uploading Image no " + i);
            bitmapCount++;
//            imageView1.setImageBitmap(mBitmap);
            // Add detection log.
            bitmapList.add(mBitmap);
            Log.d("RecognizeActivity", "Image: " + mImageUri + " resized to " + mBitmap.getWidth()
                    + "x" + mBitmap.getHeight());

            doRecognize();
        }

    }

    public class MyCount extends CountDownTimer {

        public MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            // TODO Auto-generated method stub
            textViewWait.setVisibility(View.GONE);
            selectImage(current);
            Log.d("Wait", "Over");
//            Intent i=new Intent(mainact.this,MyMainActivity.class);
            //startActivity(i);
            //finish();


        }

        @Override
        public void onTick(long millisUntilFinished) {
        }
    }

    public void doRecognize() {

        // Do emotion detection using auto-detected faces.
        try {
            new doRequest(false).execute();
        } catch (Exception e) {
            mEditText.append("Error encountered. Exception is: " + e.toString());
        }

        String faceSubscriptionKey = getString(R.string.faceSubscription_key);
        if (faceSubscriptionKey.equalsIgnoreCase("Please_add_the_face_subscription_key_here")) {
            mEditText.append("\n\nThere is no face subscription key in res/values/strings.xml. Skip the sample for detecting emotions using face rectangles\n");
        } else {
            // Do emotion detection using face rectangles provided by Face API.
            try {
                // new doRequest(true).execute();
            } catch (Exception e) {
                mEditText.append("Error encountered. Exception is: " + e.toString());
            }
        }
    }

    // Called when the "Select Image" button is clicked.

    // Called when image selection is done.
    // @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.d("RecognizeActivity", "onActivityResult");
//        switch (requestCode) {
//            case REQUEST_SELECT_IMAGE:
//                if (resultCode == RESULT_OK) {
//                    // If image is selected successfully, set the image URI and bitmap.
//                    mImageUri = data.getData();
//
//                    mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
//                            mImageUri, getContentResolver());
//                    if (mBitmap != null) {
//                        // Show the image on screen.
//                        ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
//                        imageView.setImageBitmap(mBitmap);
//
//                        // Add detection log.
//                        Log.d("RecognizeActivity", "Image: " + mImageUri + " resized to " + mBitmap.getWidth()
//                                + "x" + mBitmap.getHeight());
//
//                        doRecognize();
//                    }
//                }
//                break;
//            default:
//                break;
//        }
//    }


    private List<RecognizeResult> processWithAutoFaceDetection() throws EmotionServiceException, IOException {
        Log.d("emotion", "Start emotion detection with auto-face detection");

        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        long startTime = System.currentTimeMillis();
        // -----------------------------------------------------------------------
        // KEY SAMPLE CODE STARTS HERE
        // -----------------------------------------------------------------------

        List<RecognizeResult> result = null;
        //
        // Detect emotion by auto-detecting faces in the image.
        //
        result = this.client.recognizeImage(inputStream);

        String json = gson.toJson(result);
        Log.d("result", json);

        Log.d("auto emotion", String.format("Detection done. Elapsed time: %d ms", (System.currentTimeMillis() - startTime)));
        // -----------------------------------------------------------------------
        // KEY SAMPLE CODE ENDS HERE
        // -----------------------------------------------------------------------
        return result;
    }

    private List<RecognizeResult> processWithFaceRectangles() throws EmotionServiceException, com.microsoft.projectoxford.face.rest.ClientException, IOException {
        Log.d("emotion", "Do emotion detection with known face rectangles");
        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        long timeMark = System.currentTimeMillis();
        Log.d("emotion", "Start face detection using Face API");
        FaceRectangle[] faceRectangles = null;
        String faceSubscriptionKey = getString(R.string.faceSubscription_key);
        FaceServiceRestClient faceClient = new FaceServiceRestClient(faceSubscriptionKey);
        Face faces[] = faceClient.detect(inputStream, false, false, null);
        Log.d("emotion", String.format("Face detection is done. Elapsed time: %d ms", (System.currentTimeMillis() - timeMark)));

        if (faces != null) {
            faceRectangles = new FaceRectangle[faces.length];

            for (int i = 0; i < faceRectangles.length; i++) {
                // Face API and Emotion API have different FaceRectangle definition. Do the conversion.
                com.microsoft.projectoxford.face.contract.FaceRectangle rect = faces[i].faceRectangle;
                faceRectangles[i] = new com.microsoft.projectoxford.emotion.contract.FaceRectangle(rect.left, rect.top, rect.width, rect.height);
            }
        }

        List<RecognizeResult> result = null;
        if (faceRectangles != null) {
            inputStream.reset();

            timeMark = System.currentTimeMillis();
            Log.d("emotion", "Start emotion detection using Emotion API");
            // -----------------------------------------------------------------------
            // KEY SAMPLE CODE STARTS HERE
            // -----------------------------------------------------------------------
            result = this.client.recognizeImage(inputStream, faceRectangles);

            String json = gson.toJson(result);
            Log.d("result", json);
            // -----------------------------------------------------------------------
            // KEY SAMPLE CODE ENDS HERE
            // -----------------------------------------------------------------------
            Log.d("normal emotion", String.format("Emotion detection is done. Elapsed time: %d ms", (System.currentTimeMillis() - timeMark)));
        }
        return result;
    }

    private class doRequest extends AsyncTask<String, String, List<RecognizeResult>> {
        // Store error message
        private Exception e = null;
        private boolean useFaceRectangles = false;

        public doRequest(boolean useFaceRectangles) {
            this.useFaceRectangles = useFaceRectangles;
        }

        @Override
        protected List<RecognizeResult> doInBackground(String... args) {
            if (this.useFaceRectangles == false) {
                try {
                    return processWithAutoFaceDetection();
                } catch (Exception e) {
                    this.e = e;    // Store error
                }
            } else {
                try {
                    return processWithFaceRectangles();
                } catch (Exception e) {
                    this.e = e;    // Store error
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<RecognizeResult> result) {
            super.onPostExecute(result);
            // Display based on error existence

            if (this.useFaceRectangles == false) {
                mEditText.append("\n\nRecognizing emotions with auto-detected face rectangles...\n");
            } else {
                mEditText.append("\n\nRecognizing emotions with existing face rectangles from Face API...\n");
            }
            if (e != null) {
                mEditText.setText("Error: " + e.getMessage());
                this.e = null;
            } else {
                if (result.size() == 0) {
                    mEditText.append("No emotion detected :(");
                } else {

                    // Covert bitmap to a mutable bitmap by copying it
                    Bitmap bmp = mBitmap1.copy(Bitmap.Config.ARGB_8888, true);
                    Bitmap bmp1 = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
//                    Canvas faceCanvas = new Canvas(bitmapCopy);
//                    faceCanvas.drawBitmap(mBitmap, 0, 0, null);
//                    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//                    paint.setStyle(Paint.Style.STROKE);
//                    paint.setStrokeWidth(5);
//                    paint.setColor(Color.RED);
                    // Bitmap bmp=Bitmap.createBitmap(imageView.getHeight(),imageView.getWidth(),Bitmap.Config.RGB_565);
                    Canvas cnvs = new Canvas(bmp);
                    Canvas cnvs1 = new Canvas(bmp1);
                    //img.setImageBitmap(bmp);
                    cnvs.drawBitmap(bmp, 0, 0, null);
                    cnvs1.drawBitmap(bmp1, 0, 0, null);

                    Paint paint = new Paint();

                    for (RecognizeResult r : result) {
                        paint.setColor(Color.rgb(
                                (int) Math.floor((r.scores.anger + r.scores.contempt + r.scores.fear + (r.scores.neutral * .33)) * 255),
                                (int) Math.floor((r.scores.happiness + r.scores.surprise + (r.scores.neutral * .33)) * 255),
                                (int) Math.floor((r.scores.neutral * .34) * 255)
                        ));
                        paint.setAlpha(75);

//                        mEditText.append(String.format("\nFace #%1$d \n", count));
                        anger += r.scores.anger;
                        contempt += r.scores.contempt;
                        disgust += r.scores.disgust;
                        fear += r.scores.fear;
                        happiness += r.scores.happiness;
                        neutral += r.scores.neutral;
                        sadness += r.scores.sadness;
                        surprise += r.scores.surprise;
//                        mEditText.append(String.format("\t anger: %1$.5f\n", r.scores.anger));
//                        mEditText.append(String.format("\t contempt: %1$.5f\n", r.scores.contempt));
//                        mEditText.append(String.format("\t disgust: %1$.5f\n", r.scores.disgust));
//                        mEditText.append(String.format("\t fear: %1$.5f\n", r.scores.fear));
//                        mEditText.append(String.format("\t happiness: %1$.5f\n", r.scores.happiness));
                        mEditText.append(String.format("\t neutral: %1$.5f\n", r.scores.neutral));
//                        mEditText.append(String.format("\t sadness: %1$.5f\n", r.scores.sadness));
//                        mEditText.append(String.format("\t surprise: %1$.5f\n", r.scores.surprise));
//                        mEditText.append(String.format("\t face rectangle: %d, %d, %d, %d", r.faceRectangle.left, r.faceRectangle.top, r.faceRectangle.width, r.faceRectangle.height));
//                        faceCanvas.drawRect(130,130,130,130,
//                                paint);
                        Rect rect = new Rect();
                        rect.set(r.faceRectangle.left, r.faceRectangle.top, r.faceRectangle.left + r.faceRectangle.width, r.faceRectangle.top + r.faceRectangle.height);
                        rectList.add(rect);


                        cnvs.drawRect(r.faceRectangle.left, r.faceRectangle.top, r.faceRectangle.left + r.faceRectangle.width, r.faceRectangle.top + r.faceRectangle.height, paint);
                        cnvs1.drawRect(r.faceRectangle.left, r.faceRectangle.top, r.faceRectangle.left + r.faceRectangle.width, r.faceRectangle.top + r.faceRectangle.height, paint);
                        paintList.add(paint);
                        // imageView.setImageBitmap(bmp);
                        // imageView.setImageBitmap(mBitmap);
                        count++;
                    }
                    mBitmap1 = bmp;
                    imageView.setImageBitmap(mBitmap1);
                    bitmapArray[bitmapCount] = bmp1;
                    imageView1.setImageBitmap(bmp1);

//                     imageView.setImageDrawable(new BitmapDrawable(getResources(), mBitmap));
                }
                mEditText.setSelection(0);
                selectImage(++imageCount);
            }

        }
    }


}
