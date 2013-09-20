package com.example.TestApplication;

import android.util.Log;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class CelestialObject
{
    private int[] color;            //Defines RGB values for color rendering
    private int planetRadius;	    //Radius of the planet
    private float hourOfDay;        //Day length of planet
    private float dayOfYear;        //Rotational period around center of system
    private float day;		        //max hours per day
    private float year;		        //max days per year

    private int orbitRadius;	    //Orbit radius from center of system
    private float angle;	        //Current rotation angle around center of system
    //float delta;	        //Change in rotation angle for update

    //OpenGL ES support ---------------------------------------------------

    private FloatBuffer orbitalPathBuffer;
    private float[] orbitalPathVertices;

    private FloatBuffer axisBuffer;
    private float[] axisVertices =
    {
            0.0f, 500.0f, 0.0f,
            0.0f, -500.0f,0.0f
    };

    private FloatBuffer sphereBuffer;
    private FloatBuffer sphereIndexBuffer;
    private float[] sphereVertices;
    private float[] sphereIndecies;


    public CelestialObject(int red, int green, int blue, int planetRadius, int orbitRadius, float year, float day)
    {
        color = new int[3];
        color[0] = red;
        color[1] = green;
        color[2] = blue;

        this.planetRadius = planetRadius;
        this.orbitRadius = orbitRadius;
        //this.delta = delta;
        angle = 0;

        hourOfDay = 0;
        dayOfYear = 0;
        this.year = year;
        this.day = day;

        //OpenGL ES support -----------------------------------------

        //Setup axis vertex array buffer. Vertices in float. A float has 4 bytes
        ByteBuffer axisbb = ByteBuffer.allocateDirect(axisVertices.length * 4);
        axisbb.order(ByteOrder.nativeOrder());//use native byte order
        axisBuffer = axisbb.asFloatBuffer();  //Convert from byte to float
        axisBuffer.put(axisVertices);      //Copy data into buffer
        axisBuffer.position(0);            //rewind?


        //Setup orbital path vertex array buffer
        orbitalPathVertices = MakeCircle2d(100, orbitRadius, 0, 0);
        ByteBuffer orbitbb = ByteBuffer.allocateDirect(orbitalPathVertices.length * 4);
        orbitbb.order(ByteOrder.nativeOrder());
        orbitalPathBuffer = orbitbb.asFloatBuffer();
        orbitalPathBuffer.put(orbitalPathVertices);
        orbitalPathBuffer.position(0);

        //Setup sphere vertices
        sphereVertices = MakeSphere3d(50, 100, planetRadius);
        ByteBuffer spherebb = ByteBuffer.allocateDirect(sphereVertices.length * 4);
        spherebb.order(ByteOrder.nativeOrder());
        sphereBuffer = spherebb.asFloatBuffer();
        sphereBuffer.put(sphereVertices);
        sphereBuffer.position(0);

        ByteBuffer sphereIndexbb = ByteBuffer.allocateDirect(sphereIndecies.length * 4);
        sphereIndexbb.order(ByteOrder.nativeOrder());
        sphereIndexBuffer = sphereIndexbb.asFloatBuffer();
        sphereIndexBuffer.put(sphereIndecies);
        sphereIndexBuffer.position(0);
    }

    public static float[] MakeCircle2d(int vertexCount, float radius, float center_x, float center_y)
    {
        //create a buffer for vertex data
        float buffer[] = new float[vertexCount*2]; // (x,y) for each vertex
        int idx = 0;

        for (int i = 0; i < vertexCount; ++i)
        {
            float percent = (i / (float) (vertexCount-1));
            float rad = (float) (percent * 2*Math.PI);

            //vertex position
            double outer_x = center_x + radius * Math.cos(rad);
            double outer_y = center_y + radius * Math.sin(rad);

            buffer[idx++] = (float) outer_x;
            buffer[idx++] = (float) outer_y;
        }

        return buffer;
    }

    public float[] MakeSphere3d(int slices, int verticesPerSlice, float radius)
    {
        //create buffer for vertex data
        // x, y, z per vertex
        // verticesPerSlice = total no. of points per circle drawn
        // slices = total no. of circles drawn
        //Example: 20 slices, 100pts per slice, 3 values for x,y,z
        float vertices[] = new float[slices * verticesPerSlice * 3];
        Log.d("CelestialObject", "Sphere vertices array length: "+vertices.length);
        int index = 0;

        for(int j = 0; j < slices; j++)
        {
            Log.d("CelestialObject", "Slice: "+j);
            float rotationAngle = j / (float) (slices -1);
            float theta = (float) (rotationAngle * Math.PI);
            Log.d("CelestialObject", "Theta: "+theta);

            for (int i = 0; i < verticesPerSlice; i++)
            {
                float circleAngle = i / (float) (verticesPerSlice -1);
                float phi = (float) (circleAngle * 2*Math.PI);

                // x value
                vertices[index++] = (float) (radius * Math.sin(phi) * Math.cos(theta)); // x=r *cos(theta)
                // y value
                vertices[index++] = (float) (radius * Math.sin(phi) * Math.sin(theta));// y= r*sin(theta)

                // z value
                vertices[index++] = (float) (radius * Math.cos(phi));
            }
        }

        sphereIndecies = new float[slices * verticesPerSlice * 2];
        index = 0;
        for(int j=0; j<slices; j++)
        {
            for(int i=0; i<verticesPerSlice; i++)
            {
                sphereIndecies[index++] = ((float) ((j * slices) + (i % slices)));
                sphereIndecies[index++] = ((float) (((j + 1) * slices) + (i % slices)));
            }
        }

        return vertices;
    }

    public void render(GL10 gl)
    {
        //Can no longer define individual vertices w/glVertex command
        //Instead have to use two step process:
        //  1. Define x,y,z location of vertices in java array
        //  2. Transfer vertex data into a buffer

        //Render axis of rotation & orbital path
        //Enable the vertex-array and define the buffers
        gl.glFrontFace(GL10.GL_CCW);    // Front face in counter-clockwise orientation
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        //Draw the primitives via index-array
        gl.glPushMatrix();
            gl.glColor4f(1f, 1f, 1f, 1f);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, axisBuffer);
            gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, axisVertices.length / 3);  //Draw object's rotation axis

            gl.glRotatef(90, 1, 0, 0);
            gl.glVertexPointer(2, GL10.GL_FLOAT, 0, orbitalPathBuffer);
            gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, orbitalPathVertices.length/2);//Draw object's orbital path
        gl.glPopMatrix();

        //Render planet
        gl.glColor4f(color[0], color[1], color[2], 1);


        gl.glRotatef((float) (360.0 * dayOfYear / year), 0, 1, 0);//rotate
        gl.glTranslatef(orbitRadius, 0, 0);

        gl.glPushMatrix();
        gl.glRotatef(360 * hourOfDay / day, 0, 1, 0); //Rotate planet on its axis
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, sphereBuffer);
            gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, sphereVertices.length / 3);
            //gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, sphereIndexBuffer.capacity(), GL10.GL_FLOAT, sphereIndexBuffer);
        gl.glPopMatrix();

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

    }//end render

    public void update(float stepMult)
    {
        hourOfDay += stepMult;
        dayOfYear += stepMult/24.0;

        hourOfDay = hourOfDay - ((int)(hourOfDay/day))*day;
        dayOfYear = dayOfYear - ((int)(dayOfYear/year))*year;
    }
}
