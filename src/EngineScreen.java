import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings("unused")
public abstract class EngineScreen extends Canvas {

    private int
            screenPixelWidth,
            screenPixelHeight,
            screenWidth,
            screenHeight;
    protected float
            pixelWidth,
            pixelHeight;
    private int
            pixelWidthInt,
            pixelHeightInt;

    protected final GameWindow window;

    private Graphics graphics;
    private Image[] imgStorage;
    private Image image;

    public EngineScreen(GameWindow window) {
        this.window = window;
        this.imgStorage = new Image[0];
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
        resize();
    }

    public void resize() {
        Dimension windowDimension = window.getContentPane().getSize();
        this.screenWidth = windowDimension.width;
        this.screenHeight = windowDimension.height;
        this.pixelWidth = (float) windowDimension.width / (float) screenPixelWidth;
        this.pixelHeight = (float) windowDimension.height / (float) screenPixelHeight;
        this.pixelWidthInt = (int) Math.ceil(pixelWidth);
        this.pixelHeightInt = (int) Math.ceil(pixelHeight);
    }

    //image drawing

    private static class Image {
        int
                sx, sy,
                width, height;
        byte[][] a, r, g, b;

        Image(int width, int height) {
            this.width = width;
            this.height = height;
            a = new byte[width][height];
            r = new byte[width][height];
            g = new byte[width][height];
            b = new byte[width][height];
        }
    }

    public void storeImage(int id) {
        imgStorage[id] = image;
    }

    public int storeImage() {
        ArrayList<Image> storageList = new ArrayList<>(Arrays.asList(imgStorage));
        storageList.add(image);
        imgStorage = storageList.toArray(new Image[0]);
        return imgStorage.length - 1;
    }

    public void readImage(File file) {
        BufferedImage bi;
        try {
            bi = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        image = new Image(bi.getWidth(), bi.getHeight());
        boolean hasAlpha = bi.getAlphaRaster() != null;
        Color ic;
        for (int iy = 0; iy < image.height; iy++)
            for (int ix = 0; ix < image.width; ix++) {
                ic = new Color(bi.getRGB(ix, iy), hasAlpha);
                image.a[ix][iy] = (byte) (ic.getAlpha() - 128);
                image.r[ix][iy] = (byte) (ic.getRed() - 128);
                image.g[ix][iy] = (byte) (ic.getGreen() - 128);
                image.b[ix][iy] = (byte) (ic.getBlue() - 128);
            }
    }

    public void setImage(int id) {
        image = imgStorage[id];
    }

    public void startPositionImage() {
        image.sx = 0;
        image.sy = 0;
    }

    public void centerPositionImage() {
        image.sx = -image.width / 2;
        image.sy = -image.height / 2;
    }

    public void rotateImage(float rotPi) {
        rotateImage(
                (float) image.width / 2 + image.sx,
                (float) image.height / 2 + image.sy,
                rotPi
        );
    }

    public void rotateImage(float cx, float cy, float rotPi) {
        float
                rcx = cx - image.sx,
                rcy = cy - image.sy;
        int
                maxX = image.width - 1,
                maxY = image.height - 1;
        Point[] corners = new Point[]{
                rotatePoint(0, 0, rcx, rcy, rotPi),
                rotatePoint(0, maxY, rcx, rcy, rotPi),
                rotatePoint(maxX, 0, rcx, rcy, rotPi),
                rotatePoint(maxX, maxY, rcx, rcy, rotPi)
        };
        int
                lx = Integer.MAX_VALUE, ly = Integer.MAX_VALUE,
                hx = Integer.MIN_VALUE, hy = Integer.MIN_VALUE;
        for (Point corner : corners) {
            if (corner.x < lx) lx = corner.x;
            if (corner.x > hx) hx = corner.x;
            if (corner.y < ly) ly = corner.y;
            if (corner.y > hy) hy = corner.y;
        }
        Image rotated = new Image(hx - lx, hy - ly);
        rotated.sx += lx;
        rotated.sy += ly;
        rotPi *= -1;
        Point op;
        for (int iy = 0; iy < rotated.height; iy++)
            for (int ix = 0; ix < rotated.width; ix++) {
                op = rotatePoint(ix + lx, iy + ly, rcx, rcy, rotPi);
                if (op.x < 0 || op.y < 0 || op.x > maxX || op.y > maxY) {
                    rotated.a[ix][iy] = Byte.MIN_VALUE;
                    continue;
                }
                rotated.a[ix][iy] = image.a[op.x][op.y];
                rotated.r[ix][iy] = image.r[op.x][op.y];
                rotated.g[ix][iy] = image.g[op.x][op.y];
                rotated.b[ix][iy] = image.b[op.x][op.y];
            }
        image = rotated;
    }

    private Point rotatePoint(int ox, int oy, float cx, float cy, float rotPi) {
        if (ox == cx && oy == cy)
            return new Point(ox, oy);
        float vecX = ox - cx, vecY = oy - cy;
        float dist = (float) Math.sqrt(Math.abs(Math.pow(vecX, 2) + Math.pow(vecY, 2)));
        float angle = (float) Math.atan((double) vecX / vecY);
        if (vecY < 0) angle += Math.PI;
        angle += rotPi;
        return new Point(
                (int) Math.round((Math.sin(angle) * dist) + cx),
                (int) Math.round((Math.cos(angle) * dist) + cy));
    }

    public void resizeImage(int nWidth, int nHeight) {
        scaleImage((float) nWidth / image.width, (float) nHeight / image.height);
    }

    public void scaleImage(float sx, float sy) {
        Image scaled = new Image(
                (int) (image.width * sx),
                (int) (image.height * sy));
        scaled.sx = image.sx;
        scaled.sy = image.sy;
        int tx, ty;
        for (int iy = 0; iy < scaled.height; iy++)
            for (int ix = 0; ix < scaled.width; ix++) {
                tx = (int) (ix / sx);
                ty = (int) (iy / sy);
                scaled.a[ix][iy] = image.a[tx][ty];
                scaled.r[ix][iy] = image.r[tx][ty];
                scaled.g[ix][iy] = image.g[tx][ty];
                scaled.b[ix][iy] = image.b[tx][ty];
            }
        image = scaled;
    }

    public void drawImage(int x, int y) {
        int
                sx = x + image.sx,
                sy = y + image.sy;
        for (int iy = 0; iy < image.height; iy++)
            for (int ix = 0; ix < image.width; ix++) {
                setColor(new Color(
                        image.r[ix][iy] + 128,
                        image.g[ix][iy] + 128,
                        image.b[ix][iy] + 128,
                        image.a[ix][iy] + 128));
                drawPixel(sx + ix, sy + iy);
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

    public void drawRect(int x1, int y1, int x2, int y2) {
        //horizontal lines
        for (int ix = x1; ix <= x2; ix++) {
            drawPixel(ix, y1);
            drawPixel(ix, y2);
        }
        //vertical lines
        for (int iy = y1; iy <= y2; iy++) {
            drawPixel(x1, iy);
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

