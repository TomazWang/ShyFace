package me.tomaz.vision.shyface.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by TomazWang on 18/06/2017.
 */

public class BitmapUtils {
    private static final String TAG = BitmapUtils.class.getSimpleName();


    public @Nullable Bitmap getBitmapFromUri(Context context, Uri uri) throws IOException {
        InputStream input = null;

        input = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input);
        input.close();

        return bitmap;

    }


}
