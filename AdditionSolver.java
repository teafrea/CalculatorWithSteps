public class AdditionSolver extends EquationSolver {
    @Override
    public String solve(String equation) {
        String[] parts = equation.split("\\+");
        StringBuilder steps = new StringBuilder();
        int result = 0;

        for (String part : parts) {
            part = part.trim();
            if (part.contains("-")) {
                SubtractionSolver subSolver = new SubtractionSolver();
                String subResult = subSolver.solve(part);
                steps.append(subResult);
                String[] subParts = subResult.split("=");
                result += Integer.parseInt(subParts[subParts.length - 1].trim());
            } else {
                int value = Integer.parseInt(part);
                result += value;
                steps.append(result).append("\n");
            }
        }

        steps.append(equation).append(" = ").append(result);
        return steps.toString();
    }
}
