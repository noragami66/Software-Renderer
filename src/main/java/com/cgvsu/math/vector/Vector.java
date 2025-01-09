package com.cgvsu.math.vector;

public interface Vector <T extends Vector<T>> {
    void add(T v);
    void sub(T v);

    void sub(Vector3f var1, Vector3f var2);

    T multiply(float c);
    void mult(float c);

    T divide(float c);

    void div(float c);

    float length();

    T normal();

    boolean equals(T other);
}