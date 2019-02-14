package aion.dashboard.util;

public class Tuple2<S,T> {

    private final S _1;
    private final T _2;

    public Tuple2(S s, T t) {

        _1 = s;
        _2 = t;
    }

    public S _1() {
        return _1;
    }

    public T _2() {
        return _2;
    }
}
