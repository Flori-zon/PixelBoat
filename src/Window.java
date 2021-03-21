import javax.swing.*;
import java.awt.*;

public class Window extends JFrame {

    public static void main(String[] args) {
        new Window();
    }

    Screen screen;

    private boolean running;
    private Updater updater;

    public Window() {
        super();

        this.screen = new MenuScreen(this);

        setResizable(false);
        setSize(new Dimension(800, 400));
        setLocationRelativeTo(null);

        start();
        setVisible(true);
    }

    private class Updater extends Thread {
        @Override
        public void run() {
            running = true;
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
                    screen.tick();
                    delta--;
                }
                if (running)
                    screen.draw();
                frames++;
                if (System.currentTimeMillis() - timer > 1000) {
                    timer += 1000;
                    System.out.println("FPS: " + frames);
                    frames = 0;
                }
            }
        }

    }

    private void start() {
        updater = new Updater();
        updater.start();
    }

    private void stop() {
        running = false;
        try {
            updater.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void close() {
        stop();
        setVisible(false);
        dispose();
    }
}
