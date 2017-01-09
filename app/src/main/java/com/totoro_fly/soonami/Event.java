package com.totoro_fly.soonami;

/**
 * Created by totoro-fly on 2017/1/9.
 */

public class Event {
    final String title;
    final Long time;
    final int tsunamiAlert;

    public Event(String title, Long time, int tsunamiAlert) {
        this.title = title;
        this.time = time;
        this.tsunamiAlert = tsunamiAlert;
    }

    public String getTitle() {
        return title;
    }

    public Long getTime() {
        return time;
    }

    public int getTsunamiAlert() {
        return tsunamiAlert;
    }
}
