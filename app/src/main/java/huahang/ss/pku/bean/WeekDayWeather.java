package huahang.ss.pku.bean;

/**
 * Created by amoshua on 06/12/2017.
 */

public class WeekDayWeather {
    String week,type,high,low,wind;

    public WeekDayWeather(String week, String type, String high, String low, String wind) {
        this.week = week;
        this.type = type;
        this.high = high;
        this.low = low;
        this.wind = wind;
    }

    public WeekDayWeather() {

    }

    public void setWeek(String week) {
        this.week = week;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public void setWind(String wind) {
        this.wind = wind;
    }

    public String getWeek() {
        return week;
    }

    public String getType() {
        return type;
    }

    public String getHigh() {
        return high;
    }

    public String getLow() {
        return low;
    }

    public String getWind() {
        return wind;
    }

    @Override
    public String toString() {
        return "WeekDayWeather{" +
                "week='" + week + '\'' +
                ", type='" + type + '\'' +
                ", high='" + high + '\'' +
                ", low='" + low + '\'' +
                ", wind='" + wind + '\'' +
                '}';
    }
}
