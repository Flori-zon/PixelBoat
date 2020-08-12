import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class MandelbrotScreen extends UIScreen implements KeyListener {

    final int MAX_I = 500;

    final int SENSIVITY = 100;

    double
            cx1 = -2,
            cx2 = 2,
            cy1 = -2,
            cy2 = 2;

    boolean
            wDown = false,
            aDown = false,
            sDown = false,
            dDown = false,
            qDown = false,
            eDown = false;

    public MandelbrotScreen(GameWindow window) {
        super(window);

        pixelate(500, 500);
        window.addKeyListener(this);

    }

    @Override
    public void tick() {
        super.tick();

        double i = (cx2 - cx1) / SENSIVITY;

        if (aDown) {
            cx1 -= i;
            cx2 -= i;
        }
        if (dDown) {
            cx1 += i;
            cx2 += i;
        }
        if (wDown) {
            cy1 -= i;
            cy2 -= i;
        }
        if (sDown) {
            cy1 += i;
            cy2 += i;
        }

        if (qDown) {
            cx1 += i;
            cx2 -= i;
            cy1 += i;
            cy2 -= i;
        }
        if (eDown) {
            cx1 -= i;
            cx2 += i;
            cy1 -= i;
            cy2 += i;
        }

    }

    @Override
    public void render() {
        super.render();

        for (int ix = 0; ix < screenPixelWidth; ix++)
            for (int iy = 0; iy < screenPixelHeight; iy++) {
                double
                        a = map(ix, 0, screenPixelWidth, cx1, cx2),
                        b = map(iy, 0, screenPixelHeight, cy1, cy2);

                double ca = a, cb = b;

                int i = 0;
                for (; i < MAX_I; i++) {
                    double na = a * a - b * b;
                    double nb = 2 * a * b;
                    a = na + ca;
                    b = nb + cb;
                    if (a + b > 16) break;
                }

                //int col = (int) map(i, 0, MAX_I,0,255);
                if (i == MAX_I)
                    setColor(Color.BLACK);
                else
                    setColor(Color.getHSBColor(1/(float) map(i, 0,MAX_I,0,1),1,1));

                drawPixel(ix, iy);

            }

    }

    double map(double val, double oMin, double oMax, double nMin, double nMax) {
        return (val - oMin) * (nMax - nMin) / (oMax - oMin) + nMin;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyChar()) {
            case 'w':
                wDown = true;
                break;
            case 'a':
                aDown = true;
                break;
            case 's':
                sDown = true;
                break;
            case 'd':
                dDown = true;
                break;
            case 'q':
                qDown = true;
                break;
            case 'e':
                eDown = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyChar()) {
            case 'w':
                wDown = false;
                break;
            case 'a':
                aDown = false;
                break;
            case 's':
                sDown = false;
                break;
            case 'd':
                dDown = false;
                break;
            case 'q':
                qDown = false;
                break;
            case 'e':
                eDown = false;
        }
    }
}
