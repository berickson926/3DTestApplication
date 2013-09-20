package com.example.TestApplication;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer
{

    Context context;

    SolarSystem solarSystem;

    private long mLastTime;
    private int mFPS;
    private TextView fpsTextView;
    private final Handler myHandler = new Handler();

    // Lighting (NEW)
    boolean lightingEnabled = true;   // Is lighting on? (NEW)
    private float[] lightAmbient = {1f, 1f, 1f, 1.0f};
    private float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
    private float[] light_specular = {1.0f, 1.0f, 1.0f, 1.0f};
    private float[] lightPosition = {0.0f, 0.0f, 0.0f, 0.0f};

    private float[] ambient = {0.3f, 0.3f, 0.3f, 0.5f};
    private float[] diffuse = {0.5f, 0.5f, 0.5f, 0.5f};
    private float[] specular = {0.5f, 0.5f, 0.5f, 0.5f};
    private float[] emission = {0.0f, 0.0f, 0.0f, 1.0f};
    private float[] shiness = {50.0f};

    public MyGLRenderer(Context context, TextView fpsTextView)
    //public MyGLRenderer(Context context)
    {
        this.context = context;

        mLastTime = System.currentTimeMillis();
        mFPS = 0;
        this.fpsTextView = fpsTextView;
    }

    //Callback for when the surface is first created or recreated
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig)
    {
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);    // Set color's clear-value to black
        gl.glClearDepthf(1.0f);                     // Set depth's clear-value to farthest
        gl.glEnable(GL10.GL_DEPTH_TEST);            // Enables depth-buffer for hidden surface removal
        gl.glDepthFunc(GL10.GL_LEQUAL);             // The type of depth testing to do
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);  // nice perspective view
        gl.glShadeModel(GL10.GL_SMOOTH);            // Enable smooth shading of color
        gl.glDisable(GL10.GL_DITHER);               // Disable dithering for better performance

        //OpenGL|ES initialization code here
        solarSystem = new SolarSystem();

        // Setup lighting GL_LIGHT1 with ambient and diffuse lights (NEW)
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbient, 0);
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuse, 0);
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, light_specular, 0);
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPosition, 0);

        gl.glMaterialfv(GL10.GL_FRONT, GL10.GL_EMISSION, emission, 0);
        gl.glMaterialfv(GL10.GL_FRONT, GL10.GL_AMBIENT, ambient, 0);
        gl.glMaterialfv(GL10.GL_FRONT, GL10.GL_DIFFUSE, diffuse, 0 );
        gl.glMaterialfv(GL10.GL_FRONT, GL10.GL_SPECULAR, specular, 0 );
        gl.glMaterialfv(GL10.GL_FRONT, GL10.GL_SHININESS, shiness, 0 );

        //gl.glEnable(GL10.GL_LIGHT1);   // Enable Light 1 (NEW)
        gl.glEnable(GL10.GL_LIGHT0);   // Enable the default Light 0 (NEW)
    }

    // Call back after onSurfaceCreated() or whenever the window's size changes
    //Equivalent to using glutReshapeFunc(reshape)?
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        if (height == 0) height = 1;   // To prevent divide by zero

        // Set the viewport (display area) to cover the entire window
        gl.glViewport(0, 0, width, height);

        // Setup perspective projection, with aspect ratio matches viewport
        gl.glMatrixMode(GL10.GL_PROJECTION); // Select projection matrix
        gl.glLoadIdentity();                 // Reset projection matrix
        // Use perspective projection
        GLU.gluPerspective(gl, 60.0f,  width / height, 1.0f, 1000);

        gl.glMatrixMode(GL10.GL_MODELVIEW);  // Select model-view matrix
        gl.glLoadIdentity();                 // Reset

        GLU.gluLookAt(gl, 50,100,500, 0,0,0, 0,1,0);
    }

    @Override
    public void onDrawFrame(GL10 gl)
    {
        // Clear color and depth buffers using clear-value set earlier
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        // Enable lighting?
        if (lightingEnabled)
        {
            gl.glEnable(GL10.GL_LIGHTING);
            gl.glEnable(GL10.GL_COLOR_MATERIAL);
        }
        else
        {
            gl.glDisable(GL10.GL_LIGHTING);
            gl.glDisable(GL10.GL_COLOR_MATERIAL);
        }

        solarSystem.update();
        solarSystem.render(gl);

        //FPS measurement

        mFPS++;
        long currentTime = System.currentTimeMillis();
        if(currentTime >= mLastTime + 1000)
        {

            myHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    fpsTextView.setText("FPS: "+mFPS);
                }
            });

            Log.d("MyGLRenderer", "FPS: "+mFPS);
            mFPS =0;
            mLastTime = currentTime;
        }
    }
}
