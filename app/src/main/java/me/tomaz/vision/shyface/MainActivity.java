package me.tomaz.vision.shyface;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.widget.ImageView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.PicassoEngine;
import java.util.List;
import me.tomaz.vision.shyface.patch.SafeFaceDetector;
import me.tomaz.vision.shyface.vision.FaceView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CODE_CHOOSE = 102;
    @BindView(R.id.img_main_face)
    ImageView imgMainFace;
    @BindView(R.id.face_main_overlay)
    FaceView faceMainOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_main_search)
    public void onChooseImg() {
        Matisse.from(MainActivity.this)
                .choose(MimeType.of(MimeType.GIF, MimeType.JPEG, MimeType.PNG))
                .countable(false)
                .maxSelectable(1)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new PicassoEngine())
                .forResult(REQUEST_CODE_CHOOSE);
    }

    private void updateImage(Uri uri) {
        Picasso.with(this).load(uri).into(imgMainFace, new Callback() {
            @Override
            public void onSuccess() {
                imgMainFace.buildDrawingCache();
                refreshDetection(imgMainFace.getDrawingCache());
            }

            @Override
            public void onError() {

            }
        });

    }

    private void refreshDetection(Bitmap bitmap) {
        FaceDetector detector = new FaceDetector.Builder(getApplicationContext())
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build();

        Detector<Face> safeDetector = new SafeFaceDetector(detector);

        // Create a frame from the bitmap and run face detection on the frame.
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Face> faces = safeDetector.detect(frame);

        if (!safeDetector.isOperational()) {
            Log.w(TAG, "Face detector dependencies are not yet available.");

            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        faceMainOverlay.setContent(bitmap, faces);
        safeDetector.release();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CHOOSE:
                if (resultCode == RESULT_OK) {
                    List<Uri> selected = Matisse.obtainResult(data);
                    Log.d("Matisse", "mSelected: " + selected);
                    updateImage(selected.get(0));
                }
                break;
        }
    }
}
