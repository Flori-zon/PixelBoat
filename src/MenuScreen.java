import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MenuScreen extends UIScreen {
    public MenuScreen(GameWindow window) {
        super(window);
        KeyAdapter adapter = new KeyAdapter(){
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyReleased(e);
            }
        };
    }
}
