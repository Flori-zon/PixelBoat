import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class Screen implements MouseListener, MouseMotionListener, KeyListener, ComponentListener {

    Window window;
    Dimension size;

    // input
    Input input, tInput;

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

    static class Input {
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
        tInput = input;

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

    @NotNull
    abstract String getName();

    @NotNull
    abstract Color getBackground();

    abstract void tickScreen();

    abstract void drawScreen();
}

abstract class UIScreen extends Screen {

    View root;

    public UIScreen(Window window) {
        super(window);

        Data data = new Data();
        data.setInt("width", size.width);
        data.setInt("height", size.height);
        root = new View(data) {
            @Override
            void tickView() {
            }

            @Override
            void drawView() {
            }
        };

        window.addMouseListener(this);
        window.addMouseMotionListener(this);
    }

    View getView(@NotNull String name) {
        List<View> search = new ArrayList<>(Collections.singletonList(root));
        while (search.size() != 0) {
            View v = search.get(0);
            if (name.equals(v.name)) return v;
            search.addAll(v.views);
        }
        return null;
    }

    @Override
    void tickScreen() {

        boolean mouseDown = tInput.mbPressed.size() > 0,
                mouseUp = tInput.mbReleased.size() > 0;

        View selected = null;
        if (tInput.mouse != null) {
            List<View> check = new ArrayList<>(Collections.singletonList(root));
            while (check.size() > 0) {
                if (check.get(0).box.contains(tInput.mouse)) {
                    check = new ArrayList<>(check.get(0).views);
                    selected = check.get(0);
                } else check.remove(0);
            }
        }

        if (selected != null) {
            selected.hover = true;
            selected.press = mouseDown;
            selected.release = mouseUp;
            selected.click = selected.hold && mouseUp;
        }

        root.tick();

        tickUIScreen();
    }

    @Override
    void drawScreen() {
        root.draw();
        g.drawImage(root.frame, 0, 0, null);

        drawUIScreen();
    }

    abstract void tickUIScreen();

    abstract void drawUIScreen();
}
