package com.cgvsu.math.matrix;

import com.cgvsu.math.vector.Vector2f;
import com.cgvsu.math.vector.Vector3f;

import static com.cgvsu.math.Global.EPS;

public class Matrix3f {
    private float[][] matrix;
    private static final int SIZE = 3;

    public Matrix3f(final float[][] matrix) {
        if (matrix.length != SIZE || matrix[0].length != SIZE) {
            throw new IllegalArgumentException("Матрица должна быть 3 на 3.");
        }
        this.matrix = matrix;
    }
    public Matrix3f() {this.matrix = new float[SIZE][SIZE];}
    //Трехмерная матрица с определенным элементом на главной диагонали
    public Matrix3f(float num) {
        this.matrix = new float[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            this.matrix[i][i] = num;
        }
    }

    public static Matrix3f add(final Matrix3f first, final Matrix3f second) {
        Matrix3f result = new Matrix3f(new float[SIZE][SIZE]);
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                result.matrix[row][col] = first.matrix[row][col] + second.matrix[row][col];
            }
        }
        return result;
    }

    public static Matrix3f sub(final Matrix3f first, final Matrix3f second) {
        Matrix3f result = new Matrix3f(new float[SIZE][SIZE]);
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                result.matrix[row][col] = first.matrix[row][col] - second.matrix[row][col];
            }
        }
        return result;
    }

    public static Matrix3f multiply(final Matrix3f first, final Matrix3f second) {
        Matrix3f result = new Matrix3f(new float[SIZE][SIZE]);
        for (int firstRow = 0; firstRow < SIZE; firstRow++) {
            for (int secondCol = 0; secondCol < SIZE; secondCol++) {
                float n = 0;
                for (int i = 0; i < SIZE; i++) {
                    n += first.matrix[firstRow][i] * second.matrix[i][secondCol];
                }
                result.matrix[firstRow][secondCol] = n;
            }
        }
        return result;
    }

    //Умножение на вектор
    public Vector3f mulVector(final Vector3f v) {
        float[] arr = new float[]{v.getX(), v.getY(), v.getZ()};
        float[] res = new float[SIZE];
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                res[row] += this.matrix[row][col] * arr[col];
            }
        }
        return new Vector3f(res[0], res[1], res[2]);
    }

    public void transpose() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = row + 1; col < SIZE; col++) {
                float a = this.matrix[row][col];
                this.matrix[row][col] = this.matrix[col][row];
                this.matrix[col][row] = a;
            }
        }
    }

    public float determinant() {
        return matrix[0][0] * (matrix[1][1] * matrix[2][2] - matrix[1][2] * matrix[2][1])
                - matrix[0][1] * (matrix[1][0] * matrix[2][2] - matrix[1][2] * matrix[2][0])
                + matrix[0][2] * (matrix[1][0] * matrix[2][1] - matrix[1][1] * matrix[2][0]);
    }

    public boolean equals(final Matrix3f other) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (Math.abs(this.matrix[row][col] - other.matrix[row][col]) >= EPS) {
                    return false;
                }
            }
        }
        return true;
    }
}
