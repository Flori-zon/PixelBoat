import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public abstract class UIScreen extends EngineScreen implements MouseListener, MouseMotionListener {

    private final View rootView;

    protected Point
            mousePos,
            mousePixelPos;
    protected boolean
            mouseMoved,
            mousePressed,
            mouseReleased;

    /* TODO
     * dialogs with following
     * Lists (scrollbar)
     * Button Text/Image
     * Text bars
     */

    public UIScreen(GameWindow window) {
        super(window);
        rootView = new View(this, null, 0, 0, 0, 0) {
            @Override
            void drawThis() {
            }
        };
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseMoved = true;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mousePressed = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseReleased = true;
    }

    public void tick() {
        //mouse pos
        mousePos = window.getContentPane().getMousePosition(true);
        if (mousePos == null)
            mousePixelPos = null;
        else
            mousePixelPos = new Point(
                    (int) ((float) mousePos.x / pixelWidth),
                    (int) ((float) mousePos.y / pixelHeight));

        //actions
        if (mouseMoved) {
            rootView.checkHover(mousePixelPos);
            mouseMoved = false;
        }
        if (mousePressed) {
            rootView.checkHold(true);
            mousePressed = false;
        }
        if (mouseReleased) {
            rootView.checkClick();
            rootView.checkHold(false);
            mouseReleased = false;
        }
    }

    public void render() {
        rootView.draw();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

}
