import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Random;

import javax.swing.*;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * Model of the game of breakout
 * @author Mike Smith University of Brighton
 */

public class Model extends Observable
{
    // Boarder
    private static final int B              = 6;  // Border offset
    private static final int M              = 20; // Menu offset

    // Size of things
    private static final float BALL_SIZE    = 30; // Ball side
    private static final float BRICK_WIDTH  = 50; // Brick size
    private static final float BRICK_HEIGHT = 30;

    private static final int BAT_MOVE       = 10; // Distance to move bat

    // Scores
    private static final int HIT_BRICK      = 50;  // Score
    private static final int HIT_BOTTOM     = -200;// Score

    //check if game is over
    private boolean GameOver = false;

    private GameObj ball;          // The ball
    private List<GameObj> bricks;  // The bricks
    private GameObj bat;           // The bat

    private boolean runGame = true; // Game running
    private boolean fast = false;   // Sleep in run loop

    private int score = 0;
    private int lives = 5;
    private int bricksLeft;

    private final float W;         // Width of area
    private final float H;         // Height of area

    public Model( int width, int height )
    {
        this.W = width; this.H = height;
    }

    /**
     * Create in the model the objects that form the game
     */

    public void createGameObjects()
    {
        synchronized( Model.class )
        {
            ball   = new GameObj(W/2, H/2, BALL_SIZE, BALL_SIZE, Colour.RED );
            bat    = new GameObj(W/2, H - BRICK_HEIGHT*1.5f, BRICK_WIDTH*3,BRICK_HEIGHT/4, Colour.WHITE);
            bricks = new ArrayList<>();

            // *[1]******************************************************[1]*
            // * Fill in code to place the bricks on the board              *

             for (int row = 0; row < 10; row++) { 
                bricks.add(new GameObj((5*row*W/(BRICK_WIDTH)+5), 100, BRICK_WIDTH, BRICK_HEIGHT, Colour.GRAY));
                bricks.add(new GameObj((5*row*W/(BRICK_WIDTH)+5), 135, BRICK_WIDTH, BRICK_HEIGHT, Colour.GRAY));
                bricks.add(new GameObj((5*row*W/(BRICK_WIDTH)+5), 170, BRICK_WIDTH, BRICK_HEIGHT, Colour.GRAY));
                bricks.add(new GameObj((5*row*W/(BRICK_WIDTH)+5), 205, BRICK_WIDTH, BRICK_HEIGHT, Colour.GRAY));
                bricks.add(new GameObj((5*row*W/(BRICK_WIDTH)+5), 240, BRICK_WIDTH, BRICK_HEIGHT, Colour.GRAY));
                bricks.add(new GameObj((5*row*W/(BRICK_WIDTH)+5), 275, BRICK_WIDTH, BRICK_HEIGHT, Colour.GRAY));
             }
            
          
            bricksLeft = bricks.size();

            System.out.println("Bricks Created: "+ bricks.size());
            System.out.println();
        }
    }

    private ActivePart active  = null;
    /**
     * Start the continuous updates to the game
     */
    public void startGame()
    {
        synchronized ( Model.class )
        {
            stopGame();
            active = new ActivePart();
            Thread t = new Thread( active::runAsSeparateThread );
            t.setDaemon(true);   // So may die when program exits
            t.start();
            playMusic("Music.wav");
        }
    }

    /**
     * Stop the continuous updates to the game
     * Will freeze the game, and let the thread die.
     */
    public void stopGame()
    {  
        synchronized ( Model.class )
        {
            if ( active != null ) { active.stop(); active = null; }
        }
    }

    public int getBricksLeft() {
        return bricksLeft;
    }
    
    public GameObj getBat()            
    { return bat; }

    public GameObj getBall()           
    { return ball; }

    public List<GameObj> getBricks()    
    { return bricks; }

    /**
     * Add to score n units
     * @param n units to add to score
     */
    protected void addToScore(int n)    
    { score += n; }

    public int getScore()               
    { return score; }

    protected void MinusLife(int n)    
    { lives -= n; }

    public int getLives()               
    { return lives; }

    /**
     * Set speed of ball to be fast (true/ false)
     * @param fast Set to true if require fast moving ball
     */
    public void setFast(boolean fast)   
    { 
        this.fast = fast; 
    }

    /**
     * Move the bat. (-1) is left or (+1) is right
     * @param direction - The direction to move
     */
    public void moveBat( int direction )
    {
        // *[2]******************************************************[2]*
        // * Fill in code to prevent the bat being moved off the screen *
        // **************************************************************

        float dist = direction * BAT_MOVE;    // Actual distance to move

        if (direction == -1) //left
        {
            if (bat.getX() <= 0) {
                dist = 0;
            }
        }

        if (direction == 1) //right
        {
            if (bat.getX() >=  W - bat.getWidth()) {
                dist = 0;
            }
        }

        Debug.trace( "Model: Move bat = %6.2f", dist );
        bat.moveX(dist);
    }

    /**
     * This method is run in a separate thread
     * Consequence: Potential concurrent access to shared variables in the class
     */
    class ActivePart
    {
        private boolean runGame = true;

        public void stop()
        {
            runGame = false;
        }

        public void runAsSeparateThread()
        {
            float S = 3; // Units to move (Speed)
            float upperLimit = 5;
            try
            {
                synchronized ( Model.class ) // Make thread safe
                {
                    GameObj       ball   = getBall();     // Ball in game
                    GameObj       bat    = getBat();      // Bat
                    List<GameObj> bricks = getBricks();   // Bricks
                }

                while (runGame)
                {
                    synchronized ( Model.class ) // Make thread safe
                    {
                        float x = ball.getX();  // Current x,y position
                        float y = ball.getY();
                        // Deal with possible edge of board hit
                        if (x >= W - B - BALL_SIZE)  
                            ball.changeDirectionX();

                        if (x <= 0 + B            )  
                            ball.changeDirectionX();

                        if (y >= H - B - BALL_SIZE)  // Bottom
                        { 
                            ball.changeDirectionY(); 
                            addToScore( HIT_BOTTOM ); 
                            MinusLife(1);
                            System.out.println("Model: Bottom HIT");
                            System.out.println("Model: Lives: " + lives);
                            playSound("BottomHit.wav");
                        }

                        if (y <= 0 + M            ) 
                            ball.changeDirectionY();

                        // As only a hit on the bat/ball is detected it is 
                        //  assumed to be on the top or bottom of the object.
                        // A hit on the left or right of the object
                        //  has an interesting affect

                        boolean hit = false;

                        // *[3]******************************************************[3]*
                        // * Fill in code to check if a visible brick has been hit      *
                        // *      The ball has no effect on an invisible brick          *
                        // **************************************************************

                        for (int i = 0; i < bricks.size(); i++) {
                            if (bricks.get(i).isVisible())
                                if (bricks.get(i).hitBy(ball))
                                {
                                    //check brick is out of lives
                                    //if it is, then check if all other bricks are out of lives
                                    
                                    bricks.get(i).brickHit();
                                    if (bricks.get(i).getbricksLive() == 0) {
                                        bricksLeft--;
                                        //Now we check if all other bricks are destroyed
                                        boolean cont = false;
                                        for (int z = 0; z < bricks.size(); z++) {
                                            if (bricks.get(z).getbricksLive() != 0) {
                                                cont = true;
                                                break;
                                            }
                                        }
                                        
                                        if (!cont) {
                                            System.out.println("You have won!");
                                            stopGame();
                                        }
                                    }
                                    
                                    ball.changeDirectionY();
                                    addToScore(HIT_BRICK);
                                    
                                   // bricks.get(i).brickHit();
                                    if (S < upperLimit) S += 0.2;
                                    // bricks.get(i).setVisibility(false);

                                    System.out.println("Brick HIT");
                                    playSound("BrickHit.wav");
                                }
                        }

                        if (lives<= 0) //stop game if no lives left
                        {
                            stopGame();
                            GameOver = true;
                            System.out.println("GAME OVER");
                            playSound("GameOver.wav");
                        }

                        if (hit)
                            ball.changeDirectionY();

                        if ( ball.hitBy(bat) ){ 
                            ball.changeDirectionY();
                            System.out.println("Bat HIT");
                            playSound("BatHit.wav");
                        }

                    }
                    modelChanged();      // Model changed refresh screen
                    Thread.sleep( fast ? 2 : 20 );
                    ball.moveX(S);  ball.moveY(S);
                }
            } catch (Exception e) 
            { 
                Debug.error("Model.runAsSeparateThread - Error\n%s", 
                    e.getMessage() );
            }
        }
    }

    /**
     * Model has changed so notify observers so that they
     *  can redraw the current state of the game
     */
    public void modelChanged()
    {
        setChanged(); notifyObservers();
    }

    public void playSound(String soundName)
    {
        try 
        {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(soundName).getAbsoluteFile( ));
            Clip clip = AudioSystem.getClip( );
            clip.open(audioInputStream);
            clip.start();
        }
        catch(Exception ex)
        {
            System.out.println("Error with playing sound.");
            ex.printStackTrace( );
        }
    }

    public void playMusic(String soundName)
    {

        try 
        {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(soundName).getAbsoluteFile( ));
            Clip clip = AudioSystem.getClip( );

            clip.open(audioInputStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            Thread.sleep(10000); // looping as long as this thread is alivek
        }
        catch(Exception ex)
        {
            System.out.println("Error with playing Music.");
            ex.printStackTrace( );
        }

    }
}
