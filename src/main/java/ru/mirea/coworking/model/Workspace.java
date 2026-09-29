package ru.mirea.coworking.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Workspace {

    private Integer id;
    private String name;
    private WorkspaceType type;
    private int capacity;
    private BigDecimal pricePerHour;
    private boolean active;

    public Workspace() {
    }

    public Workspace(String name, WorkspaceType type, int capacity, BigDecimal pricePerHour, boolean active) {
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.pricePerHour = pricePerHour;
        this.active = active;
    }

    public Workspace(Integer id, String name, WorkspaceType type, int capacity, BigDecimal pricePerHour, boolean active) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.pricePerHour = pricePerHour;
        this.active = active;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WorkspaceType getType() {
        return type;
    }

    public void setType(WorkspaceType type) {
        this.type = type;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public BigDecimal getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(BigDecimal pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Workspace)) return false;
        Workspace workspace = (Workspace) o;
        return Objects.equals(id, workspace.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Workspace{id=" + id + ", name='" + name + "', type=" + type
                + ", capacity=" + capacity + ", pricePerHour=" + pricePerHour + ", active=" + active + "}";
    }
}
