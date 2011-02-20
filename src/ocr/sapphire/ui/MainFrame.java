/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Do Bich Ngoc
 */
package ocr.sapphire.ui;

import com.esotericsoftware.yamlbeans.YamlReader;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import javax.imageio.*;
import ocr.sapphire.ann.Layer;

import ocr.sapphire.ann.OCRNetwork;
import ocr.sapphire.util.Utils;

public class MainFrame extends JFrame implements ActionListener {

    private DrawingCanvas drawingCanvas = new DrawingCanvas();
    private JTextField charField = new JTextField(10);
    private JTextField unicodeField = new JTextField(10);
    private final JFileChooser chooser = new JFileChooser();
    private static int CHARACTER_COUNT = 186;
    private static char[] characterSet = Utils.VIETNAMESE_CHARACTERS.toCharArray();
    private volatile OCRNetwork network;

    public MainFrame() throws IOException {
        setTitle("Handwriting Recognition");
        setPreferredSize(new Dimension(500, 250));
        //setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        createMenu();
        createWorkArea();
        createNetwork();
        setVisible(true);
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenu edit = new JMenu("Edit");
        JMenu help = new JMenu("Help");

        menuBar.add(file);
        menuBar.add(edit);
        menuBar.add(help);

        // Construct File Menu

        JMenuItem openItem = new JMenuItem("Open...");
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        openItem.setActionCommand("open");
        openItem.addActionListener(this);
        file.add(openItem);

        JMenuItem saveAsItem = new JMenuItem("Save As...");
        saveAsItem.setActionCommand("saveAs");
        saveAsItem.addActionListener(this);
        file.add(saveAsItem);

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        exitItem.setActionCommand("exit");
        exitItem.addActionListener(this);
        file.add(exitItem);

        file.insertSeparator(2);

        // Construct Edit Menu
        JMenuItem clearAllItem = new JMenuItem("Clear All");
        clearAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        clearAllItem.setActionCommand("clearAll");
        clearAllItem.addActionListener(this);
        edit.add(clearAllItem);

        // Construct Help Menu
        JMenuItem helpContentsItem = new JMenuItem("Help Contents");
        helpContentsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        helpContentsItem.setActionCommand("helpContents");
        helpContentsItem.addActionListener(this);
        help.add(helpContentsItem);

        JMenuItem aboutItem = new JMenuItem("About...");
        aboutItem.setActionCommand("about");
        aboutItem.addActionListener(this);
        help.add(aboutItem);

        setJMenuBar(menuBar);
    }

    private void createWorkArea() {
        JPanel contentPane = new JPanel(new GridBagLayout());
        setContentPane(contentPane);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.insets = new Insets(5, 5, 5, 5);


        // Drawing panel

        drawingCanvas.setPreferredSize(new Dimension(120, 160));
        drawingCanvas.setBorder(BorderFactory.createEtchedBorder());
        drawingCanvas.setBackground(Color.white);

        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 2;
        contentPane.add(drawingCanvas, c);

        drawingCanvas.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                recognize();
            }
        });

        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.VERTICAL;
        add(new JSeparator(SwingConstants.VERTICAL), c);

        // Action panel
        JPanel actionPanel = new JPanel(new GridBagLayout());

        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 1;
        actionPanel.add(new JLabel("Character:"), c);

        c.gridx = 0;
        c.gridy = 1;
        actionPanel.add(new JLabel("Unicode:"), c);

        c.gridx = 1;
        c.gridy = 0;
        actionPanel.add(charField, c);
        charField.setEditable(false);

        c.gridx = 1;
        c.gridy = 1;
        actionPanel.add(unicodeField, c);
        unicodeField.setEditable(false);

//        c.gridx = 0;
//        c.gridy = 2;
//        c.gridwidth = 2;
//        c.anchor = GridBagConstraints.FIRST_LINE_END;
//        actionPanel.add(recognizeButton, c);

        c.gridx = 2;
        c.gridy = 0;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        contentPane.add(actionPanel, c);
    }

    private void createNetwork() throws IOException {
        YamlReader reader = null;
        try {
            reader = new YamlReader(new FileReader("network-temp2.yaml"),
                    Utils.DEFAULT_YAML_CONFIG);
            network = (OCRNetwork) reader.read();
            network.getNetwork().initialize();
            for (Layer layer : network.getNetwork().getLayers()) {
                layer.initialize();
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem) e.getSource();
        String s = source.getActionCommand();
        if (s.compareTo("open") == 0) {
            File file = open();
            if (file != null) {
                drawingCanvas.drawImage(file);
                recognize();
            }
        } else if (s.compareTo("saveAs") == 0) {
            saveAs();
        } else if (s.compareTo("exit") == 0) {
            System.exit(0);
        } else if (s.compareTo("clearAll") == 0) {
            drawingCanvas.clear();
            charField.setText("");
            unicodeField.setText("");
        } else if (s.compareTo("about") == 0) {
            JOptionPane.showMessageDialog(null, "Optical Character Recognition\n"
                    + "1. Le Ngoc Minh\n"
                    + "2. Do Bich Ngoc");
        } else if (s.compareTo("help") == 0) {
        }
    }

    private File open() {
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            System.out.println("Opening: " + file.getName() + ".\n");
            return file;
        } else {
            return null;
        }
    }

    private void saveAs() {
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            System.out.println("Saving: " + file.getName() + ".\n");
            try {
                ImageIO.write(drawingCanvas.getImage(), "png", file);
            } catch (Exception ex) {
            }
        } else {
        }
    }

    private void recognize() {
        SwingWorker worker = new SwingWorker<Character, Void>() {

            @Override
            protected Character doInBackground() throws InterruptedException {
                network.setCurrentImage(drawingCanvas.getImage());
                network.prepareInput();
                return network.recognize();
            }

            @Override
            protected void done() {
                try {
                    char code = get();
                    charField.setText(Character.toString(code));
                    unicodeField.setText(Integer.toHexString(code));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        worker.execute();
    }

    private char find(char c) {
        int low = 0;
        int high = CHARACTER_COUNT - 1;
        int mid;

        while (low <= high) {
            mid = (low + high) / 2;
            if (c < characterSet[mid]) {
                high = mid - 1;

            } else if (c > characterSet[mid]) {
                low = mid + 1;

            } else {
                return c;
            }
        }
        if (low > CHARACTER_COUNT - 1) {
            low = CHARACTER_COUNT - 1;
        }
        if (high < 0) {
            high = 0;
        }

        if ((c - characterSet[high]) < (c - characterSet[low])) {
            return characterSet[high];
        } else {
            return characterSet[low];
        }
    }

    public static void main(String arg[]) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                try {
                    MainFrame mainFrame = new MainFrame();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.exit(1);
                }
            }
        });
    }
}
