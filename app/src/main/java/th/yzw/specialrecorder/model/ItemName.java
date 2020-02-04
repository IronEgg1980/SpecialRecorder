package th.yzw.specialrecorder.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ItemName extends BaseModel {
    private String name;
    private boolean isOftenUse;
    private int formalation;
    private int itemType;

    @NonNull
    @Override
    public String toString() {
        return this.name;
    }

    public boolean isOftenUse() {
        return isOftenUse;
    }

    public void setOftenUse(boolean oftenUse) {
        isOftenUse = oftenUse;
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

    public ItemName() {

    }

    public ItemName(String _name, boolean _isOftenUse) {
        this();
        this.name = _name;
        this.isOftenUse = _isOftenUse;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null)
            return false;
        if(!(obj instanceof ItemName))
            return false;
        ItemName item = (ItemName)obj;
        return this.name.equalsIgnoreCase(item.getName());
    }
}
