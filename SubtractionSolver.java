public class SubtractionSolver extends EquationSolver {
    @Override
    public String solve(String equation) {
        String[] parts = equation.split("-");
        StringBuilder steps = new StringBuilder();
        int result = Integer.parseInt(parts[0].trim());

        for (int i = 1; i < parts.length; i++) {
            int value = Integer.parseInt(parts[i].trim());
            result -= value;
            steps.append(result).append("\n");
        }

        steps.append(equation).append(" = ").append(result);
        return steps.toString();
    }
}
