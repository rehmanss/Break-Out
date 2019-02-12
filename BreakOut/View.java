import javax.swing.ImageIcon;
import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

//additonal classes
import java.awt.*;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Displays a graphical view of the game of breakout.
 *  Uses Graphics2D would need to be re-implemented for Android.
 * @author Mike Smith University of Brighton
 */
public class View extends JFrame implements Observer
{ 
    private Controller controller;
    private GameObj   bat;            // The bat
    private GameObj   ball;           // The ball
    private List<GameObj> bricks;     // The bricks

    private int       score =  0;     // The score
    private int       frames = 0;     // Frames output
    private int        lives = 5;     // lives
    private int        bricksLeft;    //  live bricks

    public final int width;  // Size of screen Width
    public final int height;  // Sizeof screen Height

    /**
     * Construct the view of the game
     * @param width Width of the view pixels
     * @param height Height of the view pixels
     */
    public View(int width, int height)
    {
        this.width = width; this.height = height;

        setSize(width, height);                 // Size of window
        addKeyListener( new Transaction() );    // Called when key press
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Timer.startTimer();
    }

    /**
     *  Code called to draw the current state of the game
     *   Uses draw:       Draw a shape
     *        fill:       Fill the shape
     *        setPaint:   Colour used
     *        drawString: Write string on display
     *  @param g Graphics context to use
     */
    public void drawActualPicture( Graphics2D g )
    {
        final int  RESET_AFTER = 200; // Movements
        frames++;
        synchronized( Model.class )   // Make thread safe
        {
            if (lives == 0)
            {
                g.setPaint( Color.BLACK);
                g.fill( new Rectangle2D.Float( 0, 0, width, height) );
                g.setPaint( Color.white );
               Font font = new Font("Monospaced",Font.BOLD,50); 
               FontMetrics fm = getFontMetrics( font );
               g.setFont( font );
               String gameovermsg = "GAME OVER!"; 
                g.drawString( gameovermsg, width /2-fm.stringWidth(gameovermsg)/2, 80  );
                
            } else if (bricksLeft == 0) {
                g.setPaint( Color.BLACK);
                g.fill( new Rectangle2D.Float( 0, 0, width, height) );
                g.setPaint( Color.white );
               Font font = new Font("Monospaced",Font.BOLD,50); 
               FontMetrics fm = getFontMetrics( font );
               g.setFont( font );
               String Winmsg = "YOU'VE WON!"; 
                g.drawString( Winmsg, width /2-fm.stringWidth(Winmsg)/2, 80  );
            }else {
            
            // WHITE background
            g.setPaint( Color.WHITE );
            g.fill( new Rectangle2D.Float( 0, 0, width, height) );

            //background image
            g.drawImage( background_img, 0, 0, this ); 

            //stats display
            Font font = new Font("Monospaced",Font.BOLD,14); 
            g.setFont( font );

            displayGameObj( g, ball );   // Display the Ball
            displayGameObj( g, bat  );   // Display the Bat

            // *[4]****************************************************[4]*
            // * Display the bricks that make up the game                 *
            
            

            for(int i = 0; i < bricks.size(); i++){
                if (bricks.get(i).isVisible()) {
                    displayGameObj(g, bricks.get(i));
                    
                }
            }
            
            // Display state of game
            g.setPaint( Color.white );
            FontMetrics fm = getFontMetrics( font );
            String fmt = "BreakOut: Score = [%6d] fps=%5.1f   LIVES = [%2d] ";
            String text = String.format(fmt, score,frames/(Timer.timeTaken()/1000.0), lives);

            if ( frames > RESET_AFTER ) 
            { frames = 0; Timer.startTimer(); }

            g.drawString( text, width /2-fm.stringWidth(text)/2, 80  );
            }
        }
    }

    private void displayGameObj( Graphics2D g, GameObj go )
    {
        g.setColor( go.getColour().forSwing() );
        g.fill( new Rectangle2D.Float( go.getX(),     go.getY(), 
                go.getWidth(), go.getHeight() ) );
    }

    /**
     * Called indirectly from the model when its state has changed
     * @param aModel Model to be displayed
     * @param arg    Any arguments (Not used)
     */
    @Override
    public void update( Observable aModel, Object arg )
    {
        Model model = (Model) aModel;
        // Get from the model the ball, bat, bricks & score
        ball    = model.getBall();              // Ball
        bricks  = model.getBricks();            // Bricks
        bat     = model.getBat();               // Bat
        score   = model.getScore();             // Score
        lives = model.getLives();               //lifes
        bricksLeft = model.getBricksLeft();     //bricksLeft
        //Debug.trace("Update");
        repaint();                              // Re draw game
    }

    /**
     * Called by repaint to redraw the Model
     * @param g    Graphics context
     */
    @Override
    public void update( Graphics g )          // Called by repaint
    {
        drawPicture( (Graphics2D) g );          // Draw Picture
    }

    /**
     * Called when window is first shown or damaged
     * @param g    Graphics context
     */
    @Override
    public void paint( Graphics g )           // When 'Window' is first
    {                                         //  shown or damaged
        drawPicture( (Graphics2D) g );          // Draw Picture
    }

    private BufferedImage theAI;              // Alternate Image
    private Graphics2D    theAG;              // Alternate Graphics

    private Image background_img; 

    /**
     * Double buffer graphics output to avoid flicker
     * @param g The graphics context
     */
    private void drawPicture( Graphics2D g )   // Double buffer
    {                                          //  to avoid flicker
        if ( bricks == null ) return;            // Race condition
        if (  theAG == null )
        {
            Dimension d = getSize();              // Size of curr. image
            theAI = (BufferedImage) createImage( width, height );
            theAG = theAI.createGraphics();

            ///////////
            background_img = Toolkit.getDefaultToolkit().createImage("background.jpg");
        }

        drawActualPicture( theAG );             // Draw Actual Picture
        g.drawImage( theAI, 0, 0, this );       //  Display on screen 

    }

    /**
     * Need to be told where the controller is
     * @param aPongController The controller used
     */
    public void setController(Controller aPongController)
    {
        controller = aPongController;
    }

    /**
     * Methods Called on a key press 
     *  calls the controller to process
     */
    private class Transaction implements KeyListener  // When character typed
    {
        @Override
        public void keyPressed(KeyEvent e)      // Obey this method
        {
            // Make -ve so not confused with normal characters
            controller.userKeyInteraction( -e.getKeyCode() );
        }

        @Override
        public void keyReleased(KeyEvent e)
        {
            // Called on key release including specials
        }

        @Override
        public void keyTyped(KeyEvent e)
        {
            // Send internal code for key
            controller.userKeyInteraction( e.getKeyChar() );
        }
    }
}
