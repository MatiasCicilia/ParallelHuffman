/**
 * Created by Bruno on 20/2/2017.
 */
public class Tuple<V, T> {
    public V firstValue;
    public T secondValue;

    public Tuple(V v, T t){
        this.firstValue = v;
        this.secondValue = t;
    }
}
