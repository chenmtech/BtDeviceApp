package com.cmtech.android.bledeviceapp.dataproc.hrproc;

import com.cmtech.android.bledeviceapp.model.Account;
import com.cmtech.android.bledeviceapp.util.MathUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class HRProcessor {
    // 计算燃烧卡路里
    public static int calculateCalories(short aveHr, int timeLengthInSecond, Account person) {
        int age = new GregorianCalendar().get(Calendar.YEAR)-new Date(person.getBirthday()).getYear();
        int burned = 0;
        if(person.getGender() == Account.MALE) {
            burned = (int)( ( (-55.0969 + (0.6309*aveHr) + (0.1988*person.getWeight()) + (0.2017*age) )/4.184 )*timeLengthInSecond/60 );
            //ViseLog.e(new GregorianCalendar().get(Calendar.YEAR)+ " "+ new Date(person.getBirthday()).getYear() + " " +age+" "+hrAve+ " "+ person.getWeight() + " " + getRecordSecond());
        } else {
            burned = (int)( ((-20.4022 + (0.4472*aveHr) - (0.1263*person.getWeight()) + (0.074*age))/4.184)*timeLengthInSecond/60 );
        }
        return burned;
    }

    // 计算心率变异性，单位：毫秒
    public static short calculateHRVInMs(List<Short> hrList) {
        List<Short> hrListMs = new ArrayList<>();
        for(Short d : hrList) {
            hrListMs.add((short)(60000/d));
        }
        return (short) MathUtil.shortStd(hrListMs);
    }
}
