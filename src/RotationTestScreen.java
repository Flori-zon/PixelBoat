import java.awt.*;
import java.awt.event.MouseEvent;

public class RotationTestScreen extends UIScreen {

    Point center;
    Point mouse;
    Point vector;
    double distance;
    double angle;
    double angleC;
    Point vectorC;
    double angleCC;
    Point vectorCC;

    RotationTestScreen(GameWindow window) {
        super(window);

        Dimension size = new Dimension(1000, 1000);
        window.setMinimumSize(size);
        window.setMaximumSize(size);
        window.setPreferredSize(size);
        window.setSize(size);
        window.setLocationRelativeTo(null);
        window.setResizable(false);
        window.pack();

        pixelate(51, 51);

        center = new Point(25, 25);
        mouse = new Point();
        vector = new Point();
        vectorC = new Point();
        vectorCC = new Point();
    }

    @Override
    public void tick() {
        mouse = mousePixelPos;
        vector.x = mouse.x - center.x;
        vector.y = mouse.y - center.y;
        distance = Math.sqrt(Math.abs(Math.pow(vector.x, 2) + Math.pow(vector.y, 2)));
        angle = Math.atan((double) vector.x / vector.y);
        if (vector.y < 0) angle += Math.PI;
        angleC = angle + (double) -45 / 180 * Math.PI;
        vectorC.x = (int) Math.round((Math.sin(angleC) * distance));
        vectorC.y = (int) Math.round((Math.cos(angleC) * distance));
        angleCC = angle + (double) 45 / 180 * Math.PI;
        vectorCC.x = (int) Math.round((Math.sin(angleCC) * distance));
        vectorCC.y = (int) Math.round((Math.cos(angleCC) * distance));
    }

    @Override
    public void render() {
        setColor(Color.BLACK);
        fill();
        setColor(Color.GRAY);
        drawLine(center.x, center.y, center.x + vectorC.x, center.y + vectorC.y);
        drawLine(center.x, center.y, center.x + vectorCC.x, center.y + vectorCC.y);
        setColor(Color.BLUE);
        drawLine(center.x, center.y, mouse.x, mouse.y);
        setColor(Color.WHITE);
        drawPixel(center.x, center.y);
        setColor(Color.DARK_GRAY);
        drawPixel(center.x + vectorC.x, center.x + vectorC.y);
        drawPixel(center.x + vectorCC.x, center.y +vectorCC.y);
        setColor(Color.RED);
        drawPixel(mouse.x, mouse.y);

        setColor(Color.WHITE);
        drawString(0, 0, "distance:  " + distance);
        drawString(0, 1, "mouse:     " + mouse.x + "/" + mouse.y);
        drawString(0, 3, "angle:     " + (angle / Math.PI * 180 - 90));
        drawString(0, 4, "C-angle:   " + (angleC / Math.PI * 180 - 90));
        drawString(0, 5, "CC-angle:  " + (angleCC / Math.PI * 180 - 90));
        drawString(0, 6, "vector:    " + vector.x + "/" + vector.y);
        drawString(0, 7, "C-vector:  " + vectorC.x + "/" + vectorC.y);
        drawString(0, 8, "CC-vector: " + vectorCC.x + "/" + vectorC.y);
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
