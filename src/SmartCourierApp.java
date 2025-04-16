import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
    private Point courierStartPos, sourcePos, destinationPos, currentTarget;
    private Point courierPos;
    private Direction courierDir = Direction.RIGHT;
    private final int courierSize = 20;
    private List<Point> path = new ArrayList<>();
    private Timer moveTimer;
    private int pathIndex = 0;
    private boolean hasPackage = false;

    private enum Direction { UP, RIGHT, DOWN, LEFT }

    public SmartCourierApp() {
        setTitle("Smart Courier - Auto Delivery");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel controlPanel = new JPanel();
        JButton loadBtn = new JButton("Load Map");
        JButton randomBtn = new JButton("Acak Posisi");
        JButton startBtn = new JButton("Mulai");

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

    private void loadMap() {
        try {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                mapImage = ImageIO.read(fc.getSelectedFile());
                repaint(); // Trigger repaint after map is loaded
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
        hasPackage = false;
        path.clear();
        mapPanel.repaint();
    }

    private void startDelivery() {
        if (courierPos == null || sourcePos == null || destinationPos == null) return;
        
        if (moveTimer != null && moveTimer.isRunning()) return;
        
        if (!hasPackage) {
            currentTarget = sourcePos;
            faceTowards(courierPos, currentTarget);
            path = findPath(courierPos, currentTarget);
        } else {
            currentTarget = destinationPos;
            faceTowards(courierPos, currentTarget);
            path = findPath(courierPos, currentTarget);
        }
        
        if (path.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tidak ada jalur yang tersedia!");
            return;
        }

        startMovement();
    }

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
        updateDirection(courierPos, next);
        courierPos = next;
        pathIndex++;
    }

    private void handleArrival() {
        moveTimer.stop();
        if (currentTarget == sourcePos) {
            hasPackage = true;
            startDelivery(); // Auto lanjut ke tujuan
        } else {
            JOptionPane.showMessageDialog(this, "Paket berhasil diantar ke tujuan!");
        }
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
                Point neighbor = new Point(
                    current.x + dx[i], 
                    current.y + dy[i]
                );

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
        return p.x >= 0 && p.y >= 0 && 
               p.x < mapImage.getWidth() && 
               p.y < mapImage.getHeight() && 
               isRoad(new Color(mapImage.getRGB(p.x, p.y)));
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

    // Define a MapPanel class for drawing map and courier
    private class MapPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (mapImage != null) g.drawImage(mapImage, 0, 0, null);

            // Draw positions
            drawPosition(g, courierStartPos, Color.GREEN, "Start");
            drawPosition(g, sourcePos, Color.YELLOW, "Pickup");
            drawPosition(g, destinationPos, Color.RED, "Delivery");

            // Draw path
            g.setColor(new Color(0, 255, 255, 100));
            for (Point p : path) {
                g.fillRect(p.x, p.y, 2, 2);
            }

            // Draw courier
            drawCourier(g);
        }

        private void drawPosition(Graphics g, Point pos, Color color, String label) {
            if (pos == null) return;
            g.setColor(color);
            g.fillRect(pos.x - 5, pos.y - 5, 10, 10);
            g.setColor(Color.BLACK);
            g.drawString(label, pos.x + 8, pos.y + 5);
        }

        private void drawCourier(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(hasPackage ? new Color(255, 0, 0) : new Color(0, 0, 255));
            
            int x = courierPos.x;
            int y = courierPos.y;
            int[] xs = new int[3];
            int[] ys = new int[3];
            
            switch (courierDir) {
                case UP:
                    xs = new int[]{x, x - courierSize/2, x + courierSize/2};
                    ys = new int[]{y - courierSize/2, y + courierSize/2, y + courierSize/2};
                    break;
                case DOWN:
                    xs = new int[]{x, x - courierSize/2, x + courierSize/2};
                    ys = new int[]{y + courierSize/2, y - courierSize/2, y - courierSize/2};
                    break;
                case LEFT:
                    xs = new int[]{x - courierSize/2, x + courierSize/2, x + courierSize/2};
                    ys = new int[]{y, y - courierSize/2, y + courierSize/2};
                    break;
                case RIGHT:
                    xs = new int[]{x + courierSize/2, x - courierSize/2, x - courierSize/2};
                    ys = new int[]{y, y - courierSize/2, y + courierSize/2};
                    break;
            }
            g2.fillPolygon(xs, ys, 3);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SmartCourierApp().setVisible(true));
    }
}
