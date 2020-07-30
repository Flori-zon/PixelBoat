import java.awt.*;

public class MenuScreen extends UIScreen {

    public MenuScreen(GameWindow window) {
        super(window);

        Dimension size = new Dimension(800, 450);
        window.setMinimumSize(size);
        window.setMaximumSize(size);
        window.setPreferredSize(size);
        window.setSize(size);
        window.setLocationRelativeTo(null);
        window.setResizable(false);
        window.pack();

    }



}
