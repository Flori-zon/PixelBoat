import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;

public class GameWindow extends JFrame implements WindowListener {

    public static void main(String[] args) {
        new GameWindow();
    }

    private boolean running;
    private Updater updater;
    private EngineScreen canvas;


    private class Updater extends Thread {
        @Override
        public void run() {
            super.run();
            long lastTime = System.nanoTime();
            double amountOfTicks = 60.0;
            double ns = 1000000000 / amountOfTicks;
            double delta = 0;
            long timer = System.currentTimeMillis();
            int frames = 0;
            while (running) {
                long now = System.nanoTime();
                delta += (now - lastTime) / ns;
                lastTime = now;
                while (delta >= 1) {
                    tick();
                    delta--;
                }
                if (running)
                    render();
                frames++;
                if (System.currentTimeMillis() - timer > 1000) {
                    timer += 1000;
                    System.out.println("FPS: " + frames);
                    frames = 0;
                }
            }
        }
    }

    public GameWindow() {
        super();

        setTitle("Pixel Boat");

        canvas = new MenuScreen(this);
        add(canvas);

        addWindowListener(this);
        setVisible(true);
        pack();

        running = true;
        updater = new Updater();
        updater.start();
    }

    private void tick() {
        canvas.tick();
    }

    private void render() {
        BufferStrategy bs = canvas.getBufferStrategy();
        if (bs == null) {
            canvas.createBufferStrategy(2);
            bs = canvas.getBufferStrategy();
        }
        Graphics graphics = bs.getDrawGraphics();

        canvas.newGraphics(graphics);
        canvas.resize();
        canvas.render();

        graphics.dispose();
        bs.show();
    }

    public void newScreen(EngineScreen engineScreen) {
        running = false;
        try {
            updater.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        remove(canvas);

        canvas = engineScreen;
        add(engineScreen);

        running = true;
        updater = new Updater();
        updater.start();
    }

    public void close() {
        running = false;
        try {
            updater.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        setVisible(false);
        dispose();
    }

    @Override
    public void windowClosing(WindowEvent ev) {
        close();
    }

    //not used:

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
