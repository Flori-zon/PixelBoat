import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@SuppressWarnings("unused")
public abstract class EngineScreen extends Canvas {

    protected int
            screenWidth,
            screenHeight,
            screenPixelWidth,
            screenPixelHeight;
    protected float
            pixelWidth,
            pixelHeight;
    private int
            pixelWidthInt,
            pixelHeightInt;

    private int antialias;

    protected final GameWindow window;

    private Graphics graphics;

    public EngineScreen(GameWindow window) {
        this.window = window;
        this.antialias = 3;
        pixelate(window.getWidth(), window.getHeight());
    }

    //override

    public abstract void tick();

    public abstract void render();

    //pixel mechanism

    public void newGraphics(Graphics graphics) {
        this.graphics = graphics;
    }

    public void pixelate(int screenPixelWidth, int screenPixelHeight) {
        this.screenPixelWidth = screenPixelWidth;
        this.screenPixelHeight = screenPixelHeight;
        ratio();
    }

    public void resize() {
        Dimension windowDimension = window.getContentPane().getSize();
        if (windowDimension.width != screenWidth || windowDimension.height != screenHeight) {
            this.screenWidth = windowDimension.width;
            this.screenHeight = windowDimension.height;
            ratio();
        }
    }

    private void ratio() {
        this.pixelWidth = (float) screenWidth / (float) screenPixelWidth;
        this.pixelHeight = (float) screenHeight / (float) screenPixelHeight;
        this.pixelWidthInt = (int) Math.ceil(pixelWidth);
        this.pixelHeightInt = (int) Math.ceil(pixelHeight);
    }

    //image drawing

    static class Image {
        boolean hasAlpha;
        int width, height;
        int[][] pixels;

        Image(int width, int height) {
            this.width = width;
            this.height = height;
            this.pixels = new int[width][height];
        }
    }

    static class Vector {

        float x, y;

        Vector(float x, float y) {
            this.x = x;
            this.y = y;
        }

        Vector add(Vector p) {
            return new Vector(x + p.x, y + p.y);
        }

        Vector remove(Vector p) {
            return new Vector(x - p.x, y - p.y);
        }

        Vector divide(float d) {
            return new Vector(x / d, y / d);
        }
    }

    public Image readImage(File file) {
        BufferedImage bi;
        try {
            bi = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        Image image = new Image(bi.getWidth(), bi.getHeight());
        image.hasAlpha = bi.getAlphaRaster() != null;
        for (int iy = 0; iy < image.height; iy++)
            for (int ix = 0; ix < image.width; ix++) {
                image.pixels[ix][iy] = (bi.getRGB(ix, iy));
            }
        return image;
    }

    //TODO
    public Image rotateImage(Image image, float rotPi) {

        int endX = image.width - 1, endY = image.height - 1;
        //rotated corners
        Vector[] corners = new Vector[]{
                rotateVector(0, 0, rotPi),
                rotateVector(0, endY, rotPi),
                rotateVector(endX, 0, rotPi),
                rotateVector(endX, endY, rotPi)
        };
        //rotated frame corners
        float lx = 0, ly = 0, hx = 0, hy = 0;
        for (Vector corner : corners) {
            if (corner.x < lx) lx = corner.x;
            if (corner.x > hx) hx = corner.x;
            if (corner.y < ly) ly = corner.y;
            if (corner.y > hy) hy = corner.y;
        }
        float rWidth = (float) Math.ceil(hx - lx), rHeight = (float) Math.ceil(hy - ly);
        int rWidthInt = (int) rWidth, rHeightInt = (int) rHeight;
        Image rotatedImg = new Image(rWidthInt, rHeightInt);
        //reverse rotated frame corners
        Vector v0 = rotateVector(lx-1, ly-1, -rotPi);
        Vector v1 = rotateVector(hx+1, ly-1, -rotPi).remove(v0);
        Vector v2 = rotateVector(lx-1, hy+1, -rotPi).remove(v0);

        //DEBUG
        Vector p0 = new Vector(99, 99);
        setColor(Color.WHITE);
        drawPixel((int) p0.x, (int) p0.y);
        setColor(Color.RED);
        drawRect((int) p0.x, (int) p0.y, image.width, image.height);
        setColor(Color.BLUE);
        drawLine((int) (p0.x + corners[0].x), (int) (p0.y + corners[0].y), (int) (p0.x + corners[1].x), (int) (p0.y + corners[1].y));
        drawLine((int) (p0.x + corners[1].x), (int) (p0.y + corners[1].y), (int) (p0.x + corners[3].x), (int) (p0.y + corners[3].y));
        drawLine((int) (p0.x + corners[3].x), (int) (p0.y + corners[3].y), (int) (p0.x + corners[2].x), (int) (p0.y + corners[2].y));
        drawLine((int) (p0.x + corners[2].x), (int) (p0.y + corners[2].y), (int) (p0.x + corners[0].x), (int) (p0.y + corners[0].y));
        setColor(Color.GREEN);
        drawRect((int) (p0.x + lx), (int) (p0.y + ly), (int) (hx - lx), (int) (hy - ly));
        setColor(Color.YELLOW);
        drawLine((int) (p0.x + v0.x), (int) (p0.y + v0.y), (int) (p0.x + v0.x + v1.x), (int) (p0.y + v0.y + v1.y));
        drawLine((int) (p0.x + v0.x), (int) (p0.y + v0.y), (int) (p0.x + v0.x + v2.x), (int) (p0.y + v0.y + v2.y));

        //components for mapping from rotated frame to rev rotated-frame
        Vector vx = v1.divide(rWidth), vy = v2.divide(rHeight);
        //reverse mapping in subpixel steps
        float sp = 1 / (float) antialias;
        int aasq = antialias * antialias;
        for (int ix = 0; ix < rWidth; ix++)
            for (int iy = 0; iy < rHeight; iy++) {
                int a = 0, r = 0, g = 0, b = 0;
                for (int ispx = 0; ispx < antialias; ispx++)
                    for (int ispy = 0; ispy < antialias; ispy++) {
                        //position on rotated frame
                        float px = ix + sp * ispx, py = iy + sp * ispy;
                        //position on rev. rotated frame
                        int qx = (int) (v0.x + vx.x * px + vy.x * py), qy = (int) (v0.y + vx.y * px + vy.y * py);
                        if (qx < 0 || qy < 0 || qx > endX || qy > endY) continue;
                        int pc = image.pixels[qx][qy];
                        int ra = (pc & 0xff000000) >>> 24;
                        a += ra;
                        float pa = (float) ra / 255;
                        r += (int) (((pc & 0x00ff0000) >> 16) * pa);
                        g += (int) (((pc & 0x0000ff00) >> 8) * pa);
                        b += (int) ((pc & 0x000000ff) * pa);
                    }
                a /= aasq;
                r /= aasq;
                g /= aasq;
                b /= aasq;
                rotatedImg.pixels[ix][iy] = ((a << 24) & 0xff000000) | ((r << 16) & 0x00ff0000) | ((g << 8) & 0x0000ff00) | (b & 0x000000ff);
            }
        return rotatedImg;
    }

    private Vector rotateVector(float ox, float oy, float rotPi) {
        if (ox == 0 && oy == 0)
            return new Vector(0, 0);
        double dist = Math.sqrt(Math.abs(Math.pow(ox, 2) + Math.pow(oy, 2)));
        double angle = Math.atan(ox / oy);
        if (oy < 0) angle += Math.PI;
        angle += rotPi;
        return new Vector((float) (Math.sin(angle) * dist), (float) (Math.cos(angle) * dist));
    }

    //TODO
    public Image resizeImage(Image image, int nWidth, int nHeight) {
        return scaleImage(image, (float) nWidth / image.width, (float) nHeight / image.height);
    }

    public Image scaleImage(Image image, float sx, float sy) {
        Image scaled = new Image(
                (int) (image.width * sx),
                (int) (image.height * sy));
        int tx, ty;
        for (int iy = 0; iy < scaled.height; iy++)
            for (int ix = 0; ix < scaled.width; ix++) {
                tx = (int) (ix / sx);
                ty = (int) (iy / sy);
                scaled.pixels[ix][iy] = image.pixels[tx][ty];
            }
        return scaled;
    }

    public void drawImage(Image image, int x, int y) {
        for (int ix = 0; ix < image.width; ix++)
            for (int iy = 0; iy < image.height; iy++) {
                setColor(new Color(image.pixels[ix][iy]));
                drawPixel(x + ix, y + iy);
            }
    }

    //geometry drawing

    public void fill() {
        graphics.fillRect(0, 0, screenWidth, screenHeight);
    }

    public void setColor(Color color) {
        graphics.setColor(color);
    }

    public void drawPixel(int x, int y) {
        graphics.fillRect(
                (int) (pixelWidth * x), (int) (pixelHeight * y),
                pixelWidthInt, pixelHeightInt);
    }

    public void drawSizedString(int x, int y, int size, String string) {
        float fontSize = pixelHeight * size / getFontMetrics(new Font(Font.MONOSPACED, Font.PLAIN, 1)).getAscent();
        graphics.setFont(new Font(Font.MONOSPACED, Font.PLAIN, (int) fontSize));
        FontMetrics metrics = getFontMetrics(graphics.getFont());
        char[] chars = string.toCharArray();
        float cx = (int) (x * pixelWidth);
        int cy = (int) (y * pixelHeight + fontSize);
        for (char c : chars) {
            graphics.drawString(Character.toString(c), (int) (cx + (pixelWidth - metrics.charWidth(c)) / 3), (int) (cy - (pixelHeight - metrics.getAscent()) / 3));
            cx += pixelWidth * size;
        }
    }


    public void drawString(int x, int y, String string) {
        float fontSize = pixelHeight / getFontMetrics(new Font(Font.MONOSPACED, Font.PLAIN, 1)).getAscent();
        graphics.setFont(new Font(Font.MONOSPACED, Font.PLAIN, (int) fontSize));
        FontMetrics metrics = getFontMetrics(getFont());
        char[] chars = string.toCharArray();
        float cx = (int) (x * pixelWidth);
        int cy = (int) (y * pixelHeight + fontSize);
        for (char c : chars) {
            graphics.drawString(Character.toString(c), (int) (cx + (pixelWidth - metrics.charWidth(c)) / 3), (int) (cy - (pixelHeight - metrics.getAscent()) / 3));
            cx += pixelWidth;
        }
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        //distances x and y
        int dx = x2 - x1, dy = y2 - y1;
        if (dx == 0 && dy == 0)
            drawPixel(x1, y1);
        else if (Math.abs(dx) > Math.abs(dy))
            //if x distance is higher (each x gets 1 y pixel location)
            if (dx > 0)
                //x1 is lower
                for (int ix = x1; ix <= x2; ix++)
                    drawPixel(ix, Math.round(y1 + dy * (float) (ix - x1) / dx));
            else
                //x2 is lower (or both x equal)
                for (int ix = x2; ix <= x1; ix++)
                    drawPixel(ix, Math.round(y2 + dy * (float) (ix - x2) / dx));
        else
            //if y distance is higher (each y gets 1 x pixel location)
            if (dy > 0)
                //y1 is lower
                for (int iy = y1; iy <= y2; iy++)
                    drawPixel(Math.round(x1 + dx * (float) (iy - y1) / dy), iy);
            else
                //y2 is lower (or both y equal)
                for (int iy = y2; iy <= y1; iy++)
                    drawPixel(Math.round(x2 + dx * (float) (iy - y2) / dy), iy);
    }

    public void drawRect(int x, int y, int width, int height) {
        int x2 = x + width, y2 = y + height;
        //horizontal lines
        for (int ix = x; ix <= x2; ix++) {
            drawPixel(ix, y);
            drawPixel(ix, y2);
        }
        //vertical lines
        for (int iy = y; iy <= y2; iy++) {
            drawPixel(x, iy);
            drawPixel(x2, iy);
        }
    }

    public void fillRect(int x1, int y1, int x2, int y2) {
        //fill
        for (int ix = x1; ix <= x2; ix++)
            for (int iy = y1; iy <= y2; iy++)
                drawPixel(ix, iy);
    }

    public void drawCircle(int x, int y, int radius, Color color) {
        double rSq = Math.pow(radius, 2);
        int iEnd = radius * 2 / 3 + 1;
        for (int i = 0; i <= iEnd; i++) {
            int il = (int) Math.round(Math.sqrt(rSq - Math.pow(i, 2)));
            drawPixel(x - i, y + il); //oben links
            drawPixel(x - i, y - il); //unten links
            drawPixel(x + i, y + il); //oben rechts
            drawPixel(x + i, y - il); //unten rechts
            drawPixel(x - il, y + i); //links oben
            drawPixel(x - il, y - i); //links unten
            drawPixel(x + il, y + i); //rechts oben
            drawPixel(x + il, y - i); //rechts unten
        }
    }

    public void fillCircle(int x, int y, int radius) {
        double rSq = Math.pow(radius, 2);
        for (int ix = 0; ix <= radius; ix++) {
            int il = (int) Math.round(Math.sqrt(rSq - Math.pow(ix, 2)));
            for (int iy = 0; iy <= il; iy++) {
                drawPixel(x - ix, y + iy); //oben links
                drawPixel(x - ix, y - iy); //unten links
                drawPixel(x + ix, y + iy); //oben rechts
                drawPixel(x + ix, y - iy); //unten rechts
            }
        }
    }

    public void drawOval(int x, int y, int width, int height) {

        //like drawing a circle, but streched
        float rx = (float) width / 2, ry = (float) height / 2; //radiusses
        float rxSq = (float) Math.pow(rx, 2), rySq = (float) Math.pow(ry, 2); //radiusses squared
        float cx = x + rx, cy = y + ry; //center
        float yStr = (float) height / width, xStr = (float) width / height; //stretch

        //iteration though x
        for (int ix = 0; ix <= rx; ix++) {
            //x - specific radius
            int il = (int) Math.round(Math.sqrt(rxSq - Math.pow(ix, 2)) * yStr);
            drawPixel((int) cx - ix, (int) cy + il); //oben links
            drawPixel((int) cx - ix, (int) cy - il); //unten links
            drawPixel((int) cx + ix, (int) cy + il); //oben rechts
            drawPixel((int) cx + ix, (int) cy - il); //unten rechts
        }

        //iteration though y
        for (int iy = 0; iy <= ry; iy++) {
            //x - specific radius
            int il = (int) Math.round(Math.sqrt(rySq - Math.pow(iy, 2)) * xStr);
            drawPixel((int) cx - il, (int) cy - iy); //links oben
            drawPixel((int) cx + il, (int) cy - iy); //links unten
            drawPixel((int) cx - il, (int) cy + iy); //rechts oben
            drawPixel((int) cx + il, (int) cy + iy); //rechts unten
        }

    }

    public void fillOval(int x, int y, int width, int height) {
        float rx = (float) width / 2, ry = (float) height / 2; //radiusses
        float rxSq = (float) Math.pow(rx, 2); //radiusses squared
        float cx = x + rx, cy = y + ry; //center
        float yStr = (float) height / width; //stretch

        //fill with iteration through x
        for (int ix = 0; ix <= rx; ix++) {
            //x - specific radius
            int il = (int) Math.round(Math.sqrt(rxSq - Math.pow(ix, 2)) * yStr);
            for (int iy = 0; iy <= il; iy++) {
                drawPixel((int) cx - ix, (int) cy + iy); //unten rechts
                drawPixel((int) cx - ix, (int) cy - iy); //unten links
                drawPixel((int) cx + ix, (int) cy + iy); //oben rechts
                drawPixel((int) cx + ix, (int) cy - iy); //oben links
            }
        }
    }

    void fillTriangle(int x0, int y0, int x1, int y1, int x2, int y2) {
        int a, b, y, last;
        if (y0 > y1) {
            int t;
            t = y0;
            y0 = y1;
            y1 = t;
            t = x0;
            x0 = x1;
            x1 = t;
        }
        if (y1 > y2) {
            int t;
            t = y1;
            y1 = y2;
            y2 = t;
            t = x1;
            x1 = x2;
            x2 = t;
        }
        if (y0 > y1) {
            int t;
            t = y0;
            y0 = y1;
            y1 = t;
            t = x0;
            x0 = x1;
            x1 = t;
        }
        if (y0 == y2) {
            a = b = x0;
            if (x1 < a) a = x1;
            else if (x1 > b) b = x1;
            if (x2 < a) a = x2;
            else if (x2 > b) b = x2;
            horizLine(a, b, y0);
            return;
        }
        int
                dx01 = x1 - x0,
                dy01 = y1 - y0,
                dx02 = x2 - x0,
                dy02 = y2 - y0,
                dx12 = x2 - x1,
                dy12 = y2 - y1;
        int sa = 0, sb = 0;
        if (y1 == y2) last = y1;
        else last = y1 - 1;
        for (y = y0; y <= last; y++) {
            a = x0 + sa / dy01;
            b = x0 + sb / dy02;
            sa += dx01;
            sb += dx02;
            horizLine(a, b, y);
        }
        sa = dx12 * (y - y1);
        sb = dx02 * (y - y0);
        for (; y <= y2; y++) {
            a = x1 + sa / dy12;
            b = x0 + sb / dy02;
            sa += dx12;
            sb += dx02;
            horizLine(a, b, y);
        }
    }

    private void horizLine(int x1, int x2, int y) {
        int lx, hx;
        if (x1 < x2) {
            lx = x1;
            hx = x2;
        } else {
            lx = x2;
            hx = x1;
        }
        for (int ix = lx; ix <= hx; ix++) {
            drawPixel(ix, y);
        }
    }

    /*
    //getters

    public float getPixelWidth() {
        return pixelWidth;
    }

    public float getPixelHeight() {
        return pixelHeight;
    }

    public int getScreenPixelWidth() {
        return screenPixelWidth;
    }

    public int getScreenPixelHeight() {
        return screenPixelHeight;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public int getPixelWidthInt() {
        return pixelWidthInt;
    }

    public int getPixelHeightInt() {
        return pixelHeightInt;
    }

    public GameWindow getWindow() {
        return window;
    }
    */

    /*
    //don´t use this
    public void fillPolygon(Point[] points, Color color) {

        ArrayList<Integer> xVals = new ArrayList<>();
        ArrayList<Integer> yVals = new ArrayList<>();
        for (Point point : points) {
            xVals.add(point.x);
            yVals.add(point.y);
        }

        int yMin = Collections.min(yVals);
        int xMin = Collections.min(xVals);
        int yMax = Collections.max(yVals);
        int xMax = Collections.max(xVals);

        for (int ix = xMin; ix <= xMax; ix++) {
            FPoint v1p1 = new FPoint(avg(xVals), avg(yVals));
            for (int iy = yMin; iy <= yMax; iy++) {
                Point v1p2 = new Point(ix, iy);
                boolean in = true;
                for (int ip = 0; ip < points.length; ip++) {
                    Point v2p1 = points[ip];
                    Point v2p2;
                    if (ip != points.length - 1) v2p2 = points[ip + 1];
                    else v2p2 = points[0];
                    drawLine(v2p1.x, v2p1.y, v2p2.x, v2p2.y, color);
                    float
                            a1 = v1p2.y - v1p1.y,
                            b1 = v1p1.x - v1p2.x,
                            c1 = (v1p2.x * v1p1.y) - (v1p1.x * v1p2.y),
                            d1 = (a1 * v2p1.x) + (b1 * v2p1.y) + c1,
                            d2 = (a1 * v2p2.x) + (b1 * v2p2.y) + c1;
                    if (d1 > 0 && d2 > 0 || d1 < 0 && d2 < 0)
                        continue;
                    float
                            a2 = v2p2.y - v2p1.y,
                            b2 = v2p1.x - v2p2.x,
                            c2 = (v2p2.x * v2p1.y) - (v2p1.x * v2p2.y);
                    d1 = (a2 * v1p1.x) + (b2 * v1p1.y) + c2;
                    d2 = (a2 * v1p2.x) + (b2 * v1p2.y) + c2;
                    if (d1 > 0 && d2 > 0 || d1 < 0 && d2 < 0 || (a1 * b2) - (a2 * b1) == 0)
                        continue;
                    in = !in;
                }
                if (in) drawPixel(ix, iy, color);
            }
        }
    }

    private float avg(ArrayList<Integer> nums) {
        float sum = 0;
        for (Integer mark : nums)
            sum += mark;
        return sum / nums.size();
    }

    private static class FPoint extends Point {
        float x, y;

        public FPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
     */

}

