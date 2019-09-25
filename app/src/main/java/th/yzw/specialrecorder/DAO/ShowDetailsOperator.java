package th.yzw.specialrecorder.DAO;

import android.database.Cursor;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import th.yzw.specialrecorder.model.ShowDetailsItemFatherEntity;
import th.yzw.specialrecorder.tools.MyDateUtils;

public class ShowDetailsOperator {
    public static List<ShowDetailsItemFatherEntity> getDetailsFatherList(long date) {
        List<ShowDetailsItemFatherEntity> list = new ArrayList<>();
        String[] times = MyDateUtils.getMonthStartAndEnd(date);
        Cursor cursor = LitePal.findBySQL("SELECT DISTINCT date FROM recordentity WHERE date >= ? AND date <= ? ORDER BY date", times[0], times[1]);
        if (cursor != null && cursor.moveToFirst()) {
            long preL = cursor.getLong(0);
            list.add(new ShowDetailsItemFatherEntity(preL));
            while (cursor.moveToNext()) {
                long currentL = cursor.getLong(0);
                if (!MyDateUtils.isSameDay(preL, currentL)) {
                    list.add(new ShowDetailsItemFatherEntity(currentL));
                    preL = currentL;
                }
            }
        }
        return list;
    }
}
