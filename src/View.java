import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

abstract class View {

    private String name;
    //private View host;
    private final List<View> views;

    private boolean exists, visible;
    private Rectangle layout, abs;
    private final BufferedImage frame;
    private final Graphics2D graphics;

    private final List<Integer> holding;
    private ViewListener listener;

    View(Data attrs, List<View> children) {

        this.views = new ArrayList<>(children);

        this.name = attrs.getStr("name", null);
        this.exists = attrs.getBool("exists", true);
        this.visible = attrs.getBool("visible", true);
        this.layout = new Rectangle(
                attrs.getInt("x", 0), attrs.getInt("y", 0),
                attrs.getInt("width", 0), attrs.getInt("height", 0));

        this.frame = new BufferedImage(abs.width, abs.height, BufferedImage.TYPE_INT_ARGB);
        this.graphics = (Graphics2D) frame.getGraphics();

        this.holding = new ArrayList<>();
    }

    private void layout(Rectangle layout) {
        abs = new Rectangle(abs.x + layout.x, abs.y + layout.y, layout.width, layout.height);
    }

    void tick(Point mouse, List<Integer> mb) {

        for (View view : views)
            view.tick(new Point(1, 1), mb);

        if (abs.contains(input.mouse)) ;

        if (listener != null) {
            if (hover) listener.onHover();
            if (press) listener.onPress();
            if (hold) listener.onHold();
            if (release) listener.onRelease();
            if (click) listener.onClick();
        }

    }

    BufferedImage draw() {

        graphics.fillRect(0, 0, abs.width, abs.height);

        for (View view : views)
            graphics.drawImage(view.draw(), view.abs.x, view.abs.y, null);

        if (visible)
            drawView();

        return frame;
    }

    abstract void drawView();

    public void setListener(ViewListener viewListener) {
        this.listener = viewListener;
    }

    public String getName() {
        return name;
    }

    public List<View> getViews() {
        return views;
    }

}

interface ViewListener {
    void onHover();

    void onPress(int mb);

    void onHold(int mb);

    void onRelease(int mb);

    void onClick(int mb);
}

/* TODO
 * List
 * Button Text/Image
 * Label
 * Text bar
 */