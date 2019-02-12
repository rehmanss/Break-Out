import java.awt.event.KeyEvent;
/**
 * BreakOut controller, handles user interactions
 * @author Mike Smith University of Brighton
 */
public class Controller
{
    private Model model;   // Model of game
    private View  view;    // View of game
    int button_press = 1;

    public Controller(Model aBreakOutModel,
    View aBreakOutView )
    {
        model  = aBreakOutModel;
        view   = aBreakOutView;
        view.setController( this );    // View could talk to controller
    }

    /**
     * Decide what to do for each interaction from the user
     * Called from the interaction code in the view
     * @param keyCode The key pressed
     */
    public void userKeyInteraction(int keyCode )
    {
        // Key typed includes specials, -ve
        // Char is ASCII value
        switch ( keyCode )               // Character is
        {
            case -KeyEvent.VK_LEFT:        // Left Arrow
            model.moveBat( -1);
            break;

            case -KeyEvent.VK_RIGHT:       // Right arrow
            model.moveBat( +1 );
            break;

            case 'f' :             // Very fast ball movement now
            model.setFast( true );
            break;

            case 'n' :            // Normal speed
            model.setFast( false );
            break;

            case -KeyEvent.VK_SPACE:
            button_press++;
            if (button_press % 2 == 0)
                model.stopGame();
            else
                model.startGame();
            break;

            case 'r':             //new game
            break;

            default :
            Debug.trace( "Ch typed = %3d [%c]", keyCode, (char) keyCode );
        }
    }

}
