from enum import Enum
from dataclasses import dataclass, field
from typing import Optional


class NodeType(Enum):
    NUMBER   = "number"    # a numeric literal: 3, 2.5
    VARIABLE = "variable"  # a symbol: x, y
    BINARY_OP = "binary_op"  # two children: +, -, *, /, ^
    UNARY_OP  = "unary_op"   # one child:  sqrt, negate


@dataclass
class Node:
    """
    A single node in an expression tree.

    Numeric example:   Node(value=3, node_type=NodeType.NUMBER)
    Variable example:  Node(value='x', node_type=NodeType.VARIABLE)
    Binary op:         Node(value='+', node_type=NodeType.BINARY_OP, left=..., right=...)
    Unary op:          Node(value='sqrt', node_type=NodeType.UNARY_OP, left=...)
    """
    value:     object              # int | float | str
    node_type: NodeType
    left:  Optional["Node"] = field(default=None)   # primary child
    right: Optional["Node"] = field(default=None)   # secondary child (binary only)

    # ------------------------------------------------------------------
    # Convenience helpers
    # ------------------------------------------------------------------

    def is_leaf(self) -> bool:
        """True for NUMBER and VARIABLE nodes — no children."""
        return self.left is None and self.right is None

    def is_numeric(self) -> bool:
        """
        True when the entire subtree rooted here contains no VARIABLE nodes.
        Used by the Classifier to decide which engine to invoke.
        """
        if self.node_type == NodeType.VARIABLE:
            return False
        left_ok  = self.left.is_numeric()  if self.left  else True
        right_ok = self.right.is_numeric() if self.right else True
        return left_ok and right_ok

    def __repr__(self) -> str:
        if self.is_leaf():
            return f"Node({self.value!r})"
        if self.node_type == NodeType.UNARY_OP:
            return f"Node({self.value!r}, left={self.left!r})"
        return f"Node({self.value!r}, left={self.left!r}, right={self.right!r})"


# ------------------------------------------------------------------
# Engine router  (stub — will grow in later phases)
# ------------------------------------------------------------------

def route(tree: Node):
    """
    Classify the tree and dispatch to the correct engine.
    Returns the appropriate engine object (or a string label for now).
    """
    if tree.is_numeric():
        return "EvaluatorEngine"    # Phase 1-4
    else:
        return "TransformerEngine"  # Phase 5+


# ------------------------------------------------------------------
# Quick manual smoke-test
# ------------------------------------------------------------------

if __name__ == "__main__":
    # Numeric tree:  (1 + 2) * 3
    n1  = Node(1,   NodeType.NUMBER)
    n2  = Node(2,   NodeType.NUMBER)
    add = Node('+', NodeType.BINARY_OP, left=n1, right=n2)
    n3  = Node(3,   NodeType.NUMBER)
    mul = Node('*', NodeType.BINARY_OP, left=add, right=n3)

    print(route(mul))   # → EvaluatorEngine

    # Symbolic tree:  x + 2
    x   = Node('x', NodeType.VARIABLE)
    n4  = Node(2,   NodeType.NUMBER)
    sym = Node('+', NodeType.BINARY_OP, left=x, right=n4)

    print(route(sym))   # → TransformerEngine