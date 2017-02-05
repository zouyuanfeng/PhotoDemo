package com.itzyf.photodemo;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.itzyf.photodemo.util.ImageResizer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_TAKE_PHOTO = 1;
    public static final int REQUEST_OPEN_PHOTO = 2;
    private ImageView mImageView;

    private String saveDir = "/itzyf/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = (ImageView) findViewById(R.id.image);

        File file = new File(Environment.getExternalStorageDirectory().toString() + saveDir);
        if (!file.exists()) {
            boolean result = file.mkdir();
            if (result) Toast.makeText(this, "初始文件夹成功", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            setPic();
        } else if (requestCode == REQUEST_OPEN_PHOTO && resultCode == RESULT_OK) {
            mCurrentPhotoPath = readFilePath(data);
            setPic();
        }
    }

    private void setPic() {
        Bitmap bitmap = ImageResizer.decodeSampledBitmapFromFile(mCurrentPhotoPath, mImageView.getWidth() * 2, mImageView.getHeight() * 2);
        mImageView.setImageBitmap(bitmap);
        saveBitmapToFile(bitmap);
    }

    private String readFilePath(Intent data) {
        Uri selectedImage = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            return picturePath;
        }
        return "";
    }

    private void saveBitmapToFile(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 把压缩后的数据存放到baos中
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.CHINA).format(new Date()) + (new Random().nextInt(89) + 10);
            File file = new File(Environment.getExternalStorageDirectory().toString() + saveDir, timeStamp + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        } else {
            Toast.makeText(this, "无可用相机", Toast.LENGTH_SHORT).show();
        }
    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINESE).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    public void openPhoto(View view) {
        Intent picture = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (picture.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(picture, REQUEST_OPEN_PHOTO);
        } else {
            Toast.makeText(this, "无可用相册", Toast.LENGTH_SHORT).show();
        }
    }

    public void openCamera(View view) {
        dispatchTakePictureIntent();
    }
}
