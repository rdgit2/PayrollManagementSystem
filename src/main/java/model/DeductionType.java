/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author alfre
 */
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

}
