package gupuru.nearbytest;

public class MusicEntity {

    private String action;
    private long time;

    public MusicEntity(String action, long time){
        this.action = action;
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setTime(long time) {
        this.time = time;
    }

}
