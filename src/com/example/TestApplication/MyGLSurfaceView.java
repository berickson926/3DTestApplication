package com.example.TestApplication;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class MyGLSurfaceView extends GLSurfaceView
{
    public MyGLSurfaceView(Context context)
    {
        super(context);

        //Create an OpenGL 2.0 context
        setEGLContextClientVersion(2);

        //Set the renderer for drawing on the GLSurfaceView
        //setRenderer(new MyGLRenderer());

        //Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }
}
