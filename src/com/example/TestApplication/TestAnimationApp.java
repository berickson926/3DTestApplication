package com.example.TestApplication;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.TextView;

public class TestAnimationApp extends Activity
{
    private GLSurfaceView glView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_application_main);

        glView = (GLSurfaceView) findViewById(R.id.surfaceView);
        //glView.setRenderer(new MyGLRenderer(this, (TextView)findViewById(R.id.fps_tv)));//Using a custom OpenGL renderer
        glView.setRenderer(new MyGLRenderer(this));
        glView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
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
