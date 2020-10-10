package com.changgou;

import com.google.common.base.Preconditions;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class timeDemo {
    /**
     * 将字符串转换为Date
     * @param input 输入字符串
     * @param pattern 字符串的格式，第一个参数的格式要符合第二个参数
     * @return java.util.Date
     */
    public static Date parseStringToDate(String input, String pattern){
        Preconditions.checkNotNull(input);
        Preconditions.checkNotNull(pattern);
        DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern);
        DateTime dateTime = DateTime.parse(input, formatter);
        return dateTime.toDate();
    }

    @Test
    public void test() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date parse = simpleDateFormat.parse("20141030133525");
        Date date = parseStringToDate("20141030133525","yyyyMMddHHmmss");
        System.out.println(parse);
        System.out.println(date);
    }

    @Test
    public void IntDemo(){
        Map<String,Integer> map = new HashMap<String,Integer>();

        map.put("1212121",new Integer("32"));

        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            String key = entry.getKey();
            Integer value = Integer.valueOf(entry.getValue().toString());
            System.out.println(value);
        }
    }
}
