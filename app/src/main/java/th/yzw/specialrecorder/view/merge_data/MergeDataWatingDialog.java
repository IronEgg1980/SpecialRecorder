package th.yzw.specialrecorder.view.merge_data;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatTextView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Locale;
import java.util.Objects;

import th.yzw.specialrecorder.Broadcasts;
import th.yzw.specialrecorder.R;

public class MergeDataWatingDialog extends DialogFragment {
    private AppCompatTextView fileNameTv;
    private TextView finishTextTv,informationTv;
    private ProgressBar progressBar;
    private BroadcastReceiver receiver;

    private void initialReceiver(){
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (Broadcasts.CHANGE_LOADING_TEXT.equals(action)) {
                    String message = intent.getStringExtra("message");
                    changeInformation(message);
                }else if(Broadcasts.CHANGE_LOADING_PROGRESS.equals(action)){
                    int progress = intent.getIntExtra("progress",0);
                    float value = intent.getFloatExtra("value",0f);
                    changeProgress(progress,value);
                }else if(Broadcasts.CHANGE_LOADING_FILENAME.equals(action)){
                    String fileName = intent.getStringExtra("message");
                    changeFile(fileName);
                }
            }
        };
        String[] actions = {Broadcasts.CHANGE_LOADING_TEXT,Broadcasts.CHANGE_LOADING_PROGRESS,Broadcasts.CHANGE_LOADING_FILENAME};
        Broadcasts.bindBroadcast(getContext(),receiver,actions);
    }

    public void changeProgress(int progress,float value) {
        if (progressBar != null)
            progressBar.setProgress(progress);
        if(finishTextTv!=null)
            finishTextTv.setText(String.format(Locale.CHINA,"%1$.2f%2$s",value,"%"));
    }
    public void changeFile(String fileName){
        if(fileNameTv!=null)
            this.fileNameTv.setText(fileName);
        changeInformation("");
        changeProgress(0,0.00f);
    }
    public void changeInformation(String information){
        if(informationTv!=null)
            informationTv.setText(information);
    }
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            int width = (int) (dm.widthPixels * 0.8);
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            Window window = dialog.getWindow();
            if (window!=null) {
                window.setWindowAnimations(R.style.CommonDialogAnim);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.setLayout(width,height);
            }
        }
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.merge_loading,container,false);
        fileNameTv = view.findViewById(R.id.file_name_tv);
        finishTextTv = view.findViewById(R.id.finishText);
        informationTv = view.findViewById(R.id.information);
        progressBar = view.findViewById(R.id.progress);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialReceiver();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Broadcasts.unBindBroadcast(getContext(),receiver);
    }
}
