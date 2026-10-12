package model;

public class DeductionType {
    private int deductionTypeId;
    private String deductionName;
    private String description;

    public DeductionType() { }

    public DeductionType(int deductionTypeId, String deductionName, String description) {
        this.deductionTypeId = deductionTypeId;
        this.deductionName = deductionName;
        this.description = description;
    }

    public int getDeductionTypeId() { return deductionTypeId; }
    public void setDeductionTypeId(int deductionTypeId) { this.deductionTypeId = deductionTypeId; }

    public String getDeductionName() { return deductionName; }
    public void setDeductionName(String deductionName) { this.deductionName = deductionName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override public String toString() { return deductionName; }
}