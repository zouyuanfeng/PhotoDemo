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

        //压缩后的图片保存的文件路径，文件夹需手动创建，不然会保存失败
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

    /**
     * 压缩图片并设置图片到ImageView，最后保存到文件中
     */
    private void setPic() {
        Bitmap bitmap = ImageResizer.decodeSampledBitmapFromFile(mCurrentPhotoPath, mImageView.getWidth() * 2, mImageView.getHeight() * 2);
        mImageView.setImageBitmap(bitmap);
        saveBitmapToFile(bitmap);
    }

    /**
     * 从Intent中读取文件路径
     *
     * @param data
     * @return
     */
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

    /**
     * 存储bitmap图片到文件中
     *
     * @param bitmap
     */
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

    /**
     * 拍照
     */
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
//                Uri.fromFile(photoFile);//使用这个uri在Android7上会报错
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        } else {
            Toast.makeText(this, "无可用相机", Toast.LENGTH_SHORT).show();
        }
    }

    String mCurrentPhotoPath;

    /**
     * 创建一个临时文件用于存储图片
     *
     * @return
     * @throws IOException
     */
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

    /**
     * 打开相册
     *
     * @param view
     */
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
