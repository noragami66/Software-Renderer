package com.cgvsu.math.matrix;

import com.cgvsu.math.vector.Vector4f;

import static com.cgvsu.math.Global.EPS;

public class Matrix4f {
    public float getMatrix(int i, int j) {
        return this.matrix[i][j];
    }
    public Matrix4f(float[][] matrix){
        if (matrix.length != SIZE || matrix[0].length != SIZE) {
            throw new IllegalArgumentException("Matrix must be 4x4");
        }
        this.matrix = matrix;
    }


    public Matrix4f() {
        this.matrix = new float[SIZE][SIZE];
    }
    public Matrix4f(float num) {
        this.matrix = new float[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            this.matrix[i][i] = num;
        }
    }
    private float[][] matrix;
    static final private int SIZE = 4;


    public float[][] getMatrix() {
        return this.matrix;
    }



    public static Matrix4f add(final Matrix4f first, final Matrix4f second){
        Matrix4f result = new Matrix4f(new float[SIZE][SIZE]);
        for(int row = 0; row<SIZE; row++){
            for(int col = 0; col<SIZE; col++){
                result.matrix[row][col] = first.matrix[row][col] + second.matrix[row][col];
            }
        }
        return result;
    }

    public static Matrix4f sub(final Matrix4f first, final Matrix4f second){
        Matrix4f result = new Matrix4f(new float[SIZE][SIZE]);
        for(int row = 0; row<SIZE; row++){
            for(int col = 0; col<SIZE; col++){
                result.matrix[row][col] = first.matrix[row][col] - second.matrix[row][col];
            }
        }
        return result;
    }


    public static Matrix4f multiply(final Matrix4f first, final Matrix4f second){
        Matrix4f result = new Matrix4f(new float[SIZE][SIZE]);;
        for (int m1row = 0; m1row < SIZE; m1row++){
            for (int m2col = 0; m2col < SIZE; m2col++){
                float a = 0;
                for (int i = 0; i < SIZE; i++){
                    a+=first.matrix[m1row][i] * second.matrix[i][m2col];
                }
                result.matrix[m1row][m2col] = a;
            }
        }
        return result;
    }

    public static Matrix4f multiplication(Matrix4f matrix1, Matrix4f matrix2) {
        float[][] res = new float[SIZE][SIZE];
        for (int i = 0; i < res.length; i++) {
            for (int j = 0; j < res.length; j++) {
                for (int k = 0; k < SIZE; k++) {
                    res[i][j] += matrix1.getMatrix(i, k) * matrix2.getMatrix(k, j);
                }
            }
        }
        return new Matrix4f(res);
    }

    public Vector4f mulVector(final Vector4f v){
        float[] arr = new float[]{v.x, v.y, v.z, v.w};
        float[] res = new float[SIZE];
        for (int row = 0; row < SIZE; row++){
            for (int col = 0; col<SIZE; col++){
                res[row] += this.matrix[row][col]*arr[col];
            }
        }
        return new Vector4f(res[0], res[1], res[2], res[3]);
    }

    public void transpose(){
        for(int row = 0; row<SIZE; row++) {
            for (int col = row + 1; col < SIZE; col++) {
                float a = this.matrix[row][col];//swap
                this.matrix[row][col] = this.matrix[col][row];
                this.matrix[col][row] = a;
            }
        }
    }

    public float determinant() {
        float det = 0;
        for (int i = 0; i < SIZE; i++) {
            float[][] minor = getMinor(0, i);
            Matrix3f minorMatrix = new Matrix3f(minor);
            det += (float) (Math.pow(-1, i) * matrix[0][i] * minorMatrix.determinant());
        }
        return det;
    }

    private float[][] getMinor(int row, int col) {
        float[][] minor = new float[3][3];
        int minorRow = 0;
        for (int i = 0; i < SIZE; i++) {
            if (i == row) continue;
            int minorCol = 0;
            for (int j = 0; j < SIZE; j++) {
                if (j == col) continue;
                minor[minorRow][minorCol] = matrix[i][j];
                minorCol++;
            }
            minorRow++;
        }
        return minor;
    }

    public boolean equals(final Matrix4f other){
        for(int row = 0; row<SIZE; row++){
            for(int col = 0; col<SIZE; col++){
                if (Math.abs(this.matrix[row][col] - other.matrix[row][col]) >= EPS){
                    return false;
                }
            }
        }
        return true;
    }

    public static Matrix4f unitMatrix() {
        float[][] res = new float[4][4];
        for (int i = 0; i < res.length; i++) {
            for (int j = 0; j < res.length; j++) {
                if (i == j) {
                    res[i][j] = 1;
                } else {
                    res[i][j] = 0;
                }
            }
        }
        return new Matrix4f(res);
    }
}