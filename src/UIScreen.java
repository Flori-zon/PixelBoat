import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class UIScreen extends Screen {

    View root;

    public UIScreen(Window window, File fmlFile) {
        super(window);

        // root view
        Data data = new Data();
        data.setStr("name", "root");
        data.setInt("width", size.width);
        data.setInt("height", size.height);
        root = new View(data, FMLBuilder.build(fmlFile)); //TODO

    }

    View getView(@NotNull String name) {
        List<View> search = new ArrayList<>(Collections.singletonList(root));
        while (search.size() != 0) {
            View v = search.get(0);
            if (name.equals(v.getName())) return v;
            search.addAll(v.getViews());
        }
        return null;
    }

    @Override
    void tickScreen() {
        root.tick(tickInput.mouse, tickInput.mbPressed);

        tickUIScreen();
    }

    @Override
    void drawScreen() {
        g.drawImage(root.draw(), 0, 0, null);

        drawUIScreen();
    }

    abstract void tickUIScreen();

    abstract void drawUIScreen();
}