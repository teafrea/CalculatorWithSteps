# CalculatorWithSteps

A step-by-step expression calculator built on an expression tree core.
Supports both **numeric evaluation** (`1 + 2 * 3 = 7`) and **symbolic
algebraic transformation** (`x² + 6x + 5 → (x+3)² - 4`), each with
printed steps at every stage.

---

## Architecture Overview

```
User Input  (string)
     │
     ▼
  Parser
  (Shunting Yard → Postfix → Tree)
     │
     ▼
Expression Tree  ◄─── one shared structure for everything
     │
     ▼
  Classifier
  "Does this tree contain variables?"
     │
     ├── NO  ──►  Evaluator Engine   →  a number  +  printed steps
     │
     └── YES ──►  Transformer Engine →  a new tree +  printed steps
```

---

## 1. Core Data Structure — `Node`

Every expression, numeric or symbolic, is a tree of `Node` objects.

### Node Types

| `NodeType`  | `value` holds       | `left` | `right` | Example          |
|-------------|---------------------|--------|---------|------------------|
| `NUMBER`    | `int` or `float`    | —      | —       | `3`, `2.5`       |
| `VARIABLE`  | `str` symbol        | —      | —       | `x`, `y`         |
| `BINARY_OP` | operator `str`      | ✅     | ✅      | `+`, `-`, `*`, `/`, `^` |
| `UNARY_OP`  | operator `str`      | ✅     | —       | `sqrt`, `negate` |

### Class Definition

```python
from enum import Enum
from dataclasses import dataclass
from typing import Optional

class NodeType(Enum):
    NUMBER    = "number"
    VARIABLE  = "variable"
    BINARY_OP = "binary_op"
    UNARY_OP  = "unary_op"

@dataclass
class Node:
    value:     object        # int | float | str
    node_type: NodeType
    left:  Optional["Node"] = None   # primary child
    right: Optional["Node"] = None   # secondary child (binary only)

    def is_leaf(self) -> bool:
        return self.left is None and self.right is None

    def is_numeric(self) -> bool:
        """True when no VARIABLE node exists anywhere in this subtree."""
        if self.node_type == NodeType.VARIABLE:
            return False
        left_ok  = self.left.is_numeric()  if self.left  else True
        right_ok = self.right.is_numeric() if self.right else True
        return left_ok and right_ok
```

### Tree Examples

```
(1 + 2) * 3          x² + 6x + 5

      *                    +
     / \                  / \
    +   3                +   5
   / \                  / \
  1   2                ^   *
                      / \ / \
                     x  2 6  x
```

### Expandability Notes

- **New binary operators** (`^`, `log`, etc.): add a case to the
  evaluator/transformer — the Node class itself does not change.
- **New unary operators** (`sqrt`, `abs`, etc.): use `UNARY_OP` with
  only `left` populated — already supported.
- **Multi-variable expressions** (`x + y`): `VARIABLE` nodes already
  hold any symbol string — no change needed.

---

## 2. The Classifier

A single method (`Node.is_numeric()`) walks the tree once and returns:

- `True`  → route to **Evaluator Engine**
- `False` → route to **Transformer Engine**

```python
def route(tree: Node):
    if tree.is_numeric():
        return EvaluatorEngine
    else:
        return TransformerEngine
```

---

## 3. Evaluator Engine  *(numeric)*

Handles expressions that contain only numbers — no variables.

### How it works

Recursively walks the tree **bottom-up**: leaf nodes return their value,
operator nodes receive the results from their children, print the step,
then return their own result.

```
Input:  (1 + 2) * 3

Step 1: 1 + 2 = 3
Step 2: 3 * 3 = 9
Result: 9
```

### Division Modes

| Mode        | Behaviour            | Example       |
|-------------|----------------------|---------------|
| `standard`  | float division       | `5 / 2 = 2.5` |
| `remainder` | integer + remainder  | `5 / 2 = 2 R1`|

The mode is passed as a flag into `evaluate()` — it does not affect
the tree structure.

---

## 4. Transformer Engine  *(symbolic)*

Handles expressions that contain at least one variable.
Instead of resolving to a number, it **rewrites the tree** and prints
each rewrite as a step.

### Current target: Completing the Square

```
Input:  x² + 6x + 5

Step 1: Identify coefficients     →  a=1, b=6, c=5
Step 2: Compute (b/2)²            →  (6/2)² = 9
Step 3: Add and subtract (b/2)²   →  x² + 6x + 9 - 9 + 5
Step 4: Factor perfect square     →  (x + 3)² - 9 + 5
Step 5: Simplify constants        →  (x + 3)² - 4
Result: (x + 3)² - 4
```

Each step is a **tree rewrite**, not a numeric evaluation.

### Supported transformations (planned)

| Transformation         | Input form      | Output form        |
|------------------------|-----------------|--------------------|
| Complete the square    | `ax² + bx + c`  | `a(x+h)² + k`      |
| Expand                 | `(x+a)(x+b)`    | `x² + (a+b)x + ab` |
| Factor (future)        | `ax² + bx + c`  | `(x+r)(x+s)`       |

---

## 5. Parsing Strategy

Converts a string like `(1+2)*3` or `x^2 + 6*x + 5` into a `Node` tree.

### Step A — Tokenizer

Splits the raw string into typed tokens:

| Token type | Examples              |
|------------|-----------------------|
| `NUMBER`   | `1`, `3.14`           |
| `VARIABLE` | `x`, `y`             |
| `OPERATOR` | `+`, `-`, `*`, `/`, `^` |
| `FUNCTION` | `sqrt`, `abs`         |
| `LPAREN`   | `(`                   |
| `RPAREN`   | `)`                   |

### Step B — Shunting Yard Algorithm

Converts infix token stream → postfix (Reverse Polish Notation),
respecting operator precedence and parentheses.

Named functions like `sqrt(...)` are treated as a **function token**,
distinct from grouping parentheses.

| Operator | Precedence | Associativity |
|----------|------------|---------------|
| `+` `-`  | 1          | Left          |
| `*` `/`  | 2          | Left          |
| `^`      | 3          | Right         |
| functions| 4          | —             |

### Step C — Postfix to Tree

Process postfix tokens with a stack:

```
NUMBER or VARIABLE  →  create a leaf Node, push onto stack
OPERATOR (binary)   →  pop two Nodes, make them children of a new
                        BINARY_OP Node, push result
FUNCTION (unary)    →  pop one Node, make it left child of a new
                        UNARY_OP Node, push result
```

The final item on the stack is the root of the expression tree.

---

## 6. Implementation Phases

| Phase | Goal | Engine |
|-------|------|--------|
| **1** | `Node` class + manual `evaluate()` that prints steps | Evaluator |
| **2** | Division mode flag (`standard` / `remainder`) | Evaluator |
| **3** | Full parser: tokenizer → Shunting Yard → tree builder | Both |
| **4** | Unary operators: `sqrt`, `negate`, `abs` | Evaluator |
| **5** | Classifier (`is_numeric()`) + engine router | Both |
| **6** | Transformer engine: complete the square | Transformer |
| **7** | Transformer: expand, factor (future) | Transformer |

Each phase is additive. Phases 1–4 do not touch anything that Phase 6–7 will need to modify.