/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Do Bich Ngoc
 */
package ocr.sapphire.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import java.io.*;
import javax.imageio.*;
import java.awt.image.*;

import ocr.sapphire.ann.Network;
import ocr.sapphire.image.ImagePreprocessor;

public class MainFrame extends JFrame implements ActionListener {
    private DrawingCanvas drawingCanvas = new DrawingCanvas();

    private JTextField charField = new JTextField(10);
    private JTextField unicodeField = new JTextField(10);

//    private JButton recognizeButton = new JButton("Recognize");

    private final JFileChooser chooser = new JFileChooser();

    private SwingWorker worker;

    public MainFrame() {
        setTitle("Handwriting Recognition");
        setPreferredSize(new Dimension(500, 250));
        //setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        createMenu();
        createWorkArea();
        setVisible(true);

        worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws InterruptedException {
                //Network network = new Network(26, 30, 8);
                Thread.sleep(1000);
                return null;
            }

            @Override
            protected void done() {
                try {
                    JOptionPane.showMessageDialog(MainFrame.this, get());
                } catch (InterruptedException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

        worker.execute();
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
        JMenuItem newItem = new JMenuItem("New");
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        newItem.setActionCommand("new");
        newItem.addActionListener(this);
        file.add(newItem);

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

        file.insertSeparator(3);

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
        helpContentsItem.setActionCommand("about");
        helpContentsItem.addActionListener(this);
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

        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.VERTICAL;
        add(new JSeparator(SwingConstants.VERTICAL), c);

        // Action panel
        JPanel actionPanel  = new JPanel(new GridBagLayout());

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

    public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem) e.getSource();
        String s = source.getActionCommand();
        if (s.compareTo("new") == 0) {

        }
        else if (s.compareTo("open") == 0) {
            File file = open();
            if (file != null) {
                drawingCanvas.drawImage(file);
            }
        }
        else if (s.compareTo("saveAs") == 0) {
            saveAs();
        }
        else if (s.compareTo("exit") == 0) {

        }
        else if (s.compareTo("clearAll") == 0) {
            drawingCanvas.clear();
        }
        else if (s.compareTo("new") == 0) {

        }
        else if (s.compareTo("new") == 0) {

        }
    }

    private File open() {
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            System.out.println("Opening: " + file.getName() + ".\n");
            return file;
        }
        else {
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
        }
        else {

        }
    }

    public static void main(String arg[]) {
        MainFrame mainFrame = new MainFrame();
    }

}
