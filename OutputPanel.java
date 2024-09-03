import javax.swing.*;

public class OutputPanel extends JPanel {
    private JTextArea outputArea;

    public OutputPanel() {
        outputArea = new JTextArea(10, 30);
        outputArea.setEditable(false);
        add(new JScrollPane(outputArea));
    }

    public void setOutput(String output) {
        outputArea.setText(output);
    }
}
