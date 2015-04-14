package castofo_nower.com.co.nower.support;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class DateManager {

  public static long getTimeStamp(String dateTime) throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date = sdf.parse(dateTime);
    return date.getTime();
  }

  public static String getDateText(Calendar cal) {
    SimpleDateFormat sdf = new SimpleDateFormat("dd - MMMM - yyyy",
                                                new Locale("es_ES"));

    String result = sdf.format(cal.getTime());
    return result;
  }

  public static String getBirthdayText(Calendar cal) {
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    String result = sdf.format(cal.getTime());
    return result;
  }

}
