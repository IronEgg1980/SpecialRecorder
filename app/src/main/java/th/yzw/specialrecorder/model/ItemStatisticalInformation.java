package th.yzw.specialrecorder.model;

import android.support.annotation.NonNull;

public class ItemStatisticalInformation extends BaseModel {

    private String phoneId;
    private String name;
    private int formalation;
    private int itemType;
    private int selectedTimes;
    private int totalQuantity;

    public ItemStatisticalInformation(String name, int formalation, int itemType) {
        this.name = name;
        this.formalation = formalation;
        this.itemType = itemType;
    }

    public ItemStatisticalInformation() {

    }

    public String getPhoneId() {
        return phoneId;
    }

    public void setPhoneId(String phoneId) {
        this.phoneId = phoneId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFormalation() {
        return formalation;
    }

    public void setFormalation(int formalation) {
        this.formalation = formalation;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public int getSelectedTimes() {
        return selectedTimes;
    }

    public void setSelectedTimes(int selectedTimes) {
        this.selectedTimes = selectedTimes;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    @NonNull
    @Override
    public String toString() {
        return  formalation+"";
    }
}
