import java.awt.*;
import java.io.File;

public class ImageRotationTestScreen extends EngineScreen {

    Image image;
    float rotation;

    ImageRotationTestScreen(GameWindow window) {
        super(window);

        Dimension size = new Dimension(1000, 1000);
        window.setMinimumSize(size);
        window.setMaximumSize(size);
        window.setPreferredSize(size);
        window.setSize(size);
        window.setLocationRelativeTo(null);
        window.setResizable(false);

        pixelate(500, 500);
        image = scaleImage(readImage(new File("C:/Users/Admin/IdeaProjects/PixelBoat/resources/image.png")), 100, 100);
    }

    @Override
    public void tick() {
        rotation += 0.001 * Math.PI;
        if (rotation > Math.PI) rotation -= Math.PI;
    }

    @Override
    public void render() {
        setColor(Color.LIGHT_GRAY);
        fill();
        drawImage(
                resizeImage(
                        rotateImage(image, -2 * rotation)
                        , 500, 500)
                , 0, 0);
    }
}
