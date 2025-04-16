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
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.Timer;
import javax.swing.JOptionPane;

public class SmartCourierApp extends JFrame {

    // GUI-related variables
    private JPanel controlPanel;
    private JButton loadBtn;
    private JButton randomBtn;
    private JButton startBtn;

    // Variables related to map and positions
    private BufferedImage mapImage;
    private Point courierStartPos, sourcePos, destinationPos, courierPos;

    // Pathfinding-related variables
    private List<Point> path;

    // Variables for movement and path
    private Timer moveTimer;
    private int pathIndex = 0;
    private boolean hasPackage = false;

    // Delivery-related variables
    private Point currentTarget;

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

    // Pathfinding-related functions
    private List<Point> findPath(Point start, Point end) {
        Queue<Point> queue = new LinkedList<>();
        Map<Point, Point> cameFrom = new HashMap<>();
        Set<Point> visited = new HashSet<>();
        queue.add(start);
        visited.add(start);

        int[] dx = {0, 1, 0, -1};
        int[] dy = {-1, 0, 1, 0};

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            if (current.equals(end)) break;

            for (int i = 0; i < 4; i++) {
                Point neighbor = new Point(current.x + dx[i], current.y + dy[i]);
                if (isValidPoint(neighbor) && !visited.contains(neighbor)) {
                    visited.add(neighbor);
                    cameFrom.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        return reconstructPath(cameFrom, end);
    }

    private boolean isValidPoint(Point p) {
        return p.x >= 0 && p.y >= 0 && p.x < mapImage.getWidth() && p.y < mapImage.getHeight();
    }

    private List<Point> reconstructPath(Map<Point, Point> cameFrom, Point end) {
        LinkedList<Point> path = new LinkedList<>();
        Point current = end;
        while (cameFrom.containsKey(current)) {
            path.addFirst(current);
            current = cameFrom.get(current);
        }
        return path;
    }

    // Courier movement functions
    private void startMovement() {
        pathIndex = 0;
        moveTimer = new Timer(50, e -> {
            if (pathIndex < path.size()) {
                updateCourierPosition();
                repaint();
            } else {
                handleArrival();
            }
        });
        moveTimer.start();
    }

    private void updateCourierPosition() {
        Point next = path.get(pathIndex);
        courierPos = next;
        pathIndex++;
    }

    private void handleArrival() {
        moveTimer.stop();
        if (currentTarget == sourcePos) {
            hasPackage = true;  // Paket diambil, lanjutkan ke tujuan
            startDelivery();  // Mulai pengiriman ke tujuan
        } else {
            JOptionPane.showMessageDialog(this, "Paket berhasil diantar ke tujuan!");
        }
    }

    // Delivery related functions
    private void startDelivery() {
        if (courierPos == null || sourcePos == null || destinationPos == null) return;

        if (moveTimer != null && moveTimer.isRunning()) return;

        // Cek apakah paket sudah diambil atau belum
        if (!hasPackage) {
            currentTarget = sourcePos;  // Target pertama: pickup
            faceTowards(courierPos, currentTarget);
            path = findPath(courierPos, currentTarget);
        } else {
            currentTarget = destinationPos;  // Target kedua: delivery
            faceTowards(courierPos, currentTarget);
            path = findPath(courierPos, currentTarget);
        }

        // Jika path kosong, berarti tidak ada jalur yang tersedia
        if (path.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tidak ada jalur yang tersedia!");
            return;
        }

        // Mulai pergerakan kurir setelah menemukan jalur
        startMovement();
    }

    private void faceTowards(Point from, Point to) {
        int dx = to.x - from.x;
        int dy = to.y - from.y;
        if (Math.abs(dx) > Math.abs(dy)) {
            courierDir = dx > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            courierDir = dy > 0 ? Direction.DOWN : Direction.UP;
        }
    }
}
