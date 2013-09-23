package com.example.TestApplication;

import javax.microedition.khronos.opengles.GL10;

public class SolarSystem
{
    enum State {RUNNING, STOPPED, STEP}

    State state;			//Manages state of the entire solar system update/rendering for user control
    float stepMultiplier;	//increase/decrease speed of celestial object rotation

    //Various celestial objects
    private CelestialObject sun;
    private CelestialObject earth;
    private CelestialObject moon;
    private CelestialObject planetX;
    private CelestialObject planetY;
    private CelestialObject planetZ;

    public SolarSystem()
    {
        stepMultiplier = 24;

        sun =	new CelestialObject(1,1,0, 50,0, 1,1);
        earth = new CelestialObject(0,0,1, 15,150, 365,24);
        moon =	new CelestialObject(1,1,1, 2,20, 30.4f, 1);

        planetX = new CelestialObject(1,0,1, 30, 300, 800,1);
        planetY = new CelestialObject(1,0,0, 15, 200, 200,1);
        planetZ = new CelestialObject(0,1,1, 30, 250, 900,1);

        state = State.RUNNING;

    }//end constructor

    void render(GL10 gl)
    {
        gl.glPushMatrix();
        gl.glDisable(GL10.GL_LIGHTING);
        sun.render(gl);
        gl.glEnable(GL10.GL_LIGHTING);
        gl.glPopMatrix();

        gl.glPushMatrix();
        earth.render(gl);
        moon.render(gl);
        gl.glPopMatrix();

        gl.glPushMatrix();
        planetX.render(gl);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glRotatef(120, 0, 0, 1);
        planetY.render(gl);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glRotatef(90, 1, 1, 1);
        planetZ.render(gl);
        gl.glPopMatrix();
    }//end render

    void update()
    {
        if(state == State.RUNNING || state== State.STEP)
        {
            earth.update(stepMultiplier);
            moon.update(stepMultiplier);

            planetX.update(stepMultiplier);
            planetY.update(stepMultiplier);
            planetZ.update(stepMultiplier);

            if(state == State.STEP)
            {
                state = State.STOPPED;
            }
        }
    }

    void toggleAnimation()
    {
        if(state != State.STOPPED)
        {
            state = State.STOPPED;
        }
        else
        {
            state = State.RUNNING;
        }
    }

    void singleStep(GL10 gl)
    {
        state = State.STEP;
        this.update();
        this.render(gl);
    }

    void increaseStep()
    {
        stepMultiplier = stepMultiplier * 2;
    }

    void decreaseStep()
    {
        stepMultiplier = stepMultiplier / 2;
    }
}
