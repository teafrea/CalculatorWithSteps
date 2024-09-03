import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InputPanel extends JPanel {
    private JTextField inputField;
    private JButton solveButton;
    private MainFrame mainFrame;

    public InputPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        inputField = new JTextField(20);
        solveButton = new JButton("Solve");

        solveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String equation = inputField.getText();
                mainFrame.solveEquation(equation);
            }
        });

        add(inputField);
        add(solveButton);
    }
}
