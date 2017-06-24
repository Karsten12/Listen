package com.fonsecakarsten.audiobooky;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Karsten on 6/5/2017.
 */

public class NewCaptureActivity extends Activity {

    static final int REQUEST_TAKE_PHOTO = 1;
    private int SELECT_FILE = 2;
    private String mCurrPhotoPath;
    private ArrayList<String> mImageArray;
    private MyAdapter mAdapter;
    public static final int RequestPermissionCode = 1;
    private Bitmap rcbitmap;
    private BitmapFactory.Options mOptions = new BitmapFactory.Options();
    boolean first = true;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_book);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mImageArray = new ArrayList<>();
        mAdapter = new MyAdapter();
        recyclerView.setAdapter(mAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.FAB2);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                done();
            }
        });

        Intent fromMA = getIntent();
        intent = new Intent(getApplicationContext(), BookActivity.class);
        //AudioBook book = (AudioBook) fromMA.getSerializableExtra("newBook");
        intent.putExtra("newBook", fromMA.getSerializableExtra("newBook"));
        intent.putExtra("token", fromMA.getExtras().getString("token"));
    }

    public void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                try {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", createImageFile()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                takePictureIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PNG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".png",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {

                mImageArray.add(mCurrPhotoPath);
                mAdapter.notifyDataSetChanged();

            } else if (requestCode == SELECT_FILE) {
                onSelectFromGalleryResult(data);
            }
        }
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.Myviewholder> {

        @Override
        public Myviewholder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.image_row, parent, false);

            return new Myviewholder(v);
        }

        @Override
        public void onBindViewHolder(final Myviewholder holder, int position) {
            final String imagePath = mImageArray.get(position);
            // Get and display image
            holder.imageView.setImageBitmap(getAndRotateImage(imagePath, 16, false));
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                    View layout = inflater.inflate(R.layout.image_popup, (ViewGroup) findViewById(R.id.image_popup_root), false);
                    ImageView image = (ImageView) layout.findViewById(R.id.image_popup_image);

                    // Get and display image
                    if (first) {
                        rcbitmap = getAndRotateImage(imagePath, 4, true);
                        mOptions.inBitmap = rcbitmap;
                        first = false;
                    } else {
                        rcbitmap = getAndRotateImage(imagePath, 4, true);
                    }
                    image.setImageBitmap(rcbitmap);

                    AlertDialog imageDialog = new AlertDialog.Builder(NewCaptureActivity.this)
                            .setView(layout)
                            .setPositiveButton("RETAKE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Todo
                                    // Retake image
                                    // Remove old image
                                    // Add new image
                                    // notifyItemRemoved(position);
                                    // notifyItemRangeChanged(position, mImageArray.size());
                                }
                            })
                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("REMOVE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    mImageArray.remove(holder.getAdapterPosition());
                                    notifyItemRemoved(holder.getAdapterPosition());
                                    notifyItemRangeChanged(holder.getAdapterPosition(), mImageArray.size());
                                }
                            }).create();
                    imageDialog.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mImageArray.size();
        }

        class Myviewholder extends RecyclerView.ViewHolder {
            ImageView imageView;

            Myviewholder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.image_view);
            }
        }
    }

    public void selectImage(View v) {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo!")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (items[item].equals("Take Photo")) {
                            takePicture();

                        } else if (items[item].equals("Choose from Library")) {
                            galleryIntent();

                        } else if (items[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
        if (//hasPermission()
                true) {
            builder.show();
        } else {
            //ActivityCompat.requestPermissions(this, getResources().getStringArray(R.array.permissions), RequestPermissionCode);
        }
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void onSelectFromGalleryResult(Intent data) {
        Uri selectedImageUri = data.getData();
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(selectedImageUri, projection, null, null, null);
        assert cursor != null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        mImageArray.add(path);
        mAdapter.notifyDataSetChanged();
        cursor.close();

//        if (Intent.ACTION_SEND_MULTIPLE.equals(data.getAction()) && data.hasExtra(Intent.EXTRA_STREAM)) {
//            // retrieve a collection of selected images
//            ArrayList<Parcelable> list = data.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
//            // iterate over these images
//            if( list != null ) {
//                for (Parcelable parcel : list) {
//                    Uri uri = (Uri) parcel;
//                    // TODO handle the images one by one here
//                }
//            }
//        }
    }

    // if photo is not stored locally, use this to get rcbitmap from external
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    // Pass the images back to be processed
    private void done() {
        intent.putExtra("imageArray", mImageArray);
        startActivity(intent);
        finish();
    }

    // Check if permissions granted
//    private boolean hasPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            String[] permArray = getResources().getStringArray(R.array.permissions);
//            for (String permission : permArray) {
//                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }

    // If permissions not granted, request it
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        int granted = PackageManager.PERMISSION_GRANTED;
//        switch (requestCode) {
//            case RequestPermissionCode:
//                if (grantResults.length > 0 &&
//                        grantResults[0] == granted &&
//                        grantResults[1] == granted &&
//                        grantResults[2] == granted) {
//                    // Permissions all granted
//                    break;
//                } else {
//                    // One or more permissions denied, re-request permissions
//                    ActivityCompat.requestPermissions(this, getResources().getStringArray(R.array.permissions), RequestPermissionCode);
//                }
//        }
//    }

    public Bitmap getAndRotateImage(String path, int insamp, boolean reuse) {
        BitmapFactory.Options options;
        try {
            File f = new File(path);
            ExifInterface exif = new ExifInterface(f.getPath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            int angle = 0;

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    angle = 90;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    angle = 180;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    angle = 270;
                    break;

                default:
                    break;
            }

            Matrix mat = new Matrix();
            mat.postRotate(angle);
            Bitmap bmp;

            if (reuse) {
                options = mOptions;
                options.inSampleSize = insamp;
                bmp = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
                rcbitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mat, true);
                return rcbitmap;

            } else {
                options = new BitmapFactory.Options();
                options.inSampleSize = insamp;
                bmp = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
                return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mat, true);
            }

        } catch (IOException e) {
            System.out.println("TAG-- Error in setting image");
        } catch (OutOfMemoryError oom) {
            System.out.println("TAG-- OOM Error in setting image");
        }
        return null;
    }

}