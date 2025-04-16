import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class SmartCourierApp extends JFrame {

    // GUI-related variables moved here to avoid conflict
    private JPanel controlPanel;
    private JButton loadBtn;
    private JButton randomBtn;
    private JButton startBtn;
    
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
}
