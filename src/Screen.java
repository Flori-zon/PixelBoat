import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;

abstract class Screen implements MouseListener, MouseMotionListener, KeyListener, ComponentListener {

    Window window;
    Dimension size;

    // input
    Input input, tickInput;

    // output
    Color background;
    Canvas canvas;
    BufferStrategy bs;
    Graphics2D g;

    Screen(Window window) {
        this.window = window;
        this.background = getBackground();
        this.size = canvas.getSize();
        this.canvas = new Canvas();

        window.add(canvas);
        window.setTitle("PixelProject: " + getName());
        window.addMouseListener(this);
        window.addMouseMotionListener(this);
        window.addKeyListener(this);
        canvas.addComponentListener(this);
    }

    public static class Input {
        Point mouse;
        List<Integer>
                mbPressed, mbReleased,
                bPressed, bReleased;

        Input() {
            mbPressed = new ArrayList<>();
            mbReleased = new ArrayList<>();
            bPressed = new ArrayList<>();
            bReleased = new ArrayList<>();
        }
    }

    void tick() {
        tickInput = input;

        tickScreen();

        input = new Input();
    }

    void draw() {
        if ((bs = canvas.getBufferStrategy()) == null) {
            canvas.createBufferStrategy(2);
            bs = canvas.getBufferStrategy();
        }
        g = (Graphics2D) bs.getDrawGraphics();

        g.setColor(background);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        drawScreen();

        g.dispose();
        bs.show();
    }

    @NotNull
    abstract String getName();

    @NotNull
    abstract Color getBackground();

    abstract void tickScreen();

    abstract void drawScreen();

    public void mouseMoved(MouseEvent e) {
        input.mouse = canvas.getMousePosition();
    }

    public void mousePressed(MouseEvent e) {
        input.mbPressed.add(e.getButton());
    }

    public void mouseReleased(MouseEvent e) {
        input.mbReleased.add(e.getButton());
    }

    public void keyPressed(KeyEvent e) {
        input.bPressed.add(e.getKeyCode());
    }

    public void keyReleased(KeyEvent e) {
        input.bReleased.add(e.getKeyCode());
    }

    public void componentResized(ComponentEvent e) {
        size = canvas.getSize();
    }

    // unused

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
    }

    public void componentHidden(ComponentEvent e) {
    }

}