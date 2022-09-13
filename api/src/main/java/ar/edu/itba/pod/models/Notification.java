package ar.edu.itba.pod.models;


import java.io.Serializable;

public class Notification implements Serializable {
    private String oldCode;
    private String newCode;
    private final String destination;
    private RowCategory oldCategory;
    private RowCategory newCategory;
    private Integer currentRow;
    private Integer newRow;
    private Character currentCol;
    private Character newCol;

    public Notification(String oldCode, String destination) {
        this.oldCode = oldCode;
        this.destination = destination;
    }

    public Notification(String oldCode, String destination, String newCode) {
        this.oldCode = oldCode;
        this.destination = destination;
        this.newCode = newCode;
    }

    public Notification(String oldCode, String destination, RowCategory oldCategory,
                        Integer currentRow, Character currentCol) {
        this(oldCode, destination);
        this.oldCategory = oldCategory;
        this.currentRow = currentRow;
        this.currentCol = currentCol;
    }

    public Notification(String oldCode, String destination, RowCategory oldCategory,
                        Integer currentRow, Character currentCol, RowCategory newCategory,
                        Integer newRow, Character newCol) {
        this(oldCode, destination, oldCategory, currentRow, currentCol);
        this.newCategory = newCategory;
        this.newRow = newRow;
        this.newCol = newCol;
    }


    public String getOldCode() {
        return oldCode;
    }

    public void setOldCode(String oldCode) {
        this.oldCode = oldCode;
    }


    public String getNewCode() {
        return newCode;
    }

    public String getDestination() {
        return destination;
    }

    public RowCategory getOldCategory() {
        return oldCategory;
    }

    public RowCategory getNewCategory() {
        return newCategory;
    }

    public Integer getCurrentRow() {
        return currentRow;
    }

    public Integer getNewRow() {
        return newRow;
    }

    public Character getCurrentCol() {
        return currentCol;
    }

    public Character getNewCol() {
        return newCol;
    }
}
