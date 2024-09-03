import javax.swing.*;

public class MainFrame extends JFrame {
    private InputPanel inputPanel;
    private OutputPanel outputPanel;

    public MainFrame() {
        inputPanel = new InputPanel(this);
        outputPanel = new OutputPanel();

        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(outputPanel, BorderLayout.CENTER);

        setTitle("Math Solver");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void solveEquation(String equation) {
        EquationSolver solver = SolverFactory.getSolver(equation);
        String result = solver.solve(equation);
        outputPanel.setOutput(result);
    }
}
