package it.cnr.contab.web.rest.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TreeNode {
    private String name;
    private BigDecimal value;
    private List<TreeNode> children;

    public TreeNode(String name) {
        this.name = name;
        this.children = new ArrayList<>();
        this.value = BigDecimal.ZERO;
    }

    public TreeNode(String name, BigDecimal value) {
        this.name = name;
        this.value = value;
        this.children = new ArrayList<>();
    }

    // Calcola ricorsivamente il valore totale sommando i figli
    public BigDecimal calculateTotalValue() {
        if (children == null || children.isEmpty()) {
            return value != null ? value : BigDecimal.ZERO;
        }

        BigDecimal total = children.stream()
                .map(TreeNode::calculateTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.value = total;
        return total;
    }

    // Getters e setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public List<TreeNode> getChildren() { return children; }
    public void setChildren(List<TreeNode> children) { this.children = children; }
}