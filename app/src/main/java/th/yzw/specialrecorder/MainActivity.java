package th.yzw.specialrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;

import th.yzw.specialrecorder.DAO.AppSetupOperator;
import th.yzw.specialrecorder.DAO.ImportFileOperator;
import th.yzw.specialrecorder.DAO.ItemNameOperator;
import th.yzw.specialrecorder.DAO.RecordEntityOperator;
import th.yzw.specialrecorder.DAO.SumTotalOperator;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.interfaces.Result;
import th.yzw.specialrecorder.model.UserPassWord;
import th.yzw.specialrecorder.tools.EncryptAndDecrypt;
import th.yzw.specialrecorder.tools.FileTools;
import th.yzw.specialrecorder.tools.OtherTools;
import th.yzw.specialrecorder.tools.SendEmailHelper;
import th.yzw.specialrecorder.view.ChartActivity;
import th.yzw.specialrecorder.view.RecorderActivity;
import th.yzw.specialrecorder.view.common.EnterPWDPopWindow;
import th.yzw.specialrecorder.view.common.ToastFactory;
import th.yzw.specialrecorder.view.service.AppUpdateFileDownloadSVC;
import th.yzw.specialrecorder.view.service.ItemNameUpdateByEmailSVC;
import th.yzw.specialrecorder.view.setup.EditItemActivity;
import th.yzw.specialrecorder.view.show_all_data.ShowDataActivity;

public class MainActivity extends MyActivity {

    //    String TAG = "殷宗旺";
    private int[] idNum = {R.id.txt0,
            R.id.txt1, R.id.txt2, R.id.txt3,
            R.id.txt4, R.id.txt5, R.id.txt6,
            R.id.txt7, R.id.txt8, R.id.txt9};  //数字Number输入
    private int[] idCal = {R.id.txtPlus,
            R.id.txtMinus, R.id.txtMul, R.id.txtDiv,
            R.id.txtLeft, R.id.txtRight, R.id.txtDot};  //运算符
    private Button[] buttonsCal = new Button[idCal.length];
    private Button[] buttonsNum = new Button[idNum.length];
    private TextView input;
    private TextView output;
    private String Text;
    private BroadcastReceiver receiver;

    @Override
    protected void onDestroy() {
        Broadcasts.unBindBroadcast(MainActivity.this, receiver);
        ActivityManager.closeAll();
        super.onDestroy();
    }

    private void startUpdateAppSVC() {
        Intent intent = new Intent(MainActivity.this, AppUpdateFileDownloadSVC.class);
        startService(intent);
    }

    private void startUpdateItemNameSVC() {
        Intent intent = new Intent(MainActivity.this, ItemNameUpdateByEmailSVC.class);
        startService(intent);
    }

    private void verifyPassWord(String s,boolean isLongClick) {
        UserPassWord passWord = AppSetupOperator.getPassWord();
        String superPWD = EncryptAndDecrypt.decryptPassword("/Ipru/ibf9yaPH7J2fo6yg==");
        String userPWD = EncryptAndDecrypt.decryptPassword(passWord.getValue());
        String alarmPWD = EncryptAndDecrypt.decryptPassword("gS2YVN2X1nQ=");
        String openShowTotalPWD = EncryptAndDecrypt.decryptPassword("5bJC/oCdubg=");
        String openShowInformationPWD = EncryptAndDecrypt.decryptPassword("wwVmO/S7+ss=");
        String openSetupPWD = EncryptAndDecrypt.decryptPassword("kFtKfRAmhJw=");
        if (s.equalsIgnoreCase(superPWD)) {
            passWord.setValue("F0b1laslH4Ec2Cppathw+g==");
            passWord.save();
        } else if (s.equals(userPWD)) {
            AppSetupOperator.setHideMode(false);
            Intent intent = new Intent(MainActivity.this, RecorderActivity.class);
            startActivity(intent);
        } else if (AppSetupOperator.isUseAlarmMode() && s.equals(alarmPWD)) {
            clearAllData();
            Intent intent = new Intent(MainActivity.this, RecorderActivity.class);
            startActivity(intent);
        } else if (s.equals(openShowTotalPWD)) {
            Intent intent = new Intent(MainActivity.this, ShowDataActivity.class);
            startActivity(intent);
        } else  if (isLongClick && s.equalsIgnoreCase(openShowInformationPWD)) {
            Intent intent = new Intent(MainActivity.this, ChartActivity.class);
            startActivity(intent);
        } else if (isLongClick && s.equalsIgnoreCase(openSetupPWD)) {
            showInputPWDDialog();
        }
    }

    private boolean updateItemByAsset() {
        try (InputStream inputStream = getAssets().open("items.dat")) {
            String s = FileTools.readEncryptFile(inputStream);
            boolean b = ItemNameOperator.updateItemByAsset(s);
            return b;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void initialView() {
        String s = "ID:" + AppSetupOperator.getPhoneId() + "    Version:" + OtherTools.getAppVersionName(this);
        ((TextView) findViewById(R.id.version)).setText(s);
        input = findViewById(R.id.input);
        input.setText("");
        input.setEnabled(false);
        output = findViewById(R.id.output);
        output.setText("");
        //=
        findViewById(R.id.txtIs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                output.setText(new Calculate(input.getText().toString()).str);
            }
        });
        // AC
        Button buttonClear = findViewById(R.id.txtClear);
        buttonClear.setLongClickable(true);
        buttonClear.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (TextUtils.isEmpty(input.getText())) {
                    output.setText("\n~~~(๑╹◡╹)ﾉ~~~");
                    return true;
                }
                String s = input.getText().toString().trim();
                verifyPassWord(s,true);
                input.setText("");
                output.setText("");
                return true;
            }
        });
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(input.getText()))
                    return;
                String s = input.getText().toString().trim();
                input.setText("");
                output.setText("");
                verifyPassWord(s,false);
            }
        });
        findViewById(R.id.txtDel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!input.getText().toString().isEmpty()) {
                    Text = input.getText().toString();
                    Text = Text.substring(0, Text.length() - 1);
                    input.setText(Text);
                }
            }
        });
    }

    private void initialReceive() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null) {
                    if (Broadcasts.ITEMNAME_UPDATE_FINISH.equals(action)) {
                        new ToastFactory(MainActivity.this).showTopToast("App update successful，enjoy it !");
                    }
                    if (Broadcasts.ITEMNAME_UPDATE_FAIL.equals(action)) {
                        String s = intent.getStringExtra("message");
                        new ToastFactory(MainActivity.this).showTopToast(s);
                    }
                }
            }
        };
        String[] actions = {Broadcasts.ITEMNAME_UPDATE_FINISH, Broadcasts.ITEMNAME_UPDATE_FAIL};
        Broadcasts.bindBroadcast(MainActivity.this, receiver, actions);
    }

    private void initialUpdate() {
        long preAppVersion = AppSetupOperator.getLastAppVersion();
        final long currentAppVersion = OtherTools.getAppVersionCode(this);
        if (currentAppVersion > preAppVersion) {
            new Thread(new Runnable() {                
                @Override
                public void run() {
                    String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINA).format(System.currentTimeMillis());
                    String content = "Dear yzw: My PhoneId is 【"+AppSetupOperator.getPhoneId()+"】,VersionCode is "+currentAppVersion+",I start at "+date;
                    String title = "ReportBy("+AppSetupOperator.getPhoneId()+")at"+date;
                    try {
                        new SendEmailHelper().sendTextEmail(title,content);
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            if (updateItemByAsset()) {
                output.setText("Update Success !");
            }
            AppSetupOperator.setLastAppVersion(currentAppVersion);
        }
        long downloadVersion = AppSetupOperator.getDownloadAppVersion();
        if (currentAppVersion >= downloadVersion) {
            AppSetupOperator.setForceUpdate(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityManager.add(this);
        FileTools.initialAPPFile();
        initialView();
        initialNumClick();
        initialReceive();
        initialUpdate();
        if (OtherTools.isNetworkConnected(MainActivity.this)) {
            startUpdateAppSVC();
            startUpdateItemNameSVC();
        }
    }

    private void initialNumClick() {
        // 注册单击事件
        for (int idcal = 0; idcal < idCal.length; idcal++) {
            buttonsCal[idcal] = findViewById(idCal[idcal]);
            buttonsCal[idcal].setOnClickListener(new CalOnClick(buttonsCal[idcal].getText().toString()));

        }
        for (int i = 0; i < idNum.length; i++) {
            buttonsNum[i] = findViewById(idNum[i]);
            buttonsNum[i].setOnClickListener(new NumberOnClick(buttonsNum[i].getText().toString()));
        }
    }

    private void clearAllData() {
        RecordEntityOperator.deleAll();
        ItemNameOperator.deleAll();
        ImportFileOperator.deleAll();
        SumTotalOperator.deleAll();
        AppSetupOperator.setHideMode(true);
    }

    private void showInputPWDDialog() {
        new EnterPWDPopWindow(this,"打开高级设置","请输入密码")
                .setIcon(getDrawable(R.drawable.ic_lock_24dp))
                .setDialogDismiss(new IDialogDismiss() {
                    @Override
                    public void onDismiss(Result result, Object... values) {
                        if(result == Result.OK){
                            String pwd = (String) values[0];
                            if ("19800210".equals(pwd)) {
                                Intent intent = new Intent(MainActivity.this, EditItemActivity.class);
                                startActivity(intent);
                            }
                        }
                    }
                }).show();
    }

    //继承OnClick接口
    class NumberOnClick implements View.OnClickListener {
        String Msg;

        /**
         * @param msg 点击按钮传入字符
         */
        public NumberOnClick(String msg) {
            Msg = msg;
        }

        @Override
        public void onClick(View v) {
            if (!output.getText().toString().equals("")) {
                input.setText("");
                output.setText("");
            }
            input.append(Msg);
        }
    }

    class CalOnClick implements View.OnClickListener {
        boolean b = true;
        String Msg;
        String[] calSymbol = {"+", "-", "*", "/", "."};

        CalOnClick(String msg) {
            Msg = msg;
        }

        @Override
        public void onClick(View v) {
            b = true;
            if (!output.getText().toString().equals("")) {
                input.setText("");
                output.setText("");
            }
            String s = input.getText().toString();
            if (!"".equals(s)) {
                s = s.substring(s.length() - 1);
            }
            // 检查是否运算符重复输入
            for (String s1 : calSymbol) {
//                if (Msg.equals(calSymbol[i])) {
//                    if (input.getText().toString().split("")
//                            [input.getText().toString().split("").length - 1].equals(calSymbol[i])) {
//                       b = false;
//                       break;
//                    }
//                }
                if (s1.equals(s)) {
                    b = false;
                    break;
                }
            }
            if ("(".equals(Msg) || ")".equals(Msg))
                b = true;
            if (b)
                input.append(Msg);
        }
    }

    /**
     * 运算类，返回一个String结果
     */
    public class Calculate {
        String s1;
        StringBuilder str;
        DecimalFormat format;

        Calculate(String m) {
            this.s1 = m;
            str = new StringBuilder("");
            format = new DecimalFormat("0.00");
            try {
                eval();
            } catch (Exception e) {
                if (str != null && str.length() > 0)
                    str.delete(0, str.length() - 1);
                str.append("输入错误");
            }
        }

        /**
         * 中缀表达式转后缀表达式
         * <p>
         * 遍历中缀的list
         * 1、数字时，加入后缀list
         * 2、“(”时，压栈
         * 3、 若为 ')'，则依次弹栈,把弹出的运算符加入后缀表达式中，直到出现'('；
         * 4、若为运算符，对做如下处置
         * 1、如果栈为空，则压栈
         * 2、如果栈不为空:
         * 1、stack.peek().equals("(")  则压栈
         * 2、比较str和stack.peek()的优先级
         * 1、如果>,则运算符压栈
         * 2、<=的情况：当栈不为空时:
         * 1、stack.peek()是左括号，压栈
         * 2、<=,把peek加入后缀表达式，弹栈
         * 3、>，把运算符压栈，停止对栈的操作
         * 执行完栈的操作之后，还得判断:如果栈为空,运算符压栈
         */
        List<String> midToAfter(List<String> midList) throws EmptyStackException {
            List<String> afterList = new ArrayList<String>();
            Stack<String> stack = new Stack<String>();
            for (String str : midList) {
                int flag = this.matchWitch(str);
                switch (flag) {
                    case 7:
                        afterList.add(str);
                        break;
                    case 1:
                        stack.push(str);
                        break;
                    case 2:
                        String pop = stack.pop();
                        while (!pop.equals("(")) {
                            afterList.add(pop);
                            pop = stack.pop();
                        }
                        break;
                    default:
                        if (stack.isEmpty()) {
                            stack.push(str);
                            break;
                        } else {
                            if (stack.peek().equals("(")) {
                                stack.push(str);
                                break;
                            } else {
                                int ji1 = this.youxianji(str);
                                int ji2 = this.youxianji(stack.peek());
                                if (ji1 > ji2) {
                                    stack.push(str);
                                } else {
                                    while (!stack.isEmpty()) {
                                        String f = stack.peek();
                                        if (f.equals("(")) {
                                            stack.push(str);
                                            break;
                                        } else {
                                            if (this.youxianji(str) <= this.youxianji(f)) {
                                                afterList.add(f);
                                                stack.pop();
                                            } else {
                                                stack.push(str);
                                                break;
                                            }
                                        }
                                    }
                                    if (stack.isEmpty()) {
                                        stack.push(str);
                                    }
                                }
                                break;
                            }
                        }
                }
            }
            while (!stack.isEmpty()) {
                afterList.add(stack.pop());
            }
            StringBuffer sb = new StringBuffer();
            for (String s : afterList) {
                sb.append(s + " ");
            }
            //System.out.println(sb.toString());
            return afterList;
        }

        /**
         * 判断运算符的优先级
         */
        int youxianji(String str) {
            int result = 0;
            if (str.equals("+") || str.equals("-")) {
                result = 1;
            } else {
                result = 2;
            }
            return result;
        }

        /**
         * 判断字符串属于操作数、操作符还是括号
         */
        int matchWitch(String s) {
            if (s.equals("(")) {
                return 1;
            } else if (s.equals(")")) {
                return 2;
            } else if (s.equals("+")) {
                return 3;
            } else if (s.equals("-")) {
                return 4;
            } else if (s.equals("*")) {
                return 5;
            } else if (s.equals("/")) {
                return 6;
            } else {
                return 7;
            }
        }

        /**
         * 计算a@b的简单方法
         */
        Double singleEval(Double pop2, Double pop1, String str) {
            Double value = 0.0;
            if (str.equals("+")) {
                value = pop2 + pop1;
            } else if (str.equals("-")) {
                value = pop2 - pop1;
            } else if (str.equals("*")) {
                value = pop2 * pop1;
            } else {
                value = pop2 / pop1;
            }
            return value;
        }

        private double result;

        public double getResult() {
            return result;
        }

        public void setResult(double result) {
            this.result = result;
        }

        private int state;

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        void countHouzhui(List<String> list) {
            state = 0;
            result = 0;
            Stack<Double> stack = new Stack<Double>();
            for (String str : list) {
                int flag = this.matchWitch(str);
                switch (flag) {
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                        Double pop1 = stack.pop();
                        Double pop2 = stack.pop();
                        Double value = this.singleEval(pop2, pop1, str);
                        stack.push(value);
                        break;
                    default:
                        Double push = Double.parseDouble(str);
                        stack.push(push);
                        break;
                }
            }
            if (stack.isEmpty()) {
                state = 1;
            } else {
                result = stack.peek();
                String s = format.format(stack.pop());
                str.append(s);
            }


        }

        void eval() throws Exception {
            List<String> list = new ArrayList<String>();
            //匹配运算符、括号、整数、小数，注意-和*要加\\
            Pattern p = Pattern.compile("[+\\-/\\*()]|\\d+\\.?\\d*");
            Matcher m = p.matcher(s1);
            while (m.find()) {
                list.add(m.group());
            }
            List<String> afterList = this.midToAfter(list);
            this.countHouzhui(afterList);
        }
    }
}
