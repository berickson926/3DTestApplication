package com.example.TestApplication;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class MyGLRenderer implements GLSurfaceView.Renderer
{

    private Context context;

    //Objects we want to render
    private SolarSystem solarSystem;
    private CelestialObject testPlanet;

//    //Used for FPS calculations
//    private long mLastTime;
//    private int mFPS;
//    private TextView fpsTextView;
//    private final Handler myHandler = new Handler();

    // Lighting
    boolean lightingEnabled = true;   // Is lighting on? (NEW)
    private float[] lightAmbient = {0.5f, 0.5f, 0f, 1.0f};
    private float[] lightDiffuse = {.5f, .5f, .5f, .5f};
    private float[] lightSpecular = {1f, 1f, 1f, 1f};
    private float[] lightPosition = {1000.0f, 0.0f, 0.0f, 1f};

    private float[] ambient = {0.3f, 0.3f, 0.3f, 0.5f};
    private float[] diffuse = {0.5f, 0.5f, 0.5f, 0.5f};
    private float[] specular = {0.5f, 0.5f, 0.5f, 0.5f};
    private float[] emission = {0.0f, 0.0f, 0.0f, 1.0f};
    private float[] shiness = {100.0f};

    // The buffers for our light values
    private FloatBuffer lightAmbientBuffer;
    private FloatBuffer lightDiffuseBuffer;
    private FloatBuffer lightPositionBuffer;
    private FloatBuffer lightSpecularBuffer;

    private FloatBuffer diffuseBuffer;
    private FloatBuffer specularBuffer;
    private FloatBuffer emissionBuffer;
    private FloatBuffer shinessBuffer;
    private FloatBuffer ambientBuffer;

    //public MyGLRenderer(Context context, TextView fpsTextView)
    public MyGLRenderer(Context context)
    {
        this.context = context;

//        mLastTime = System.currentTimeMillis();
//        mFPS = 0;
//        this.fpsTextView = fpsTextView;

        //light buffers
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(lightAmbient.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        lightAmbientBuffer = byteBuf.asFloatBuffer();
        lightAmbientBuffer.put(lightAmbient);
        lightAmbientBuffer.position(0);

        byteBuf = ByteBuffer.allocateDirect(lightDiffuse.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        lightDiffuseBuffer = byteBuf.asFloatBuffer();
        lightDiffuseBuffer.put(lightDiffuse);
        lightDiffuseBuffer.position(0);

        byteBuf = ByteBuffer.allocateDirect(lightSpecular.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        lightSpecularBuffer = byteBuf.asFloatBuffer();
        lightSpecularBuffer.put(lightSpecular);
        lightSpecularBuffer.position(0);

        byteBuf = ByteBuffer.allocateDirect(lightPosition.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        lightPositionBuffer = byteBuf.asFloatBuffer();
        lightPositionBuffer.put(lightPosition);
        lightPositionBuffer.position(0);

        //Material shading buffers
        byteBuf = ByteBuffer.allocateDirect(shiness.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        shinessBuffer = byteBuf.asFloatBuffer();
        shinessBuffer.put(shiness);
        shinessBuffer.position(0);

        byteBuf = ByteBuffer.allocateDirect(emission.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        emissionBuffer = byteBuf.asFloatBuffer();
        emissionBuffer.put(emission);
        emissionBuffer.position(0);

        byteBuf = ByteBuffer.allocateDirect(diffuse.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        diffuseBuffer = byteBuf.asFloatBuffer();
        diffuseBuffer.put(diffuse);
        diffuseBuffer.position(0);

        byteBuf = ByteBuffer.allocateDirect(specular.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        specularBuffer = byteBuf.asFloatBuffer();
        specularBuffer.put(specular);
        specularBuffer.position(0);

        byteBuf = ByteBuffer.allocateDirect(ambient.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        ambientBuffer = byteBuf.asFloatBuffer();
        ambientBuffer.put(ambient);
        ambientBuffer.position(0);
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
        testPlanet = new CelestialObject(1, 1, 0, 100, 1, 365, 24);

        //And there'll be light!
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbientBuffer);		//Setup The Ambient Light ( NEW )
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuseBuffer);		//Setup The Diffuse Light ( NEW )
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, lightSpecularBuffer);
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPositionBuffer);	//Position The Light ( NEW )
        gl.glEnable(GL10.GL_LIGHT0);											//Enable Light 0 ( NEW )

        gl.glMaterialfv(GL10.GL_FRONT, GL10.GL_AMBIENT, ambientBuffer);
        gl.glMaterialfv(GL10.GL_FRONT, GL10.GL_DIFFUSE, diffuseBuffer);
        gl.glMaterialfv(GL10.GL_FRONT, GL10.GL_SPECULAR, specularBuffer);
        gl.glMaterialfv(GL10.GL_FRONT, GL10.GL_SHININESS, shinessBuffer);
        gl.glMaterialfv(GL10.GL_FRONT, GL10.GL_EMISSION, emissionBuffer);
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

//        //FPS measurement
//
//        mFPS++;
//        long currentTime = System.currentTimeMillis();
//        if(currentTime >= mLastTime + 1000)
//        {
//
//            myHandler.post(new Runnable()
//            {
//                @Override
//                public void run()
//                {
//                    fpsTextView.setText("FPS: "+mFPS);
//                }
//            });
//
//            Log.d("MyGLRenderer", "FPS: "+mFPS);
//            mFPS =0;
//            mLastTime = currentTime;
//        }
    }
}
