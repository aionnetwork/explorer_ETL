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


    @Override
    public String toString() {
        String type1 =_1==null ? "null" :_1.getClass().getSimpleName();
        String type2 =_2==null ? "null": _2.getClass().getSimpleName();
        return String.format("(element0:%s=>%s, element1:%s=>%s)", type1, _1, type2, _2);
    }
}
