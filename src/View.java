import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

abstract class View {

    final UIScreen screen;
    final View host;
    int id;

    int x, y, height, width;
    boolean visible;
    boolean hover, hold;
    ViewListener[] listeners;
    View[] views;

    public View(UIScreen screen, View host, int x, int y, int width, int height) {
        this.screen = screen;
        this.host = host;
        reposition(x, y);
        resize(width, height);
        this.visible = true;
        this.listeners = new ViewListener[0];
        this.views = new View[0];
    }

    public void reposition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }

   /*
    View[] allViews() {
        ArrayList<View> allViews = new ArrayList<>(Collections.singletonList(this));
        for (View view : views)
            allViews.addAll(Arrays.asList(view.allViews()));
        return (View[]) allViews.toArray();
    }
    */

    public int addView(int hostId, View addView) {
        if (id == hostId) {
            addView.id = viewNum();
            ArrayList<View> viewsList = (ArrayList<View>) Arrays.asList(views);
            viewsList.add(addView);
            views = (View[]) viewsList.toArray();
        } else if (views.length != 0)
            for (View view : views)
                if (view.addView(hostId, addView) != 0)
                    break;
        return addView.id;
    }

    public boolean removeView(int viewId) {
        for (View view : views)
            if (view.id == id) {
                ArrayList<View> viewsList = (ArrayList<View>) Arrays.asList(view.host.views);
                viewsList.remove(view);
                view.views = (View[]) viewsList.toArray();
                return true;
            } else if (view.removeView(viewId)) {
                return true;
            }
        return false;
    }

    public void addListener(ViewListener listener) {
        ArrayList<ViewListener> listenerList = new ArrayList<>(Collections.singletonList(listener));
        listenerList.add(listener);
        listeners = (ViewListener[]) listenerList.toArray();
    }

    public boolean checkHover(Point m) {
        boolean viewsHover = false;
        for (View view : views)
            viewsHover = view.checkHover(m) || viewsHover;
        boolean thisHover = !viewsHover && visible && m != null && m.x >= x && m.x <= x + width && m.y >= y && m.y <= y + height;
        if (thisHover != hover) {
            hover = thisHover;
            for (ViewListener listener : listeners)
                listener.onHoverChange();
        }
        return thisHover;
    }

    public boolean checkHold(boolean d) {
        boolean viewsHold = false;
        for (View view : views)
            viewsHold = view.checkHold(d) || viewsHold;
        boolean thisHold = !viewsHold && d && hover;
        if (thisHold != hold) {
            hold = thisHold;
            for (ViewListener listener : listeners)
                listener.onHoldChange();
        }
        return thisHold;
    }

    public boolean checkClick() {
        boolean viewsClick = false;
        for (View view : views)
            viewsClick = view.checkClick() || viewsClick;
        boolean thisClick = !viewsClick && hover && hold;
        if (thisClick)
            for (ViewListener listener : listeners)
                listener.onClick();
        return thisClick;
    }

    public void draw() {
        if (visible)
            drawThis();
        for (View view : views)
            view.draw();
    }

    int viewNum() {
        int viewNum = 1;
        for (View view : views)
            viewNum += view.viewNum();
        return viewNum;
    }

    void drawThis() {
    }
}

interface ViewListener {
    void onHoverChange();

    void onHoldChange();

    void onClick();
}

abstract class FrameView extends View {

    Color
            bodyColor,
            frameColor;

    public FrameView(
            UIScreen screen, View host, int x, int y, int x2, int y2,
            Color bgColor, Color frameColor
    ) {
        super(screen, host, x, y, x2, y2);
        setBodyColor(bgColor);
        setFrameColor(frameColor);
    }

    public void setBodyColor(Color bodyColor) {
        this.bodyColor = bodyColor;
    }

    public void setFrameColor(Color frameColor) {
        this.frameColor = frameColor;
    }

    @Override
    void drawThis() {
        screen.setColor(Color.GRAY);
        screen.fillRect(x, y, width, height);
        screen.setColor(Color.DARK_GRAY);
        screen.drawRect(x, y, width, height);
    }
}

class ImageView extends FrameView {

    File imgFile;
    EngineScreen.Image image;

    public ImageView(
            UIScreen screen, View host, int x1, int y1, int x2, int y2,
            Color bgColor, Color frameColor,
            File imgFile
    ) {
        super(screen, host, x1, y1, x2, y2, bgColor, frameColor);
        setImage(imgFile);
    }

    public void setImage(File imgFile) {
        this.imgFile = imgFile;
        image = screen.resizeImage(screen.readImage(imgFile), width - 4, height - 4);
    }

    @Override
    public void resize(int x, int y) {
        super.reposition(x, y);
        if (imgFile != null) setImage(imgFile);
    }

    @Override
    void drawThis() {
        super.drawThis();
        screen.drawImage(image, x + 2, y + 2);
    }

}

class TextView extends FrameView {

    String text;
    int textSize;

    public TextView(
            UIScreen screen, View host, int x, int y, int x2, int y2,
            Color bgColor, Color frameColor,
            String text
    ) {
        super(screen, host, x, y, x2, y2, bgColor, frameColor);
        setText(text);
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void resize(int x, int y) {
        super.reposition(x, y);
        textSize = height - 4;
    }

    @Override
    void drawThis() {
        super.drawThis();
        screen.drawSizedString(x + 2, y + 2, textSize, text);
    }

}