package th.yzw.specialrecorder.view;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.ArrayMap;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import th.yzw.specialrecorder.DAO.AppSetupOperator;
import th.yzw.specialrecorder.DAO.ItemStatisticalInformationOperator;
import th.yzw.specialrecorder.JSON.ItemStatisticJSONHelper;
import th.yzw.specialrecorder.JSON.JSONHelper;
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.interfaces.SelectDialogClicker;
import th.yzw.specialrecorder.model.ItemStatisticalInformation;
import th.yzw.specialrecorder.tools.DataTool;
import th.yzw.specialrecorder.tools.FileTools;
import th.yzw.specialrecorder.tools.OtherTools;
import th.yzw.specialrecorder.view.common.DialogFactory;
import th.yzw.specialrecorder.view.common.ConfirmPopWindow;
import th.yzw.specialrecorder.view.common.ToastFactory;

public class ChartActivity extends AppCompatActivity {
    private final String microMsgPath = Environment.getExternalStorageDirectory() + "/tencent/MicroMsg/download/";

    private PieChart pieChart1, pieChart2;
    private BarChart barChart;
    private TextView phoneIdTV;
    private int selectTimesColor, totalQuantityColor;
    private DialogFactory dialogFactory;
    private String phoneId;
    private ToastFactory toastFactory;
    private List<ItemStatisticalInformation> ordredDataList;
    private RadioButton isSortBySelectTimesBT;

    private ArrayMap<String, Integer> getItemPieMap(List<ItemStatisticalInformation> list) {
        Collections.sort(list, new Comparator<ItemStatisticalInformation>() {
            @Override
            public int compare(ItemStatisticalInformation o1, ItemStatisticalInformation o2) {
                Integer i2 = o2.getItemType();
                Integer i1 = o1.getItemType();
                return i1.compareTo(i2);
            }
        });//排序
        ArrayMap<String ,Integer> result = new ArrayMap<>();
        for(ItemStatisticalInformation information:list){
            String type =DataTool.getItemTypeString(information.getItemType());
            result.put(type,result.getOrDefault(type,0)+information.getTotalQuantity());//利用map集合key不能重复去重，同时计算数量
        }
        return result;
    }

    private ArrayMap<String, Integer> getFormalationPieMap(List<ItemStatisticalInformation> list) {
        Collections.sort(list, new Comparator<ItemStatisticalInformation>() {
            @Override
            public int compare(ItemStatisticalInformation o1, ItemStatisticalInformation o2) {
                Integer i2 = o2.getFormalation();
                Integer i1 = o1.getFormalation();
                return i1.compareTo(i2);
            }
        });//排序
        ArrayMap<String, Integer> formalationPieMap = new ArrayMap<>();
        for(ItemStatisticalInformation information:list){
            String formalation = DataTool.getItemFomalationString(information.getFormalation());
            formalationPieMap.put(formalation,formalationPieMap.getOrDefault(formalation,0) + information.getTotalQuantity()); //利用map集合key不能重复去重，同时计算数量
        }
        return formalationPieMap;
    }

    private void showSelectDataFileDialog() {
        String[] temp = FileTools.getFileList(new File(microMsgPath), ".data");
        if (temp == null || temp.length == 0) {
            toastFactory.showCenterToast("没有数据文件");
        } else {
            final String[] files = new String[temp.length + 1];
            files[0] = "本机数据";
            System.arraycopy(temp, 0, files, 1, temp.length);
            dialogFactory.showSingleSelect(files, new SelectDialogClicker() {
                @Override
                public void click(int checkedItem) {
                    if (checkedItem == 0) {
                        showMyData();
                        return;
                    }
                    File file = new File(microMsgPath, files[checkedItem]);
                    showFileData(file);
                }
            });
        }
    }

    private int[] getPieColorList(ArrayMap<String, Integer> pieMap) {
        if (pieMap == null || pieMap.isEmpty())
            return new int[0];
        int size = pieMap.size();
        int[] colors = new int[size];
        for (int i = 0; i < size; i++)
            colors[i] = Color.parseColor(OtherTools.getRandomColor(88));
        return colors;
    }

    private void showData(List<ItemStatisticalInformation> list) {
        String s = "ID:" + phoneId;
        phoneIdTV.setText(s);
        if(ordredDataList == null)
            ordredDataList = new ArrayList<>();
        ordredDataList.clear();
        ordredDataList.addAll(list);
        setBarChartData(isSortBySelectTimesBT.isChecked());
        setPieChartData(list);
    }

    private void sortDataList(boolean orderBySelectTimes){
//        List<ItemStatisticalInformation> orderedList = new ArrayList<>(list);
        Comparator<ItemStatisticalInformation> comparator;
        if(orderBySelectTimes){
            comparator =  new Comparator<ItemStatisticalInformation>() {
                @Override
                public int compare(ItemStatisticalInformation o1, ItemStatisticalInformation o2) {
                    int diff = o2.getSelectedTimes() - o1.getSelectedTimes();
                    if (diff > 0)
                        return 1;
                    else if (diff < 0)
                        return -1;
                    else {
                        int _diff = o2.getTotalQuantity() - o1.getTotalQuantity();
                        if (_diff > 0)
                            return 1;
                        else if (_diff < 0)
                            return -1;
                        return 0;
                    }
                }
            };
        }else{
            comparator =  new Comparator<ItemStatisticalInformation>() {
                @Override
                public int compare(ItemStatisticalInformation o1, ItemStatisticalInformation o2) {
                    int diff = o2.getTotalQuantity() - o1.getTotalQuantity();
                    if (diff > 0)
                        return 1;
                    else if (diff < 0)
                        return -1;
                    else {
                        int _diff = o2.getSelectedTimes() - o1.getSelectedTimes();
                        if (_diff > 0)
                            return 1;
                        else if (_diff < 0)
                            return -1;
                        return 0;
                    }
                }
            };
        }
        Collections.sort(ordredDataList,comparator);
//        return orderedList;
    }

    private void showMyData() {
        phoneId = AppSetupOperator.getPhoneId() + "(本机)";
        List<ItemStatisticalInformation> list = ItemStatisticalInformationOperator.findAllByOrder();
        showData(list);
    }

    private void showFileData(File file) {
        try {
            String s = FileTools.readEncryptFile(file);
            ArrayMap<String, String> outMap = new ArrayMap<>();
            List<ItemStatisticalInformation> list = new ItemStatisticJSONHelper().parseSharedFile(s, outMap);
            phoneId = outMap.get(JSONHelper.PHONEID);
            showData(list);
        } catch (IOException e) {
            e.printStackTrace();
            toastFactory.showCenterToast("打开文件失败！原因：" + e.getMessage());
        } catch (JSONException e) {
            e.printStackTrace();
            toastFactory.showCenterToast("解析文件失败！原因：" + e.getMessage());
        }
    }

    private void setBarChartData(boolean isSortBySelectTimes) {
        barChart.clear();
        if (ordredDataList == null || ordredDataList.isEmpty()) {
            barChart.setNoDataText("没有数据");
            barChart.invalidate();
            phoneIdTV.setText("---");
            return;
        }
        sortDataList(isSortBySelectTimes);
        final List<String> xLabels = new ArrayList<>();
        xLabels.add("");
        List<BarEntry> entries1 = new ArrayList<>();
        List<BarEntry> entries2 = new ArrayList<>();
        for (int i = 1; i <= ordredDataList.size(); i++) {
            ItemStatisticalInformation item = ordredDataList.get(i - 1);
            BarEntry entry1 = new BarEntry(i, item.getSelectedTimes());
            BarEntry entry2 = new BarEntry(i, item.getTotalQuantity());
            entries1.add(entry1);
            entries2.add(entry2);
            xLabels.add(item.getName());
        }
        BarDataSet set1 = new BarDataSet(entries1, "");
        set1.setColor(selectTimesColor);
        BarDataSet set2 = new BarDataSet(entries2, "");
        set2.setColor(totalQuantityColor);
        BarData data = new BarData(set1, set2);
        float group = 0.6f;
        float width = 0.2f;
        data.setBarWidth(width);
        data.groupBars(0.5f, group, 0f);
        barChart.setData(data);
        barChart.setDescription(getDescription("", false));
        barChart.setVisibleXRangeMaximum(7f);
        setXAxis(xLabels);
        setYAxis();
        setLegend();
        barChart.invalidate();
    }

    private void setPieChartData(List<ItemStatisticalInformation> list) {
        ArrayMap<String, Integer> itemPieMap = getItemPieMap(list);
        ArrayMap<String, Integer> formalationPieMap = getFormalationPieMap(list);
        int[] itemTypeColors = getPieColorList(itemPieMap);
        int[] formalatonColors = getPieColorList(formalationPieMap);
        pieChart1.clear();
        pieChart2.clear();
        if (formalationPieMap == null || formalationPieMap.isEmpty()) {
            pieChart2.setNoDataText("没有Formalation数据");
        } else {
            List<PieEntry> entries2 = new ArrayList<>();
            for (ArrayMap.Entry<String, Integer> entry : formalationPieMap.entrySet()) {
                String formalation = entry.getKey();
                int count = entry.getValue();
                PieEntry peiEntry = new PieEntry(count * 1.0f, formalation);
                entries2.add(peiEntry);
            }
            PieDataSet set2 = new PieDataSet(entries2, "Formalation");
            set2.setColors(formalatonColors);
            set2.setHighlightEnabled(true);
            set2.setSliceSpace(2f);
            set2.setSelectionShift(10f);
            //下面4行用于设置标签显示在外侧
            set2.setValueLinePart1OffsetPercentage(80f);
            set2.setValueLinePart1Length(0.6f);
            set2.setValueLinePart2Length(0.4f);
            set2.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
            PieData data2 = new PieData(set2);
            data2.setDrawValues(true);
            data2.setValueTextColor(Color.BLACK);
            data2.setValueTextSize(12f);
            data2.setValueFormatter(new PercentFormatter(pieChart2));
            pieChart2.setData(data2);
        }
        if (itemPieMap == null || itemPieMap.isEmpty()) {
            pieChart1.setNoDataText("没有ItemType数据");
        } else {
            List<PieEntry> entries1 = new ArrayList<>();
            for (ArrayMap.Entry<String, Integer> entry : itemPieMap.entrySet()) {
                String type = entry.getKey();
                int count = entry.getValue();
                PieEntry pieEntry = new PieEntry(count * 1.0f, type);
                entries1.add(pieEntry);
            }
            PieDataSet set1 = new PieDataSet(entries1, "ItemType");
            set1.setColors(itemTypeColors);
            set1.setHighlightEnabled(true);
            set1.setSliceSpace(2f);
            set1.setSelectionShift(10f);
            //下面4行用于设置标签显示在外侧
            set1.setValueLinePart1OffsetPercentage(80f);
            set1.setValueLinePart1Length(0.6f);
            set1.setValueLinePart2Length(0.4f);
            set1.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
            PieData data1 = new PieData(set1);
            data1.setDrawValues(true);
            data1.setValueTextColor(Color.BLACK);
            data1.setValueTextSize(12f);
            data1.setValueFormatter(new PercentFormatter(pieChart1));
            pieChart1.setData(data1);
        }
        pieChart1.animateY(1000, Easing.EaseInOutQuad);
        pieChart2.animateY(1000, Easing.EaseInOutQuad);
        setPieLengend();
        pieChart1.invalidate();
        pieChart2.invalidate();
    }

    private void setXAxis(final List<String> xLabels) {
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
//                int index = Math.round(value - 1f);
                int index = Math.round(value);
                if (index < 0 || index >= xLabels.size())
                    return "";
                return xLabels.get(index);
            }
        });
    }

    private void setYAxis() {
        YAxis yAxis = barChart.getAxisLeft();
        yAxis.setGranularity(1f);
        yAxis.setAxisMinimum(0f);
        barChart.getAxisRight().setEnabled(false);
    }

    private void setLegend() {
        Legend legend = barChart.getLegend();
        LegendEntry entry1 = new LegendEntry();
        entry1.label = "SelectTimes";
        entry1.formColor = selectTimesColor;
        entry1.form = Legend.LegendForm.LINE;
        entry1.formLineWidth = 4f;
        LegendEntry entry2 = new LegendEntry();
        entry2.label = "TotalQuantity";
        entry2.formColor = totalQuantityColor;
        entry2.form = Legend.LegendForm.LINE;
        entry2.formLineWidth = 4f;
        legend.setCustom(new LegendEntry[]{entry1, entry2});
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
    }

    private Description getDescription(String s, boolean enabled) {
        Description description = new Description();
        description.setText(s);
        description.setTextSize(9f);
        description.setEnabled(enabled);
        return description;
    }

    private void showSelectList(final String[] names) {
        dialogFactory.showMultiSelect(names, new SelectDialogClicker() {
            @Override
            public void click(boolean[] checkedItems) {
                for (int i = checkedItems.length - 1; i > -1; i--) {
                    if (checkedItems[i]) {
                        ItemStatisticalInformationOperator.del(names[i]);
                    }
                }
                showMyData();
            }
        });
    }

    private void initialPieChart() {
        pieChart2.setUsePercentValues(true);
        pieChart2.getDescription().setEnabled(false);
        pieChart2.setDragDecelerationFrictionCoef(0.95f);
        pieChart2.setRotationEnabled(true);
        pieChart2.setHighlightPerTapEnabled(true);
        pieChart2.animateY(1000, Easing.EaseInOutQuad);
        pieChart2.setDrawEntryLabels(false);
        pieChart2.setEntryLabelColor(Color.BLACK);
        pieChart2.setEntryLabelTextSize(12f);
        pieChart2.setDrawHoleEnabled(true);
        pieChart2.setHoleRadius(28f);
        pieChart2.setTransparentCircleRadius(31f);
        pieChart2.setTransparentCircleAlpha(50);
        pieChart2.setTransparentCircleColor(totalQuantityColor);
        pieChart2.setHoleColor(Color.WHITE);
        pieChart2.setDrawCenterText(true);
        pieChart2.setCenterText("Formalation");
        pieChart2.getLegend().setEnabled(true);

        pieChart1.setUsePercentValues(true);
        pieChart1.getDescription().setEnabled(false);
        pieChart1.setDragDecelerationFrictionCoef(0.95f);
        pieChart1.setRotationEnabled(true);
        pieChart1.setHighlightPerTapEnabled(true);
        pieChart1.animateY(1000, Easing.EaseInOutQuad);
        pieChart1.setDrawEntryLabels(false);
        pieChart1.setEntryLabelColor(Color.BLACK);
        pieChart1.setEntryLabelTextSize(12f);
        pieChart1.setDrawHoleEnabled(true);
        pieChart1.setHoleRadius(28f);
        pieChart1.setTransparentCircleRadius(31f);
        pieChart1.setTransparentCircleAlpha(50);
        pieChart1.setTransparentCircleColor(totalQuantityColor);
        pieChart1.setHoleColor(Color.WHITE);
        pieChart1.setDrawCenterText(true);
        pieChart1.setCenterText("ItemType");
        pieChart1.getLegend().setEnabled(true);
    }

    private void setPieLengend(){
        Legend mLegend1 = pieChart1.getLegend();  //设置比例图
        mLegend1.setDrawInside(false);
        mLegend1.setFormSize(11f);//比例块字体大小
        mLegend1.setXEntrySpace(3f);//设置距离饼图的距离，防止与饼图重合
        mLegend1.setYEntrySpace(2f);
        //设置比例块换行...
        mLegend1.setWordWrapEnabled(true);
        mLegend1.setDirection(Legend.LegendDirection.LEFT_TO_RIGHT);
//        mLegend1.setOrientation(Legend.LegendOrientation.VERTICAL);
//        mLegend1.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
//        mLegend.setCenterTextColor();
        mLegend1.setForm(Legend.LegendForm.SQUARE);
        Legend mLegend2 = pieChart2.getLegend();  //设置比例图
        mLegend2.setDrawInside(false);
        mLegend2.setFormSize(11f);//比例块字体大小
        mLegend2.setXEntrySpace(3f);//设置距离饼图的距离，防止与饼图重合
        mLegend2.setYEntrySpace(2f);
        //设置比例块换行...
        mLegend2.setWordWrapEnabled(true);
        mLegend2.setDirection(Legend.LegendDirection.LEFT_TO_RIGHT);
//        mLegend2.setOrientation(Legend.LegendOrientation.VERTICAL);
//        mLegend2.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
//        mLegend.setCenterTextColor();
        mLegend2.setForm(Legend.LegendForm.SQUARE);
    }

    private void initialView() {
        isSortBySelectTimesBT = findViewById(R.id.isSortBySelectTimesRB);
        RadioGroup sortGroup = findViewById(R.id.sortGroup);
        sortGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                setBarChartData(isSortBySelectTimesBT.isChecked());
            }
        });
        dialogFactory = new DialogFactory(this);
        toastFactory = new ToastFactory(this);
        pieChart1 = findViewById(R.id.pieChart1);
        pieChart2 = findViewById(R.id.pieChart2);
        barChart = findViewById(R.id.barChart);
        phoneIdTV = findViewById(R.id.phoneIdTextView);
        phoneIdTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectDataFileDialog();
            }
        });
        TextView resetData = findViewById(R.id.resetData);
        resetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final String[] names = ItemStatisticalInformationOperator.getNameList();
                if (names.length == 0) {
                    toastFactory.showCenterToast("没有统计数据");
                    return;
                }
                new ConfirmPopWindow(ChartActivity.this,"重置统计数据将无法恢复，请谨慎选择！")
                        .show(ChartActivity.this, new IDialogDismiss() {
                            @Override
                            public void onDismiss(boolean isConfirmed, Object... values) {
                                if(isConfirmed)
                                    showSelectList(names);
                            }
                        });
//                dialogFactory.showDefaultConfirmDialog("重置统计数据将无法恢复，请谨慎选择！", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        showSelectList(names);
//                    }
//                });
            }
        });
        initialPieChart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        selectTimesColor = Color.parseColor(OtherTools.getRandomColor(0xff));
        totalQuantityColor = Color.parseColor(OtherTools.getRandomColor(0xff));
        initialView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        showMyData();
    }
}
