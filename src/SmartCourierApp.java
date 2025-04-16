import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JFileChooser;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.imageio.ImageIO;
import java.util.Random;

public class SmartCourierApp extends JFrame {

    // GUI-related variables moved here to avoid conflict
    private JPanel controlPanel;
    private JButton loadBtn;
    private JButton randomBtn;
    private JButton startBtn;

    // Variables related to map and positions
    private BufferedImage mapImage;
    private Point courierStartPos, sourcePos, destinationPos, courierPos;
    
    public SmartCourierApp() {
        setTitle("Smart Courier - Auto Delivery");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Control panel setup
        controlPanel = new JPanel();
        loadBtn = new JButton("Load Map");
        randomBtn = new JButton("Acak Posisi");
        startBtn = new JButton("Mulai");

        controlPanel.add(loadBtn);
        controlPanel.add(randomBtn);
        controlPanel.add(startBtn);
        add(controlPanel, BorderLayout.SOUTH);

        MapPanel mapPanel = new MapPanel();
        add(mapPanel, BorderLayout.CENTER);

        loadBtn.addActionListener(e -> loadMap());
        randomBtn.addActionListener(e -> randomizePositions(mapPanel));
        startBtn.addActionListener(e -> startDelivery());
    }

    // Map-related functions

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
