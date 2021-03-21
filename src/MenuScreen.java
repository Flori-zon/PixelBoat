import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class MenuScreen extends UIScreen {
    public MenuScreen(Window window) {
        super(window);
    }

    @Override
    void tickUIScreen() {

    }

    @Override
    void drawUIScreen() {

    }

    @Override
    @NotNull String getName() {
        return "MenuScreen";
    }

    @Override
    @NotNull Color getBackground() {
        return Color.BLACK;
    }
}
