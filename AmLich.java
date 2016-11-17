package com.anhhong.mod;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ASUS on 11/17/2016.
 */

public class AmLich extends TextView {
    private Handler mHandler = new
            Handler(){
        @Override
        public void handleMessage(Message message) {
            AmLich.this.updateAmLich();
        }
    };
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver(){
        public void onReceive(Context object, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.TIME_TICK".equals(action) || "android.intent.action.TIME_SET".equals(action) || "android.intent.action.TIMEZONE_CHANGED".equals(action)) {
                AmLich.this.mHandler.sendEmptyMessage(0);
            }
            if (!"android.intent.action.LOCALE_CHANGED".equals(action)) {
                return;
            }
            AmLich.this.mHandler.sendEmptyMessage(0);
        }
    };
    private Context mContext;

    public AmLich(Context context) {
        super(context);
        mContext=context;
    }

    public AmLich(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext=context;
    }

    public AmLich(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext=context;
    }

    private void setUpdates(){

        this.setText("aaa");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.TIME_TICK");
        intentFilter.addAction("android.intent.action.TIME_SET");
        intentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
        intentFilter.addAction("android.intent.action.LOCALE_CHANGED");
        this.mContext.registerReceiver(this.mIntentReceiver, intentFilter, null, null);
        this.updateAmLich();
    }
    private void updateAmLich() {
        int day = getday("dd");
        int month = getday("MM");
        int year = getday("yyyy");
        this.setText(ConvertAmLich(day,month,year));
    }
    private String ConvertAmLich (int day,int month,int year){
        int lunarYear;
        int dayNumber = converAmLich(day, month, year);
        int k = (int) ((dayNumber - 2415021.076998695) / 29.530588853);
        int dayStartMonth = getStartDay(k+1,7);

        //kiểm tra đã qua tháng chưa
        if(dayStartMonth >dayNumber){
            dayStartMonth=getStartDay(k,7);
        }
        int month11 = getLunarMonth11(year, 7);
        int month11C =month11;
        if(month11>=dayStartMonth){
            lunarYear =year;
            month11 = getLunarMonth11(year-1, 7);
        } else {
            lunarYear =year+1;
            month11C = getLunarMonth11(year+1, 7);
        }
        int lunarDay = dayNumber-dayStartMonth +1;
        int diff = (dayStartMonth -month11)/29;
        int lunarLeap =0;
        int lunarMonth =diff+11;
        if(month11C-month11 >365){
            int cc =getLeapMonthOffset(month11, 7);
            if ((diff>=cc)){
                lunarLeap=1;
            }
        }
        if (lunarMonth >12){
            lunarMonth-=12;
        }
        if ((lunarMonth>=11 && diff<4)){
            lunarYear-=1;
        }
        return "["+lunarDay+"/"+ lunarMonth+"]";
        //return "["+lunarDay+"/"+ lunarMonth+"]  "+ lunarYear;
        //you can add aray to get name of year.
    }

    private int getLeapMonthOffset(int a11, int timezone) {
        int k,i,last = 0,arc;;
        double a;
        k = (int)((a11 - 2415021.076998695) / 29.530588853 + 0.5);
        i = 1; // We start with the month following lunar month 11
        arc = getSunLongitude(getStartDay(k+i, 7), 7);
        do {
            last = arc;
            i++;
            arc = getSunLongitude(getStartDay(k+i, 7), 7);
        } while (arc != last && i < 14);
        return i-1;
    }

    ;
    private int  converAmLich(int day,int month,int year) {
        int y, jd, m;
        y = -(14 - month) / 12;
        m = month - 12 * y - 3;
        y += year + 4800;
        jd = day + (153 * m + 2) / 5 + 365 * y + y / 4 - 32083;
        if (jd >= 2299161) {
            jd += y / 400 - y / 100 + 38;
        }
        return jd;
    }
    private int getLunarMonth11(int yy,int timezone){
        int off = converAmLich(31, 12, yy) - 2415021;
        int k = (int)(off / 29.530588853);
        int nm = getStartDay(k, timezone);
        int sunLong = getSunLongitude(nm, timezone); // sun longitude at local midnight
        if (sunLong >= 9) {
            nm = getStartDay(k-1, timezone);
        }
        return nm;
    }

    private int getSunLongitude(int jdn, int timezone) {
        double T,T2,dr,M,L0,DL,L;
        T = (jdn - 2451545.5 - timezone/24.0) / 36525; // Time in Julian centuries from 2000-01-01 12:00:00 GMT
        T2 = T*T;
        dr = Math.PI/180; // degree to radian
        M = 357.52910 + 35999.05030*T - 0.0001559*T2 - 0.00000048*T*T2; // mean anomaly, degree
        L0 = 280.46645 + 36000.76983*T + 0.0003032*T2; // mean longitude, degree
        DL = (1.914600 - 0.004817*T - 0.000014*T2)*Math.sin(dr*M);
        DL = DL + (0.019993 - 0.000101*T)*Math.sin(dr*2*M) + 0.000290*Math.sin(dr*3*M);
        L = L0 + DL; // true longitude, degree
        L = L*dr;
        L = L - Math.PI*2*((int)(L/(Math.PI*2))); // Normalize to (0, 2*PI)
        return (int)(L / Math.PI * 6);
    }

    private int getStartDay(int k,int timezone){
        double dr,Jd1,M,Mpr,F,C1,deltat,JdNew;
        double T =k/1236.85;
        double T2 = T*T;
        double T3 = T2*T;
        dr = Math.PI/180;
        Jd1 = 2415020.75933 + 29.53058868*k + 0.0001178*T2 - 0.000000155*T3;
        Jd1 = Jd1 + 0.00033*Math.sin((166.56 + 132.87*T - 0.009173*T2)*dr); // Mean new moon
        M = 359.2242 + 29.10535608*k - 0.0000333*T2 - 0.00000347*T3; // Sun's mean anomaly
        Mpr = 306.0253 + 385.81691806*k + 0.0107306*T2 + 0.00001236*T3; // Moon's mean anomaly
        F = 21.2964 + 390.67050646*k - 0.0016528*T2 - 0.00000239*T3; // Moon's argument of latitude
        C1=(0.1734 - 0.000393*T)*Math.sin(M*dr) + 0.0021*Math.sin(2*dr*M);
        C1 = C1 - 0.4068*Math.sin(Mpr*dr) + 0.0161*Math.sin(dr*2*Mpr);
        C1 = C1 - 0.0004*Math.sin(dr*3*Mpr);
        C1 = C1 + 0.0104*Math.sin(dr*2*F) - 0.0051*Math.sin(dr*(M+Mpr));
        C1 = C1 - 0.0074*Math.sin(dr*(M-Mpr)) + 0.0004*Math.sin(dr*(2*F+M));
        C1 = C1 - 0.0004*Math.sin(dr*(2*F-M)) - 0.0006*Math.sin(dr*(2*F+Mpr));
        C1 = C1 + 0.0010*Math.sin(dr*(2*F-Mpr)) + 0.0005*Math.sin(dr*(2*Mpr+M));
        if (T < -11) {
            deltat= 0.001 + 0.000839*T + 0.0002261*T2 - 0.00000845*T3 - 0.000000081*T*T3;
        } else {
            deltat= -0.000278 + 0.000265*T + 0.000262*T2;
        };
        JdNew = Jd1 + C1 - deltat;
        return (int)(JdNew + 0.5 + timezone/24.0);
    }

    private int getday(String st){
        SimpleDateFormat sdf = new SimpleDateFormat(st);
        Date d = new Date();
        st = sdf.format(d);
        int num=0;
        try {
            num = Integer.parseInt(st) ;
        } catch(NumberFormatException nfe) {

        }
        return num;
    }
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.setUpdates();
    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.setUpdates();
    }
    @Override
    protected void onVisibilityChanged(View view, int n) {
        super.onVisibilityChanged(view, n);
        this.setUpdates();
    }
    @Override
    protected void onWindowVisibilityChanged(int n) {
        super.onWindowVisibilityChanged(n);
        this.setUpdates();
    }

}
