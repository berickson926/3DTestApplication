package com.example.TestApplication;

import android.app.Fragment;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TestAnimationFragment extends Fragment
{
    private GLSurfaceView glView;

//    @Override
//    public void onStart(Bundle bundle)
//    {
//        super.onCreate(bundle);
//
//        glView = (GLSurfaceView) getView().findViewById(R.id.surfaceView);
//        glView.setRenderer(new MyGLRenderer(getActivity(), (TextView) (getView().findViewById(R.id.fps_tv))));//Using a custom OpenGL renderer
//        glView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
//    }

    public static Fragment newInstance()
    {
       Fragment TestAnimationFragment = new TestAnimationFragment();

        return TestAnimationFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container,savedInstanceState);

        glView = (GLSurfaceView) getView().findViewById(R.id.surfaceView);
        glView.setRenderer(new MyGLRenderer(getActivity(), (TextView) (getView().findViewById(R.id.fps_tv))));//Using a custom OpenGL renderer
        glView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        return inflater.inflate(R.layout.test_application_main, container, false);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        glView.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        glView.onResume();
    }
}
