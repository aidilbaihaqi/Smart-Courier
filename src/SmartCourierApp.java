import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class SmartCourierApp extends JFrame {
    private BufferedImage mapImage;
    private Point courierPos, destinationPos;
    private Direction courierDir = Direction.RIGHT;
    private final int courierSize = 20;
    private java.util.List<Point> path = new ArrayList<>();
    private Timer moveTimer;
    private int pathIndex = 0;

    private enum Direction {
        UP, RIGHT, DOWN, LEFT
    }

    public SmartCourierApp() {
        setTitle("Smart Courier Simulator");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JButton loadMapBtn = new JButton("Load Map");
        JButton randomizeBtn = new JButton("Acak Kurir & Tujuan");
        JButton startBtn = new JButton("Mulai");

        JPanel panel = new JPanel();
        panel.add(loadMapBtn);
        panel.add(randomizeBtn);
        panel.add(startBtn);

        add(panel, BorderLayout.SOUTH);

        MapPanel mapPanel = new MapPanel();
        add(mapPanel, BorderLayout.CENTER);

        loadMapBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    mapImage = ImageIO.read(fc.getSelectedFile());
                    mapPanel.repaint();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        randomizeBtn.addActionListener(e -> {
            if (mapImage == null) return;
            courierPos = randomValidPoint();
            destinationPos = randomValidPoint();
            path.clear();
            mapPanel.repaint();
        });

        startBtn.addActionListener(e -> {
            if (courierPos == null || destinationPos == null) return;
            path = findPath(courierPos, destinationPos);
            if (path.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tidak ada jalur yang ditemukan!");
                return;
            }
            pathIndex = 0;
            if (moveTimer != null) moveTimer.stop();
            moveTimer = new Timer(10, evt -> {
                if (pathIndex < path.size()) {
                    Point next = path.get(pathIndex);
                    updateDirection(courierPos, next);
                    courierPos = next;
                    pathIndex++;
                    mapPanel.repaint();
                } else {
                    ((Timer) evt.getSource()).stop();
                    JOptionPane.showMessageDialog(this, "Paket berhasil diantar!");
                }
            });
            moveTimer.start();
        });
    }

    private void updateDirection(Point current, Point next) {
        int dx = next.x - current.x;
        int dy = next.y - current.y;
        if (Math.abs(dx) > Math.abs(dy)) {
            courierDir = dx > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            courierDir = dy > 0 ? Direction.DOWN : Direction.UP;
        }
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
                int nx = current.x + dx[i];
                int ny = current.y + dy[i];
                Point neighbor = new Point(nx, ny);

                if (nx >= 0 && ny >= 0 && nx < mapImage.getWidth() && ny < mapImage.getHeight()) {
                    Color c = new Color(mapImage.getRGB(nx, ny));
                    if (isRoad(c) && !visited.contains(neighbor)) {
                        queue.add(neighbor);
                        visited.add(neighbor);
                        cameFrom.put(neighbor, current);
                    }
                }
            }
        }

        List<Point> path = new ArrayList<>();
        Point current = end;
        while (!current.equals(start)) {
            path.add(current);
            current = cameFrom.get(current);
            if (current == null) return new ArrayList<>(); // no path
        }
        path.add(start);
        Collections.reverse(path);
        return path;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SmartCourierApp app = new SmartCourierApp();
            app.setVisible(true);
        });
    }
}
