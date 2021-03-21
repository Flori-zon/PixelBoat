import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

class View {

    View host;

    // properties
    String name;
    boolean exists, visible;
    Rectangle layout, box;
    final List<View> views;
    Dimension grid;

    // input
    boolean hover, press, hold, release, click;

    // output
    BufferedImage frame;
    Graphics2D graphics;
    ViewListener listener;

    View(@NotNull Data attrs) {
        this.name = attrs.getStr("name", null);

        this.exists = attrs.getBool("exists", true);
        this.visible = attrs.getBool("visible", true);
        this.layout = new Rectangle(
                attrs.getInt("x", 0), attrs.getInt("y", 0),
                attrs.getInt("width", 0), attrs.getInt("height", 0));
        this.grid = new Dimension(
                attrs.getInt("gx", 0), attrs.getInt("gy", 0));

        this.views = new ArrayList<>();
        this.frame = new BufferedImage(box.width, box.height, BufferedImage.TYPE_INT_ARGB);
        this.graphics = (Graphics2D) frame.getGraphics();
    }

    void tick() {
        for (View view : views)
            view.tick();

        if (listener != null) {
            if (hover) listener.onHover();
            if (press) listener.onPress();
            if (hold) listener.onHold();
            if (release) listener.onRelease();
            if (click) listener.onClick();
        }

        click = false;
        release = false;
        if (!hover) hold = false;
        press = false;
        hover = false;
    }

    void draw() {
        graphics.fillRect(0, 0, box.width, box.height);

        for (View view : views) {
            view.draw();
            graphics.drawImage(view.frame, view.box.x, view.box.y, null);
        }

    }

    void add(View view) {
        views.add(view);
        view.host = this;
        view.box();
    }

    void remove(View view) {
        if (views.remove(view))
            view.host = null;
    }

    void setListener(ViewListener newListener) {
        listener = newListener;
    }

    void layout(Rectangle newLayout) {
        layout = newLayout;
        box();
    }

    private void box() {
        if (host == null) return;
        double mx = host.grid.width == 0 ? 1 : (double) host.box.width / host.grid.width,
                my = host.grid.height == 0 ? 1 : (double) host.box.height / host.layout.height;
        box = new Rectangle(
                (int) (layout.x * mx),
                (int) (layout.y * my),
                (int) (layout.x == 0 ? host.box.width : layout.width * mx),
                (int) (layout.y == 0 ? host.box.height : layout.height * my));
    }

}

interface ViewListener {
    void onHover();
    void onPress();
    void onHold();
    void onRelease();
    void onClick();
}

/* TODO
 * List
 * Button Text/Image
 * Label
 * Text bar
 */

/*
class FrameView extends View {

    Color
            bodyColor,
            frameColor;

    FrameView(Data attrs) {
        super(attrs);

        bodyColor = new Color(attrs.getInt("bodyColor", Color.WHITE.getRGB()));
        frameColor = new Color(attrs.getInt("bodyColor", Color.BLACK.getRGB()));
    }

    @Override
    void tick() {
        super.tick();
        screen.setColor(bodyColor);
        screen.fillRect(x, y, width, height);
        screen.setColor(frameColor);
        screen.drawRect(x, y, width, height);
    }
}

class ImageView extends DrawableView {

    File imgFile;
    PixelGraphics.Image image;

    public ImageView(UIScreen screen, int id, View host) {
        super(screen, id, host);
        setImage(imgFile);
    }

    public void setImage(File imgFile) {
        this.imgFile = imgFile;
        image = screen.resizeImage(screen.readImage(imgFile), width - 4, height - 4);
    }

    @Override
    public void setPosition(int x, int y) {
        super.setPosition(x, y);
        if (imgFile != null) setImage(imgFile);
    }

    @Override
    void draw() {
        screen.drawImage(image, x + 2, y + 2);
    }

}

class TextView extends FrameView {

    String text;
    int textSize;

    public TextView(UIScreen screen, int id, View host) {
        super(screen, id, host);
        setText(text);
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void setPosition(int x, int y) {
        super.setPosition(x, y);
        textSize = height - 4;
    }

    @Override
    void draw() {
        super.draw();
        screen.drawSizedString(x + 2, y + 2, textSize, text);
    }

} */







