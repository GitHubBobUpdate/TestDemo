package utils;

import com.google.common.base.Preconditions;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

public class TimeUtils {
    /**
     * 将字符串转换为Date
     * @param input 输入字符串
     * @param pattern 字符串的格式，第一个参数的格式要符合第二个参数
     * @return java.util.Date
     */
    public static Date parseStringToDate(String input, String pattern){
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(pattern);
        DateTime dateTime = dateTimeFormatter.parseDateTime(input);
        return dateTime.toDate();
    }

}
