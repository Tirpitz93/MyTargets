/*
 * MyTargets Archery
 *
 * Copyright (C) 2015 Florian Dreier
 * All rights reserved
 */

package de.dreier.mytargets.shared.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class BitmapUtils {
    public static Bitmap decodeSampledBitmapFromFile(Context context, File file, int reqWidth, int reqHeight)
            throws IOException {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream stream = new FileInputStream(file);
        BitmapFactory.decodeStream(stream, null, options);
        stream.close();
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        stream = new FileInputStream(file);
        Bitmap bmp = BitmapFactory.decodeStream(stream, null, options);
        stream.close();
        return bmp;
    }

    public static Bitmap decodeSampledBitmapFromStream(Context context, Uri uri, int reqWidth, int reqHeight)
            throws IOException {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream stream = context.getContentResolver().openInputStream(uri);
        BitmapFactory.decodeStream(stream, null, options);
        stream.close();
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        stream = context.getContentResolver().openInputStream(uri);
        Bitmap bmp = BitmapFactory.decodeStream(stream, null, options);
        stream.close();
        return bmp;
    }

    public static Bitmap decodeSampledBitmapFromRes(Context context, int id, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), id, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(context.getResources(), id, options);
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        int minSize = Math.max(reqWidth, reqHeight);

        if (height > minSize || width > minSize) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > minSize
                    && (halfWidth / inSampleSize) > minSize) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    // Convert Picture to Bitmap
    public static Bitmap pictureDrawable2Bitmap(Picture picture) {
        PictureDrawable pd = new PictureDrawable(picture);
        Bitmap bitmap = Bitmap.createBitmap(pd.getIntrinsicWidth(), pd.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawPicture(pd.getPicture());
        return bitmap;
    }

    public static int animateColor(int from, int to, float percent) {
        final int fa = (from >> 24) & 0xFF;
        final int fr = (from >> 16) & 0xFF;
        final int fg = (from >> 8) & 0xFF;
        final int fb = (from) & 0xFF;
        final int da = ((to >> 24) & 0xFF) - fa;
        final int dr = ((to >> 16) & 0xFF) - fr;
        final int dg = ((to >> 8) & 0xFF) - fg;
        final int db = ((to) & 0xFF) - fb;
        final int ra = (int) (fa + da * percent);
        final int rr = (int) (fr + dr * percent);
        final int rg = (int) (fg + dg * percent);
        final int rb = (int) (fb + db * percent);
        return (ra << 24) | (rr << 16) | (rg << 8) | rb;
    }

    public static Bitmap getBitmap(Context context, String imageFile) {
        try {
            if (imageFile != null) {
                if (imageFile.contains("/")) {
                    imageFile = imageFile.substring(imageFile.lastIndexOf("/") + 1);
                }
                Log.d("getImage", imageFile);
                FileInputStream in = context.openFileInput(imageFile);
                return BitmapFactory.decodeStream(in);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
