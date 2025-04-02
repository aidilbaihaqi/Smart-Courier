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

    private class MapPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (mapImage != null) g.drawImage(mapImage, 0, 0, null);

            if (!path.isEmpty()) {
                g.setColor(Color.GREEN);
                for (Point p : path) {
                    g.fillRect(p.x, p.y, 1, 1);
                }
            }

            if (courierPos != null) {
                drawCourier(g, courierPos.x, courierPos.y);
            }

            if (destinationPos != null) {
                g.setColor(Color.RED);
                g.fillRect(destinationPos.x - 5, destinationPos.y - 5, 10, 10);
            }
        }

        private void drawCourier(Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.BLUE);
            int[] xs, ys;
            switch (courierDir) {
                case UP:
                    xs = new int[]{x, x - courierSize / 2, x + courierSize / 2};
                    ys = new int[]{y - courierSize / 2, y + courierSize / 2, y + courierSize / 2};
                    break;
                case RIGHT:
                    xs = new int[]{x + courierSize / 2, x - courierSize / 2, x - courierSize / 2};
                    ys = new int[]{y, y - courierSize / 2, y + courierSize / 2};
                    break;
                case DOWN:
                    xs = new int[]{x, x - courierSize / 2, x + courierSize / 2};
                    ys = new int[]{y + courierSize / 2, y - courierSize / 2, y - courierSize / 2};
                    break;
                case LEFT:
                    xs = new int[]{x - courierSize / 2, x + courierSize / 2, x + courierSize / 2};
                    ys = new int[]{y, y - courierSize / 2, y + courierSize / 2};
                    break;
                default:
                    return;
            }
            g2.fillPolygon(xs, ys, 3);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SmartCourierApp app = new SmartCourierApp();
            app.setVisible(true);
        });
    }
}
