public class SmartCourierApp extends JFrame {

    // Delivery related variables
    private boolean hasPackage = false;
    private Point currentTarget;

    // Start delivery: menentukan apakah pengambilan atau pengantaran
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

    // Fungsi untuk memulai pergerakan kurir mengikuti jalur yang ditemukan
    private void startMovement() {
        pathIndex = 0;
        moveTimer = new Timer(50, e -> {
            if (pathIndex < path.size()) {
                updateCourierPosition();  // Update posisi kurir
                repaint();
            } else {
                handleArrival();  // Tangani kedatangan di tujuan
            }
        });
        moveTimer.start();
    }

    // Update posisi kurir setiap kali bergerak ke titik berikutnya
    private void updateCourierPosition() {
        Point next = path.get(pathIndex);
        courierPos = next;
        pathIndex++;
    }

    // Ketika kurir tiba di tujuan
    private void handleArrival() {
        moveTimer.stop();
        if (currentTarget == sourcePos) {
            hasPackage = true;  // Paket diambil, lanjutkan ke tujuan
            startDelivery();  // Mulai pengiriman ke tujuan
        } else {
            JOptionPane.showMessageDialog(this, "Paket berhasil diantar ke tujuan!");
        }
    }
}
