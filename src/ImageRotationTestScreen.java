import java.awt.*;
import java.io.File;

public class ImageRotationTestScreen extends EngineScreen {

    Image image;
    float rotation;

    ImageRotationTestScreen(GameWindow window) {
        super(window);
        pixelate(400, 200);
        image = scaleImage(readImage(new File("C:/Users/flo81/IdeaProjects/PixelBoat/resources/image.png")), 15, 15);
    }

    @Override
    public void tick() {
        rotation += 0.0005 * Math.PI;
        if (rotation > Math.PI) rotation -= Math.PI;
    }

    @Override
    public void render() {
        setColor(Color.WHITE);
        fill();
        drawImage(scaleImage(rotateImage(image, -2 * rotation),2,2), 199 , 0);

    }
}
