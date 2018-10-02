package breed;

import java.util.AbstractMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

/** classes clients need to import
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
*/

public class AlgebraVector
{
    /**
     * Returns a Stream<Map.Entry<R,V>> such that
     *   Stream<Map.Entry<R,V>> :
     *      [{f(t1,u1),g(vt1,vu1)}, {f(t1,u2),g(vt1,vu2)}];
     * provided two arguments
     *   Map.Entry<T,V> = {t1,vt1};
     * and
     *   Map<U,V> = [{u1,vu1}, {u2,vu2}];
     *
     * @param e Map.Entry<T,V>
     * @param m Map<U,V>>
     * @param f BiFunction<T,U,R> such that R r = f(T t, U u)
     * @param g SimpleOperator<V> such that V v = g(V vt, V vu)
     * @return Stream<Map.Entry<R,V>>
     */
    public static <T,U,R,V> Stream<Map.Entry<R,V>>
    outerProduct(
        Map.Entry<T,V> e,
        Map<U,V> m,
        BiFunction<T,U,R> f,
        BinaryOperator<V> g)
    {
        return m.entrySet().stream()
            .map(eu -> new AbstractMap.SimpleEntry<R,V>(
                f.apply(e.getKey(),   eu.getKey()),
                g.apply(e.getValue(), eu.getValue()))
            );
    }

    /**
     * Returns a Stream<Map.Entry<R,V>> such that
     *   Stream<Map.Entry<R,V>> :
     *      [{f(t1,u1),g(vt1,vu1)}, {f(t1,u2),g(vt1,vu2)}];
     * provided two arguments
     *   Map.Entry<T,V> = {t1,vt1};
     * and
     *   Stream<Map.Entry<U,V>> = [{u1,vu1}, {u2,vu2}];
     *
     * @param e Map.Entry<T,V>
     * @param s Stream<Map.Entry<U,V>>
     * @param f BiFunction<T,U,R> such that R r = f(T t, U u)
     * @param g SimpleOperator<V> such that V v = g(V vt, V vu)
     * @return Stream<Map.Entry<R,V>>
     */
    public static <T,U,R,V> Stream<Map.Entry<R,V>>
    outerProduct(
        Map.Entry<T,V> e,
        Stream<Map.Entry<U,V>> s,
        BiFunction<T,U,R> f,
        BinaryOperator<V> g)
    {
        return s
            .map(eu -> new AbstractMap.SimpleEntry<R,V>(
                f.apply(e.getKey(),   eu.getKey()),
                g.apply(e.getValue(), eu.getValue()))
            );
    }

    /**
    public static void main(String[] args)
    {
        final LinkedHashMap<String, Double> a
            = new LinkedHashMap<String, Double>();
        a.put("A", 2.0d);

        final LinkedHashMap<Integer, Double> b
            = new LinkedHashMap<Integer, Double>();
        b.put(3, 4.0d);
        b.put(2, 5.0d);

        final LinkedHashMap<String, Double> c
            = new LinkedHashMap<String, Double>();
        c.put("X", 1.0d);
        c.put("Y", 2.0d);
        c.put("Z", 3.0d);

        System.out.println("==== Map a: ====");
        a.entrySet().stream().forEach(e -> System.out.println(e));
        System.out.println("==== Map b: ====");
        b.entrySet().stream().forEach(e -> System.out.println(e));
        System.out.println("==== Map c: ====");
        c.entrySet().stream().forEach(e -> System.out.println(e));

        System.out.println("==== outerProduct a*b*c: ====");
        Map<String, Double> ab = null;
        Map<String, Double> abc = null;
        abc = a.entrySet().stream()
            .flatMap(entry ->
                outerProduct(entry,
                    b,
                    (t,u) -> t + Integer.toString(u),
                    (vt,vu) -> vt * vu
                )
            )
            .flatMap(entry ->
                outerProduct(entry,
                    c,
                    (t,u) -> t + u,
                    (vt,vu) -> vt * vu
                )
            )
            .collect(Collectors
                .toMap(
                    er -> er.getKey(),
                    er -> er.getValue(),
                    (e1,e2) -> e2,
                    () -> new LinkedHashMap<String, Double>()
                )
            );
        abc.entrySet().stream().forEach(e -> System.out.println(e));
    }
    */
}