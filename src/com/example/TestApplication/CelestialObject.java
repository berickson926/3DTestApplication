package com.example.TestApplication;

import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

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

    private FloatBuffer sphereVertexBuffer;
    private ByteBuffer sphereIndexBuffer;
    private float[] vertexPositionData;
    private byte[] indexData;

    //For sphere lighting
    private FloatBuffer normalBuffer;
    private float[] normalData;


    public CelestialObject(final int red, final int green, final int blue, final int planetRadius, final int orbitRadius, final float year, final float day)
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
        axisBuffer.put(axisVertices);         //Copy data into buffer
        axisBuffer.position(0);               //rewind?


        //Setup orbital path vertex array buffer
        orbitalPathVertices = MakeCircle2d(100, orbitRadius, 0, 0);
        ByteBuffer orbitbb = ByteBuffer.allocateDirect(orbitalPathVertices.length * 4);
        orbitbb.order(ByteOrder.nativeOrder());
        orbitalPathBuffer = orbitbb.asFloatBuffer();
        orbitalPathBuffer.put(orbitalPathVertices);
        orbitalPathBuffer.position(0);

        //Setup sphere vertices

        MakeSphere3d(planetRadius);
        ByteBuffer sphereVertexBB = ByteBuffer.allocateDirect(vertexPositionData.length * 4);
        sphereVertexBB.order(ByteOrder.nativeOrder());
        sphereVertexBuffer = sphereVertexBB.asFloatBuffer();
        sphereVertexBuffer.put(vertexPositionData);
        sphereVertexBuffer.position(0);

        ByteBuffer byteBuf = ByteBuffer.allocateDirect(normalData.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        normalBuffer = byteBuf.asFloatBuffer();
        normalBuffer.put(normalData);
        normalBuffer.position(0);

        sphereIndexBuffer = ByteBuffer.allocateDirect(indexData.length);
        sphereIndexBuffer.put(indexData);
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

    public void MakeSphere3d(final float radius)
    {
        final int latitudeBands = 100;
        final int longitudeBands = 100;

        vertexPositionData = new float[3 * latitudeBands * longitudeBands];
        normalData = new float[3 * latitudeBands * longitudeBands];
        int normalIndex = 0;
        int vertexIndex = 0;

        for(int latNumber = 0; latNumber < latitudeBands; latNumber++)
        {
            final float theta = (float) (latNumber * Math.PI) / latitudeBands;
            final float sinTheta = (float) Math.sin(theta);
            final float cosTheta = (float) Math.cos(theta);

            for(int longNumber = 0; longNumber < longitudeBands; longNumber++)
            {
                final float phi = (float) (longNumber * 2 * Math.PI / longitudeBands);
                final float sinPhi = (float) Math.sin(phi);
                final float cosPhi = (float) Math.cos(phi);

                final float x = cosPhi * sinTheta;
                final float y = cosTheta;
                final float z = sinPhi * sinTheta;

                normalData[normalIndex++] = x;
                normalData[normalIndex++] = y;
                normalData[normalIndex++] = z;

                vertexPositionData[vertexIndex++] = radius * x;
                vertexPositionData[vertexIndex++] = radius * y;
                vertexPositionData[vertexIndex++] = radius * z;
            }
        }

        //stitch vertices together, a list of vertex data that contains sequences of six values,
        //  each representing a square expressed as a pair of triangles
        indexData = new byte[latitudeBands * longitudeBands * 6];
        int index = 0;

        for(int latNumber = 0; latNumber < latitudeBands; latNumber++)
        {
            for(int longNumber = 0; longNumber < longitudeBands; longNumber++)
            {
                final float first = (latNumber * (longitudeBands +1)) + longNumber;
                final float second = first + longitudeBands + 1;

                indexData[index++] = (byte) first;
                indexData[index++] = (byte) second;
                indexData[index++] = (byte) (first +1);

                indexData[index++] = (byte) second;
                indexData[index++] = (byte) (second + 1);
                indexData[index++] = (byte) (first + 1);
            }
        }
    }

    public void render(GL10 gl)
    {
        //Can no longer define individual vertices w/glVertex command
        //Instead have to use two step process:
        //  1. Define x,y,z location of vertices in java array
        //  2. Transfer vertex data into a buffer

        //Render axis of rotation & orbital path
        //Enable the vertex-array and define the buffers
        gl.glFrontFace(GL10.GL_CW);    // Front face in clockwise orientation
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);

        //Draw the primitives via index-array
        gl.glPushMatrix();
            gl.glDisable(GL10.GL_LIGHTING);
            gl.glColor4f(1f, 1f, 1f, 1f);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, axisBuffer);
            gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, axisVertices.length / 3);  //Draw object's rotation axis

            gl.glRotatef(90, 1, 0, 0);
            gl.glVertexPointer(2, GL10.GL_FLOAT, 0, orbitalPathBuffer);
            gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, orbitalPathVertices.length/2);//Draw object's orbital path
            gl.glEnable(GL10.GL_LIGHTING);
        gl.glPopMatrix();

        //Render planet
        gl.glColor4f(color[0], color[1], color[2], 1);


        gl.glRotatef((float) (360.0 * dayOfYear / year), 0, 1, 0);//rotate
        gl.glTranslatef(orbitRadius, 0, 0);

        gl.glPushMatrix();
        gl.glRotatef(360 * hourOfDay / day, 0, 1, 0); //Rotate planet on its axis
            gl.glEnable(GL10.GL_CULL_FACE);
            gl.glCullFace(GL10.GL_BACK);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, sphereVertexBuffer);
            gl.glNormalPointer(GL10.GL_FLOAT, 0, normalBuffer);
            gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, vertexPositionData.length / 3);
        gl.glPopMatrix();

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
        gl.glDisable(GL10.GL_CULL_FACE);

    }//end render

    public void update(float stepMult)
    {
        hourOfDay += stepMult;
        dayOfYear += stepMult/24.0;

        hourOfDay = hourOfDay - ((int)(hourOfDay/day))*day;
        dayOfYear = dayOfYear - ((int)(dayOfYear/year))*year;
    }
}
