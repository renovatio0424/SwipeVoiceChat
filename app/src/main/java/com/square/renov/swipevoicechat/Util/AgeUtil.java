package com.square.renov.swipevoicechat.Util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class AgeUtil {

    public static int getAgeFromBirth(int birthYear){
        Calendar calendar = new GregorianCalendar(Locale.KOREA);
        int nYear = calendar.get(Calendar.YEAR);
        int age = nYear - birthYear + 1;
        return age;
    }
}
