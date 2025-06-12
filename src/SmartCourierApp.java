import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class SmartCourierApp extends JFrame {
    private BufferedImage mapImage;
    private Point courierStartPos, sourcePos, destinationPos, currentTarget;
    private Point courierPos;
    private Direction courierDir = Direction.RIGHT;
    private final int courierSizeRef = 20; // Segitiga besar agar anti-aliasing kelihatan
    private java.util.List<Point> path = new java.util.ArrayList<>();
    private javax.swing.Timer moveTimer;
    private int pathIndex = 0;
    private boolean hasPackage = false;

    private enum Direction { UP, RIGHT, DOWN, LEFT }

    public SmartCourierApp() {
        setTitle("Smart Courier - Anti-Aliasing Demo");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel controlPanel = new JPanel();
        JButton loadBtn   = new JButton("Load Map");
        JButton randomBtn = new JButton("Acak Posisi");
        JButton startBtn  = new JButton("Mulai");
        controlPanel.add(loadBtn);
        controlPanel.add(randomBtn);
        controlPanel.add(startBtn);
        add(controlPanel, BorderLayout.SOUTH);

        MapPanel mapPanel = new MapPanel();
        add(mapPanel, BorderLayout.CENTER);

        loadBtn.addActionListener(e -> {
            loadMap();
            mapPanel.repaint();
        });
        randomBtn.addActionListener(e -> {
            randomizePositions();
            mapPanel.repaint();
        });
        startBtn.addActionListener(e -> startDelivery());
    }

    private void loadMap() {
        try {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                mapImage = ImageIO.read(fc.getSelectedFile());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void randomizePositions() {
    if (mapImage == null) return;
    java.util.Set<Point> positions = new java.util.HashSet<>();
    while (positions.size() < 3) {
        Point p = randomValidPoint();
        if (positions.stream().noneMatch(existing -> existing.distance(p) < 50)) {
            positions.add(p);
        }
    }
    java.util.Iterator<Point> it = positions.iterator();
    courierStartPos = it.next();
    sourcePos       = it.next();
    destinationPos  = it.next();

    if (!validatePoint(courierStartPos) || !validatePoint(sourcePos) || !validatePoint(destinationPos)) {
        courierStartPos = null;
        sourcePos = null;
        destinationPos = null;
        courierPos = null;
        JOptionPane.showMessageDialog(this, "Titik diluar dari jalur, jalur tidak ditemukan.");
        return;
    }

    courierPos = new Point(courierStartPos);
    hasPackage = false;
    path = findPath(courierStartPos, sourcePos); // <--- langsung buat preview rute
    pathIndex = 0;
}


    private void startDelivery() {
        if (courierPos == null || sourcePos == null || destinationPos == null) return;
        if (moveTimer != null && moveTimer.isRunning()) return;

        currentTarget = hasPackage ? destinationPos : sourcePos;
        faceTowards(courierPos, currentTarget);
        path = findPath(courierPos, currentTarget);
        if (path.isEmpty()) {
    JOptionPane.showMessageDialog(this, "Titik diluar dari jalur, jalur tidak ditemukan.");
    return;
}
path.addAll(findPath(sourcePos, destinationPos)); // Gabung dua rute untuk preview lengkap
    }

    private void startMovement() {
        pathIndex = 0;
        moveTimer = new javax.swing.Timer(30, e -> {
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
        if (currentTarget.equals(sourcePos)) {
            hasPackage = true;
            startDelivery();
        } else {
            JOptionPane.showMessageDialog(this, "Paket berhasil diantar ke tujuan!");
        }
    }

    private void faceTowards(Point from, Point to) {
        int dx = to.x - from.x, dy = to.y - from.y;
        if (Math.abs(dx) > Math.abs(dy)) {
            courierDir = dx > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            courierDir = dy > 0 ? Direction.DOWN : Direction.UP;
        }
    }

    private void updateDirection(Point current, Point next) {
        int dx = next.x - current.x, dy = next.y - current.y;
        if (Math.abs(dx) > Math.abs(dy)) {
            courierDir = dx > 0 ? Direction.RIGHT : Direction.LEFT;
        } else {
            courierDir = dy > 0 ? Direction.DOWN : Direction.UP;
        }
    }

    private Point randomValidPoint() {
        java.util.Random rand = new java.util.Random();
        int w = mapImage.getWidth(), h = mapImage.getHeight();
        while (true) {
            int x = rand.nextInt(w), y = rand.nextInt(h);
            Color c = new Color(mapImage.getRGB(x, y));
            if (isRoad(c)) return new Point(x, y);
        }
    }


    
    private boolean isRoad(Color c) {
    int r = c.getRed(), g = c.getGreen(), b = c.getBlue();
    boolean neutral = Math.abs(r-g)<20 && Math.abs(r-b)<20 && Math.abs(g-b)<20;
    int brightness = (r+g+b)/3;
    return neutral && brightness >= 40 && brightness <= 200;
}

    private boolean validatePoint(Point p) {
    Color c = new Color(mapImage.getRGB(p.x, p.y));
    if (!isRoad(c)) {
        JOptionPane.showMessageDialog(this,
            "Titik (" + p.x + ", " + p.y + ") berada di luar jalur,\njalur tidak ditemukan.");
        return false;
    }
    return true;
}

    private java.util.List<Point> findPath(Point start, Point end) {
        if (!isValidPoint(start) || !isValidPoint(end)) return new java.util.ArrayList<>();
        final int imgW = mapImage.getWidth(), imgH = mapImage.getHeight();
        java.util.Set<Point> closedSet = new java.util.HashSet<>();
        java.util.Map<Point, Integer> gScore = new java.util.HashMap<>();
        java.util.Map<Point, Integer> fScore = new java.util.HashMap<>();
        java.util.Map<Point, Point> cameFrom = new java.util.HashMap<>();
        java.util.function.BiFunction<Point, Point, Integer> heuristic = (p, q) ->
            Math.abs(p.x - q.x) + Math.abs(p.y - q.y);

        class Node implements Comparable<Node> {
            Point p; int f;
            Node(Point p, int f) { this.p = p; this.f = f; }
            @Override public int compareTo(Node o) { return Integer.compare(this.f, o.f); }
        }

        java.util.PriorityQueue<Node> openSet = new java.util.PriorityQueue<>();
        gScore.put(start, 0);
        fScore.put(start, heuristic.apply(start, end));
        openSet.add(new Node(start, fScore.get(start)));
        int[] dx = {0, 1, 0, -1}, dy = {-1, 0, 1, 0};

        while (!openSet.isEmpty()) {
            Node currentNode = openSet.poll();
            Point current = currentNode.p;
            if (current.equals(end)) break;
            if (closedSet.contains(current)) continue;
            closedSet.add(current);
            for (int i = 0; i < 4; i++) {
                Point neighbor = new Point(current.x + dx[i], current.y + dy[i]);
                if (!isValidPoint(neighbor) || closedSet.contains(neighbor)) continue;
                int tentativeG = gScore.getOrDefault(current, Integer.MAX_VALUE) + 1;
                if (tentativeG < gScore.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeG);
                    int fVal = tentativeG + heuristic.apply(neighbor, end);
                    fScore.put(neighbor, fVal);
                    openSet.add(new Node(neighbor, fVal));
                }
            }
        }
        java.util.LinkedList<Point> totalPath = new java.util.LinkedList<>();
        Point curr = end;
        while (cameFrom.containsKey(curr)) {
            totalPath.addFirst(curr);
            curr = cameFrom.get(curr);
        }
        return totalPath;
    }

    private boolean isValidPoint(Point p) {
        return p.x >= 0 && p.y >= 0 &&
               p.x < mapImage.getWidth() && p.y < mapImage.getHeight() &&
               isRoad(new Color(mapImage.getRGB(p.x, p.y)));
    }

    private class MapPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (mapImage == null) return;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int panelW = getWidth(), panelH = getHeight();
            int imgW = mapImage.getWidth(), imgH = mapImage.getHeight();
            double scale = Math.min((double) panelW / imgW, (double) panelH / imgH);
            int drawW = (int) (imgW * scale), drawH = (int) (imgH * scale);
            g2.drawImage(mapImage, 0, 0, drawW, drawH, null);

            g2.setColor(new Color(0, 255, 255, 100));
            int r = Math.max((int) (2 * scale), 2);
            for (Point p : path) {
                int x = (int) (p.x * scale), y = (int) (p.y * scale);
                g2.fillOval(x - r, y - r, 2 * r, 2 * r);
            }

            drawPosition(g2, courierStartPos, Color.GREEN,  "Start", scale);
            drawPosition(g2, sourcePos,       Color.ORANGE, "Pickup", scale);
            drawPosition(g2, destinationPos,  Color.RED,    "Delivery", scale);

            drawCourierAA(g2, scale);
        }

        private void drawPosition(Graphics2D g2, Point pos, Color color, String label, double scale) {
            if (pos == null) return;
            int size = Math.max((int) (12 * scale), 7);
            int x = (int) (pos.x * scale), y = (int) (pos.y * scale);
            g2.setColor(color);
            g2.fillRect(x - size/2, y - size/2, size, size);
            g2.setColor(Color.BLACK);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, (float)(13 * scale)));
            g2.drawString(label, x + size, y);
        }

        private void drawCourierAA(Graphics2D g2, double scale) {
            if (courierPos == null) return;
            float sz = (float) (courierSizeRef * scale);
            float half = sz/2f;
            float cx = (float)(courierPos.x * scale);
            float cy = (float)(courierPos.y * scale);

            Path2D.Float tri = new Path2D.Float();
            tri.moveTo(sz/2, 0);          // puncak depan
            tri.lineTo(-sz/2, -sz/2);     // sudut belakang atas
            tri.lineTo(-sz/2, sz/2);      // sudut belakang bawah
            tri.closePath();

            double angleRad = 0;
            switch (courierDir) {
                case RIGHT: angleRad = 0; break;
                case UP:    angleRad = -Math.PI/2; break;
                case DOWN:  angleRad = Math.PI/2; break;
                case LEFT:  angleRad = Math.PI; break;
            }

            AffineTransform saveAT = g2.getTransform();
            g2.translate(cx, cy);
            g2.rotate(angleRad);

            g2.setColor(hasPackage ? new Color(255,0,0) : new Color(0,0,255));
            g2.fill(tri);

            g2.setStroke(new BasicStroke((float)Math.max(2f*scale, 1f)));
            g2.setColor(Color.BLACK);
            g2.draw(tri);

            g2.setTransform(saveAT);
        }
    }

    private static class Point {
        int x, y;
        Point(int x, int y) { this.x = x; this.y = y; }
        Point(Point p)      { this.x = p.x; this.y = p.y; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof Point)) return false;
            Point p = (Point) o;
            return this.x == p.x && this.y == p.y;
        }
        @Override public int hashCode() { return x*31 + y; }
        double distance(Point p) {
            double dx = this.x - p.x, dy = this.y - p.y;
            return Math.hypot(dx, dy);
        }
    }

    static class Node {
    Point p; int f, g;
    Node(Point p, int f, int g) {
        this.p = p;
        this.f = f;
        this.g = g;
    }
}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SmartCourierApp app = new SmartCourierApp();
            app.setVisible(true);
        });
    }
}
