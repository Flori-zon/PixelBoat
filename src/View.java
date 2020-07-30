import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

abstract class View {

    final UIScreen screen;
    final View host;
    int id;

    int x1, y1, x2, y2;
    boolean visible;
    boolean hover, hold;
    ViewListener[] listeners;
    View[] views;

    public View(UIScreen screen, View host, int x1, int y1, int x2, int y2) {
        this.screen = screen;
        this.host = host;
        setPosition(x1, y1, x2, y2);
        this.visible = true;
        this.listeners = new ViewListener[0];
        this.views = new View[0];
    }

    public void setPosition(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
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
        boolean thisHover = !viewsHover && visible && m != null && m.x >= x1 && m.x <= x2 && m.y >= y1 && m.y <= y2;
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
            bgColor,
            frameColor;

    public FrameView(
            UIScreen screen, View host, int x, int y, int x2, int y2,
            Color bgColor, Color frameColor
    ) {
        super(screen, host, x, y, x2, y2);
        setBgColor(bgColor);
        setFrameColor(frameColor);
    }

    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
    }

    public void setFrameColor(Color frameColor) {
        this.frameColor = frameColor;
    }

    @Override
    void drawThis() {
        screen.setColor(Color.GRAY);
        screen.fillRect(x1, y1, x2, y2);
        screen.setColor(Color.DARK_GRAY);
        screen.drawRect(x1, y1, x2, y2);
    }
}

class ImageView extends FrameView {

    File imgFile;
    int imgId;

    public ImageView(
            UIScreen screen, View host, int x1, int y1, int x2, int y2,
            Color bgColor, Color frameColor,
            File imgFile
    ) {
        super(screen, host, x1, y1, x2, y2, bgColor, frameColor);
        this.imgId = -1;
        setImage(imgFile);
    }

    public void setImage(File imgFile) {
        this.imgFile = imgFile;
        screen.readImage(imgFile);
        if (imgId == -1)
            imgId = screen.storeImage();
        else
            screen.storeImage(imgId);
        screen.scaleImage(x2 - x1 - 2, y2 - y1 - 2);
        screen.storeImage(imgId);
    }

    @Override
    public void setPosition(int x1, int y1, int x2, int y2) {
        super.setPosition(x1, y1, x2, y2);
        if (imgFile != null) setImage(imgFile);
    }

    @Override
    void drawThis() {
        super.drawThis();
        screen.setImage(imgId);
        screen.drawImage(x1 + 1, y1 + 1);
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
    public void setPosition(int x1, int y1, int x2, int y2) {
        super.setPosition(x1, y1, x2, y2);
        textSize = x2 - x1 - 4;
    }

    @Override
    void drawThis() {
        super.drawThis();
        screen.drawSizedString(x1 + 2, y1 + 2, textSize, text);
    }

}