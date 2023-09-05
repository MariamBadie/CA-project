import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;

public class PrintingSimulationGUI extends JFrame {
    private JTextArea consoleTextArea;
    private JTextField inputField;
    private PrintStream consolePrintStream;
    private BufferedReader consoleReader;
    private String userInput;

    public PrintingSimulationGUI() {
        setTitle("Printing Simulation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Add window listener to handle maximizing the frame
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
        });

        // Set the preferred size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setPreferredSize(screenSize);

        // Set undecorated to false for exit buttons
        setUndecorated(false);

        initComponents();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        consoleTextArea = new JTextArea();
        consoleTextArea.setEditable(false);
        consoleTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14)); // Set font and size
        JScrollPane scrollPane = new JScrollPane(consoleTextArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        inputField = new JTextField();
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 14)); // Set font and size
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userInput = inputField.getText();
                inputField.setText("");
                synchronized (PrintingSimulationGUI.this) {
                    PrintingSimulationGUI.this.notify();
                }
            }
        });

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JLabel("Input:"), BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Redirect System.out to update the GUI
        consolePrintStream = new PrintStream(new CustomOutputStream(consoleTextArea));
        System.setOut(consolePrintStream);
    }


    public String getUserInput() {
        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return userInput;
    }

    // Custom OutputStream implementation that updates the GUI component
    private class CustomOutputStream extends OutputStream {
        private JTextArea textArea;

        public CustomOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(int b) {
            // Append the byte value to the text area
            textArea.append(String.valueOf((char) b));
            // Scroll to the bottom of the text area
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }
    }



    public static void main(String[] args) {
//        PrintingSimulationGUI p = new PrintingSimulationGUI();
//        Scheduler.schedule();
    }

    // ...
}
