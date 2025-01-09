package com.cgvsu.math.vector;

import static com.cgvsu.math.Global.EPS;

// Это заготовка для собственной библиотеки для работы с линейной алгеброй
public class Vector3f implements Cloneable, Vector<Vector3f>  {
    float x, y, z;

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public static Vector3f addition(final Vector3f v1, final Vector3f v2) {
        return new Vector3f(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z);
    }

    public final void add(Vector3f var1) {
        this.x += var1.x;
        this.y += var1.y;
        this.z += var1.z;
    }

    @Override
    public void sub(Vector3f v) {
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;
    }

    public static Vector3f subtraction(final Vector3f v1, final Vector3f v2) {
        return new Vector3f(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
    }

    @Override
    public final void sub(Vector3f var1, Vector3f var2) {
        this.x = var1.x - var2.x;
        this.y = var1.y - var2.y;
        this.z = var1.z - var2.z;
    }

    @Override
    public Vector3f multiply(float c) {
        return new Vector3f(c * x, c * y, c * z);
    }

    @Override
    public void mult(float c) {
        x *= c;
        y *= c;
        z *= c;
    }

    public final void mul(Vector3f var1, Vector3f var2) { //векторное произведение
        this.x = var1.y * var2.z - var1.z * var2.y;
        this.y = -(var1.x * var2.z - var1.z * var2.x);
        this.z = var1.x * var2.y - var1.y * var2.x;
    }

    @Override
    public Vector3f divide(float c) {
        if (c < EPS) {
            throw new ArithmeticException("Division by zero is not allowed.");
        }
        return new Vector3f(x / c, y / c, z / c);
    }

    @Override
    public void div(float c) {
        if (c < EPS) {
            throw new ArithmeticException("Division by zero is not allowed.");
        }
        x /= c;
        y /= c;
        z /= c;
    }


    public final void div(double n) {
        this.x /= n;
        this.y /= n;
        this.z /= n;
    }

    @Override
    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public static float lenghtTwoVectors(final Vector3f v1, final Vector3f v2){
        return (float) Math.sqrt((v1.x-v2.x) * (v1.x-v2.x) + (v1.y-v2.y) * (v1.y-v2.y) + (v1.z-v2.z) * (v1.z-v2.z));
    }

    @Override
    public Vector3f normal() {
        final float length = this.length();
        if (length < EPS) {
            throw new ArithmeticException("Normalization of a zero vector is not allowed.");
        }
        return this.divide(length);
    }

    public final void normalize() {
        float var1 = (float)(1.0 / Math.sqrt((double)(this.x * this.x + this.y * this.y + this.z * this.z)));
        this.x *= var1;
        this.y *= var1;
        this.z *= var1;
    }

    public static float dotProduct(final Vector3f v1, final Vector3f v2) {
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
    }

    public float dot(final Vector3f v){
        return this.x * v.x + this.y * v.y + this.z * v.z;
    }

    public static Vector3f crossProduct(final Vector3f v1, final Vector3f v2) {
        final float x = v1.y * v2.z - v1.z * v2.y;
        final float y = v1.z * v2.x - v1.x * v2.z;
        final float z = v1.x * v2.y - v1.y * v2.x;
        return new Vector3f(x, y, z);
    }// 0 сонапрв

    public final void cross(Vector3f var1, Vector3f var2) {
        float var3 = var1.y * var2.z - var1.z * var2.y;
        float var4 = var2.x * var1.z - var2.z * var1.x;
        this.z = var1.x * var2.y - var1.y * var2.x;
        this.x = var3;
        this.y = var4;
    }

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Vector3f multiplication(Vector3f vector, float a) {
        float resX = vector.getX() * a;
        float resY = vector.getY() * a;
        float resZ = vector.getZ() * a;
        return new Vector3f(resX, resY, resZ);
    }

    public boolean equals(Vector3f other) {
        // todo: желательно, чтобы это была глобальная константа
        final float eps = 1e-7f;
        return Math.abs(x - other.x) < eps && Math.abs(y - other.y) < eps && Math.abs(z - other.z) < eps;
    }

    @Override
    public Vector3f clone()  {
        try {
            return (Vector3f) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
