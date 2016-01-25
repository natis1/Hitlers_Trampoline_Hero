import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class BasePanel extends JPanel implements MouseListener
{
    protected double universalScalar;

    protected double now;
    private int computerHZ;
    protected boolean is4K;
    private int lastSecondTime;
    protected double lastUpdateTime;

    public static double speedMultiplier;
    double TIME_BETWEEN_UPDATES;

    //If you're worried about visual hitches more than perfect timing, set this to 1.
    final int MAX_UPDATES_BEFORE_RENDER = 100;

    //This caps the game framerate, which means that the game doesn't use delta-T
    //For calculating movement. I think this is fine if we set the cap at something like 144hz (~7)
    //Smooth enough for most monitors even if eyes can still see past it.

    //3- Draw all, 2- No useless sprites, 1- No moving background, 0- TBD when we need more GPU capabilities.
    protected int graphicsQuality = 3;

    private int frameCatchup = 0;

    public BasePanel(double scalar, int monitorHZ)
    {
        universalScalar = scalar;
        computerHZ = monitorHZ;
        TIME_BETWEEN_UPDATES = 1000000000 / computerHZ;

        speedMultiplier = (double) (60) / (double) computerHZ; //designed for 60, compensates for everything else.

        //100% working on every multiple of 60, everything except background works perfectly on any other number.
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        doDrawing(g);

        Toolkit.getDefaultToolkit().sync();
    }

    protected void doDrawing(Graphics g)
    {
        Graphics2D g2d = (Graphics2D) g;
        g2d.scale(universalScalar, universalScalar);
    }

    //Starts a new thread and runs the game loop in it.
    public void runLoop()
    {
        Thread loop = new Thread()
        {
            public void run()
            {
                loop();
            }
        };
        loop.start();
    }

    private void render(double TARGET_TIME_BETWEEN_RENDERS)
    {
        now = System.nanoTime();
        int updateCount = 0;

        //Do as many game updates as we need to, potentially playing catchup.
        while(now - lastUpdateTime > TIME_BETWEEN_UPDATES && updateCount < MAX_UPDATES_BEFORE_RENDER)
        {
            update();
            lastUpdateTime += TIME_BETWEEN_UPDATES;
            updateCount++;
            if (updateCount > 15 && graphicsQuality > 2) //TODO: Get rid of magic numbers in this class? Replace them with static constants maybe?
            {
                graphicsQuality--;
                frameCatchup = (int) (300 / speedMultiplier);
            }
        }

        //If for some reason an update takes forever, we don't want to do an insane number of catchups.
        //If you were doing some sort of game that needed to keep EXACT time, you would get rid of this.
        if ( now - lastUpdateTime > TIME_BETWEEN_UPDATES)
        {
            lastUpdateTime = now - TIME_BETWEEN_UPDATES;
        }

        //Render. To do so, we need to calculate interpolation for a smooth render.
        double lastRenderTime = now;

        //Update the frames we got.
        int thisSecond = (int) (lastUpdateTime / 1000000000);
        if (thisSecond > lastSecondTime)
        {
            //TODO System.out.println("Main: NEW SECOND " + thisSecond + " " + frame_count);
            lastSecondTime = thisSecond;
        }

        //Yield until it has been at least the target time between renders. This saves the CPU from hogging.
        while ( now - lastRenderTime < TARGET_TIME_BETWEEN_RENDERS)
        {
            if (graphicsQuality < 3){
                frameCatchup--;
            }
            if (frameCatchup < 0){
                graphicsQuality = 3; // Good job team, you did it.
            }

            Thread.yield();

            try {
                Thread.sleep(1);

            } catch(Exception e) {
                e.printStackTrace();
            }
            now = System.nanoTime();
        }

    }

    public void loop() //TODO: Break the contents of the loop into many smaller functions?
    {
        //We will need the last update time.
        lastUpdateTime = System.nanoTime();

        //If we are able to get as high as this FPS, don't render again.
        final double TARGET_FPS = computerHZ;
        final double TARGET_TIME_BETWEEN_RENDERS = 1000000000 / TARGET_FPS; //TODO: Magic number removal service

        //Simple way of finding FPS.
        lastSecondTime = (int) (lastUpdateTime / 1000000000);

        while (true) //TODO: Implement a break condition?
        {
            render(TARGET_TIME_BETWEEN_RENDERS);
        }
    }


    protected void drawGame(float interpolation)
    {
        repaint();
    }


    public void update()
    {
        if (graphicsQuality > 2){
            updateParticles();
        }

        float interpolation = Math.min(1.0f, (float) ((now - lastUpdateTime) / TIME_BETWEEN_UPDATES) );
        drawGame(interpolation);
    }

    protected void updateParticles()
    {
        //TODO ADD ACTUAL PARTICLES
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
    }

    @Override
    public void mouseReleased(MouseEvent me)
    {
        System.out.println(me.getX() / universalScalar);
        System.out.println(me.getY() / universalScalar);

        if ((me.getX() / universalScalar) > 1030 && (me.getY() / universalScalar < 316))
        {
            //1 = start windows game new.
            Main.ElvenGameState = 2;
            //TODO ADD SOMETHING HERE (Something?)
        }
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
    }

    private class TAdapter extends KeyAdapter
    {
        //TODO KEYBOARD SUPPORT
    }
}