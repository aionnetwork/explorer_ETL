package aion.dashboard.utility;

public class Timer {

    private long startTime;

    private long stopTime;

    private String name;

    public Timer() {
        name = null;
    }

    public Timer(String name){ this.name = name;}


    public void start() {
        startTime = System.nanoTime();
    }


    public void end() {
        stopTime = System.nanoTime();
    }

    public long getTime() {
        return stopTime - startTime;
    }

    public double getMilliTime() {
        return getTime() / 1000_0000D;
    }

    public double getSecondsTime() {
        return getTime() / 1_000_000_000D;
    }


    @Override
    public String toString() {
        return name ==  null? "Timer :": name+" :";
    }
}
