package com.flipcam;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.flipcam.view.CameraView;

import java.io.File;

import static android.os.Environment.getExternalStoragePublicDirectory;
import static com.flipcam.PermissionActivity.FC_SHARED_PREFERENCE;


public class VideoFragment extends Fragment{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    public static final String TAG = "VideoFragment";
    SeekBar zoombar;
    CameraView cameraView;
    ImageButton switchCamera;
    ImageButton startRecord;
    ImageButton flash;

    public VideoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment VideoFragment.
     */
    public static VideoFragment newInstance() {
        VideoFragment fragment = new VideoFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video, container, false);

        Log.d(TAG,"Inside video fragment");
        ImageView substitute = (ImageView)view.findViewById(R.id.substitute);
        substitute.setVisibility(View.INVISIBLE);
        cameraView = (CameraView)view.findViewById(R.id.cameraSurfaceView);
        zoombar = (SeekBar)view.findViewById(R.id.zoomBar);
        zoombar.setProgressTintList(ColorStateList.valueOf(getResources().getColor(R.color.progressFill)));
        cameraView.setSeekBar(zoombar);
        zoombar.setProgress(0);
        zoombar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG, "progress = " + progress);
                if (cameraView.isSmoothZoomSupported()) {
                    Log.d(TAG, "Smooth zoom supported");
                    cameraView.smoothZoomInOrOut(progress);
                } else if(cameraView.isZoomSupported()){
                    cameraView.zoomInAndOut(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(!cameraView.isSmoothZoomSupported() && !cameraView.isZoomSupported()) {
                    Toast.makeText(getContext(), "Zoom not supported for this camera.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        switchCamera = (ImageButton)view.findViewById(R.id.switchCamera);
        switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraView.switchCamera();
            }
        });
        startRecord = (ImageButton)view.findViewById(R.id.cameraRecord);
        startRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraView.record();
            }
        });
        flash = (ImageButton)getActivity().findViewById(R.id.flashOn);
        cameraView.setFlashButton(flash);
        flash.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                setFlash();
            }
        });
        //ImageView thumbnail = (ImageView)view.findViewById(R.id.thumbnail);
        //fetchMedia(thumbnail);
        return view;
    }

    boolean flashOn=false;
    private void setFlash()
    {
        if(!flashOn)
        {
            Log.d(TAG,"Flash on");
            if(cameraView.isFlashModeSupported(Camera.Parameters.FLASH_MODE_TORCH)) {
                flashOn = true;
                flash.setImageDrawable(getResources().getDrawable(R.drawable.flash_off));
            }
            else{
                Toast.makeText(getContext(),"Torch light not supported for this camera.",Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Log.d(TAG,"Flash off");
            flashOn=false;
            flash.setImageDrawable(getResources().getDrawable(R.drawable.flash_on));
        }
        cameraView.flashOnOff(flashOn);
    }

    private void fetchMedia(ImageView thumbnail)
    {
        String removableStoragePath;
        Log.d(TAG,"storage state = "+Environment.getExternalStorageState());
        File fileList[] = new File("/storage/").listFiles();
        //To find location of SD Card, if it exists
        for (File file : fileList)
        {
            if(!file.getAbsolutePath().equalsIgnoreCase(Environment.getExternalStorageDirectory().getAbsolutePath()) && file.isDirectory() && file.canRead()) {
                removableStoragePath = file.getAbsolutePath();
                Log.d(TAG,removableStoragePath);
                File newDir = new File(removableStoragePath+"/FC_Media");
                if(!newDir.exists()){
                    newDir.mkdir();
                }
                /*for(File file1 : new File(removableStoragePath).listFiles())
                {
                    Log.d(TAG,"SD Card path = "+file1.getPath());
                }*/
            }
        }
        //Storing in public folder. This will ensure that the files are visible in other apps as well.
        File root = getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        Log.d(TAG,root.getPath());
        File fc = new File(root.getPath()+"/FlipCam");
        if(!fc.exists()){
            fc.mkdir();
            Log.d(TAG,"FlipCam dir created");
            File videos = new File(fc.getPath()+"/FC_Videos");
            if(!videos.exists()){
                videos.mkdir();
                Log.d(TAG,"Videos dir created");
            }
            File images = new File(fc.getPath()+"/FC_Images");
            if(!images.exists()){
                images.mkdir();
                Log.d(TAG,"Images dir created");
            }
        }
        //Use this for sharing files between apps
        File videos = new File(root.getPath()+"/FlipCam/FC_Videos/");
        if(videos.listFiles() != null){
            Log.d(TAG,"List = "+videos.listFiles());
            Log.d(TAG,"List length = "+videos.listFiles().length);
            for(File file : videos.listFiles()){
                Log.d(TAG,file.getPath());
                /*final Bitmap img = BitmapFactory.decodeFile(file.getPath());
                thumbnail.setImageBitmap(img.createScaledBitmap(img,68,68,false));*/
                final String imgPath = file.getPath();
                Bitmap vid = ThumbnailUtils.createVideoThumbnail(file.getPath(), MediaStore.Video.Thumbnails.MICRO_KIND);
                thumbnail.setImageBitmap(vid.createScaledBitmap(vid,68,68,false));
                thumbnail.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view)
                    {
                        openMedia(imgPath);
                    }
                });
                break;
            }
        }
    }

    private void openMedia(String path)
    {
        setCameraClose();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://"+path),"video/*");
        startActivity(intent);
    }

    private void setCameraClose()
    {
        //Set this if you want to continue when the launcher activity resumes.
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(FC_SHARED_PREFERENCE,Context.MODE_PRIVATE).edit();
        editor.putBoolean("startCamera",false);
        editor.commit();
    }

    private void setCameraQuit()
    {
        //Set this if you want to quit the app when launcher activity resumes.
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(FC_SHARED_PREFERENCE,Context.MODE_PRIVATE).edit();
        editor.putBoolean("startCamera",true);
        editor.commit();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"Fragment destroy...app is being minimized");
        setCameraClose();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        Log.d(TAG,"Fragment stop...app is out of focus");
        super.onStop();
    }

    @Override
    public void onPause() {
        Log.d(TAG,"Fragment pause....app is being quit");
        setCameraQuit();
        super.onPause();
    }
}
