import java.util.Calendar;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class TableReservation {

    public String userId;
    public String name;
    public String numberOfPlaces;
    public String date;
    public String time;
    public String phone;
    public String wishes;
    public int numberOfCompletedSells;

    private int weekDayOfReservation;
    private Calendar privateCalendarToday;
    private Calendar privateCalendarReservation;

    public TableReservation(String id) {
        userId = id;
        numberOfCompletedSells = 0;
    }


    public String inputNumberOfPlaces(String str) {
        for (int i=0; i<str.length(); i++) {
            if((str.charAt(i) >= '0' && str.charAt(i) <= '9')
                    || (str.charAt(i) == ' ')
                    || (str.charAt(i) == '-')) {
            }
            else {
                return "/mistake";
            }
        }
        return str;
    }

    public String inputDate(String str) {
        final String DATE_FORMAT_NO_YEAR = "dd.MM";
        final String DATE_FORMAT_YEAR = "dd.MM.yyyy";
        try {
            return checkFroDateFormat(DATE_FORMAT_NO_YEAR, DATE_FORMAT_YEAR, str);
            } catch (Exception e) {
                return "/mistake";
            }
    }

    private String checkFroDateFormat(String DATE_FORMAT_NO_YEAR, String DATE_FORMAT_YEAR, String reservationDate)
            throws Exception {
        if(reservationDate.length() == 5) {
            reservationDate = reservationDate + ".1970";
        }
        DateFormat dateFormatNoYear = new SimpleDateFormat(DATE_FORMAT_YEAR);
        DateFormat dateFormatYear = new SimpleDateFormat(DATE_FORMAT_YEAR);
        dateFormatNoYear.setLenient(false);
        dateFormatYear.setLenient(false);
        String todayDate = new SimpleDateFormat(DATE_FORMAT_YEAR).format(new Date());
        Date dateToday = dateFormatYear.parse(todayDate);
        Date dateReservation = dateFormatNoYear.parse(reservationDate);
        // check for 1970
        Calendar calendarReservation = Calendar.getInstance();
        calendarReservation.setTime(dateReservation);
        Calendar calendarToday = Calendar.getInstance();
        calendarToday.setTime(dateToday);
        if(calendarReservation.get(Calendar.YEAR) == 1970 )
            calendarReservation.set(Calendar.YEAR, calendarToday.get(Calendar.YEAR));
        if(calendarReservation.get(Calendar.YEAR) < calendarToday.get(Calendar.YEAR))
            return "/mistake";
        if(calendarReservation.after(calendarToday) || calendarReservation.equals(calendarToday)) {
            weekDayOfReservation = calendarReservation.get(Calendar.DAY_OF_WEEK);
            privateCalendarToday = calendarToday;
            privateCalendarReservation = calendarReservation;
            return reservationDate;
        }
        else if(calendarReservation.get(Calendar.MONTH) == 0) { //check for reservation for next year
            calendarReservation.set(Calendar.YEAR, calendarToday.get(Calendar.YEAR) + 1);
            if(calendarReservation.after(calendarToday) || calendarReservation.equals(calendarToday)) {
                weekDayOfReservation = calendarReservation.get(Calendar.DAY_OF_WEEK);
                privateCalendarToday = calendarToday;
                privateCalendarReservation = calendarReservation;
                return reservationDate;
            }
        }
        else
            throw new Exception();
        return "/mistake";
    }

    public String inputTime(String str) {
        final String TIME_FORMAT = "HH:mm";
        try {
            return checkForTimeFormat(TIME_FORMAT, str);
        } catch (Exception e) {
            return "/mistake";
        }
    }

    private String checkForTimeFormat(String TIME_FORMAT, String reservationTime) throws Exception {
        DateFormat df = new SimpleDateFormat(TIME_FORMAT);
        df.setLenient(false);
        String todayTime = new SimpleDateFormat(TIME_FORMAT).format(new Date());
        Date timeToday = df.parse(todayTime);
        Date timeReservation = df.parse(reservationTime);
        Date timeOpening = df.parse("15:00");
        Date timeClosing = df.parse("23:00");
        Date timeClosingWeekend = df.parse("23:59");
        Date timeOpeningWeekend = df.parse("00:00");
        Date timeClosingWeekend2 = df.parse("02:00");
        if(privateCalendarReservation.equals(privateCalendarToday)) {
            if(weekDayOfReservation == 6) {
                if(timeReservation.after(timeToday)) {
                    if (timeReservation.after(timeOpening) && timeReservation.before(timeClosingWeekend))
                        return reservationTime;
                    else
                        return "/mistake";
                }
                else return "/mistake";
            }
            else if(weekDayOfReservation == 7) {
                if(timeReservation.after(timeToday)) {
                    if ((timeReservation.after(timeOpening) && timeReservation.before(timeClosingWeekend))
                            || (timeReservation.after(timeOpeningWeekend) && timeReservation.before(timeClosingWeekend2)))
                        return reservationTime;
                    else
                        return "/mistake";
                }
                else return "/mistake";
            }
            else if(timeReservation.after(timeToday)) {
                if (timeReservation.after(timeOpening) && timeReservation.before(timeClosing))
                    return reservationTime;
                else
                    return "/mistake";
            }
        }
        else if(privateCalendarReservation.after(privateCalendarToday)){
            if(weekDayOfReservation == 6) {
                if (timeReservation.after(timeOpening) && timeReservation.before(timeClosingWeekend))
                    return reservationTime;
                else
                    return "/mistake";

            }
            else if(weekDayOfReservation == 7) {
                if ((timeReservation.after(timeOpening) && timeReservation.before(timeClosingWeekend))
                        || (timeReservation.after(timeOpeningWeekend) && timeReservation.before(timeClosingWeekend2)))
                    return reservationTime;
                else
                    return "/mistake";
            }
            else if (timeReservation.after(timeOpening) && timeReservation.before(timeClosing))
                return reservationTime;
            else
                return "/mistake";
        }
        return "/mistake";
    }

    public String inputPhone(String str) {
        if(str.length() < 10)
            return "/mistake";
        for (int i=0; i<str.length(); i++) {
            if((str.charAt(i) >= '0' && str.charAt(i) <= '9')
                    || (str.charAt(i) == '+')
                    || (str.charAt(i) == ' ')) {
            }
            else {
                return "/mistake";
            }
        }
        return str;
    }

    public String checkForArrowSymbolLast(String str) {
        for(int i=str.length()-1; i>=0; i--) {
            if(str.charAt(i) == '>') {
                str = str.substring(i+2);
                break;
            }
        }
        return str;
    }

    public String checkForArrowSymbolFirst(String str) {
        for(int i=0; i<str.length(); i++) {
            if(str.charAt(i) == '>') {
                str = str.substring(0, i-1);
                break;
            }
        }
        return str;
    }

    public boolean checkReservationForDelete(String resDate, String resTime) {

        return false;
    }

}
