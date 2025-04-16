import javax.swing.Timer;
import java.awt.Point;
import java.util.List;

public class SmartCourierApp extends JFrame {

    // Variables for movement and path
    private Timer moveTimer;
    private List<Point> path;
    private int pathIndex = 0;
    private Point courierPos;
    private boolean hasPackage = false;
    
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
        if (!hasPackage) {
            hasPackage = true;
            startDelivery(); // Move to destination after pickup
        } else {
            JOptionPane.showMessageDialog(this, "Paket berhasil diantar ke tujuan!");
        }
    }
}
