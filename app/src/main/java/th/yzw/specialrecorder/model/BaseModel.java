package th.yzw.specialrecorder.model;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

public abstract class BaseModel extends LitePalSupport {
    private int dataMode;
    @Column(ignore = true)
    private boolean isSelected;

    public int getDataMode() {
        return dataMode;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public void setDataMode(int dataMode) {
        this.dataMode = dataMode;
    }

    public long getId(){
        return super.getBaseObjId();
    }

}
