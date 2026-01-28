package it.cnr.contab.web.rest.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TreeNode {
    private String name;
    private String key;
    private String description;
    private BigDecimal value;
    private List<TreeNode> children;

    public TreeNode(String name, String key, String description) {
        this.name = name;
        this.key = key;
        this.description = description;
        this.children = new ArrayList<>();
        this.value = BigDecimal.ZERO;
    }

    public TreeNode(String name, String key, String description, BigDecimal value) {
        this.name = name;
        this.key = key;
        this.description = description;
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

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description;}

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public List<TreeNode> getChildren() { return children; }
    public void setChildren(List<TreeNode> children) { this.children = children; }
}