package it.cnr.contab.web.rest.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TreeNode {
    private String name;
    private String key;
    private String description;
    private BigDecimal value;
    private BigDecimal secondaryValue;
    private List<TreeNode> children;

    public TreeNode(String name, String key, String description) {
        this.name = name;
        this.key = key;
        this.description = description;
        this.children = new ArrayList<>();
        this.value = BigDecimal.ZERO;
    }

    public TreeNode(String name, String key, String description, BigDecimal value) {
        this(name, key, description);
        this.value = value;
    }

    public TreeNode(String name, String key, String description, BigDecimal value, BigDecimal secondaryValue) {
        this(name, key, description, value);
        this.secondaryValue = secondaryValue;
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

    // Calcola ricorsivamente il valore totale sommando i figli
    public BigDecimal calculateTotalSecondaryValue() {
        if (children == null || children.isEmpty()) {
            return secondaryValue != null ? secondaryValue : BigDecimal.ZERO;
        }

        BigDecimal total = children.stream()
                .map(TreeNode::calculateTotalSecondaryValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.secondaryValue = total;
        return total;
    }

    // Getters e setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description;}

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public List<TreeNode> getChildren() { return children; }
    public void setChildren(List<TreeNode> children) { this.children = children; }

    public BigDecimal getSecondaryValue() { return secondaryValue; }
    public void setSecondaryValue(BigDecimal secondaryValue) { this.secondaryValue = secondaryValue; }
}