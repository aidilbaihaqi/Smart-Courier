import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import java.awt.Color;
import java.awt.Point;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SmartCourierApp extends JFrame {

    // Variables related to map and positions
    private BufferedImage mapImage;
    private Point courierStartPos, sourcePos, destinationPos, courierPos;
    
    private void loadMap() {
        try {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                mapImage = ImageIO.read(fc.getSelectedFile());
                repaint();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void randomizePositions(JPanel mapPanel) {
        if (mapImage == null) return;

        Set<Point> positions = new HashSet<>();
        while (positions.size() < 3) {
            Point p = randomValidPoint();
            if (positions.stream().noneMatch(point -> point.distance(p) < 50)) {
                positions.add(p);
            }
        }

        Iterator<Point> it = positions.iterator();
        courierStartPos = it.next();
        sourcePos = it.next();
        destinationPos = it.next();

        courierPos = new Point(courierStartPos);
        mapPanel.repaint();
    }

    private Point randomValidPoint() {
        Random rand = new Random();
        int w = mapImage.getWidth();
        int h = mapImage.getHeight();
        while (true) {
            int x = rand.nextInt(w);
            int y = rand.nextInt(h);
            Color c = new Color(mapImage.getRGB(x, y));
            if (isRoad(c)) return new Point(x, y);
        }
    }

    private boolean isRoad(Color c) {
        return c.getRed() >= 90 && c.getRed() <= 150 &&
               c.getGreen() >= 90 && c.getGreen() <= 150 &&
               c.getBlue() >= 90 && c.getBlue() <= 150;
    }
}
