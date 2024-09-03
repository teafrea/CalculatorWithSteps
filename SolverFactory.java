public class SolverFactory {
    public static EquationSolver getSolver(String equation) {
        if (equation.contains("+")) {
            return new AdditionSolver();
        } else if (equation.contains("-")) {
            return new SubtractionSolver();
        }
        throw new UnsupportedOperationException("Operation not supported.");
    }
}
