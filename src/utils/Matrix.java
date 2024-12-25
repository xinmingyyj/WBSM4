package utils;

import java.util.*;

class Filed{
    int add(int a, int b){

        boolean precondition = (a == 0 || a == 1) && (b == 0 || b == 1);
        if(!precondition){
            throw new AssertionError("the argument a and b must equal 0 or 1!");
        }

        return a ^ b;
    }

    int multi(int a, int b){
        boolean precondition = (a == 0 || a == 1) && (b == 0 || b == 1);
        if(!precondition){
            throw new AssertionError("the argument a and b must equal 0 or 1!");
        }
        return a & b;
    }

    int addinv(int a){
        boolean precondition = (a == 0 || a == 1);
        if(!precondition){
            throw new AssertionError("the argument a must equal 0 or 1!");
        }
        return a;
    }

    int mulinv(int a){
        boolean precondition = (a == 1);
        if(!precondition){
            throw new AssertionError("the argument a must equal  1!");
        }
        return a;
    }
}

public class Matrix {
    int n;
    int[][] matrix;
    Filed filed;

    Matrix(int n) {
        this.n = n;
        this.matrix = new int[n][n];
        this.filed = new Filed();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = 0;
            }
        }
    }

    public Matrix inv() {

        boolean precondition = (this.getRank() == this.n);
        if (!precondition) {
            throw new AssertionError("matrix is not invertable!");
        }

        Matrix invM = new Matrix(n);
        int N = n;
        int i, j, k;
        int[][] augM = new int[N][2 * N];
        int t1, t2, t3;

        for (i = 0; i < N; i++) {
            for (j = 0; j < 2 * N; j++) {
                if (j < N) {
                    augM[i][j] = matrix[i][j];
                } else {
                    augM[i][j] = (j - N == i ? 1 : 0);
                }
            }
        }

        for (i = 0; i < N; i++) {
            if (augM[i][i] == 0) {
                for (j = i + 1; j < N; j++) {
                    if (augM[j][i] != 0) break;
                }
                if (j == N) {
                    throw new AssertionError("matrix is not invertable!");
                }
                for (k = 0; k < 2 * N; k++) {
                    augM[i][k] = filed.add(augM[i][k], augM[j][k]);
                }
            }

            t1 = augM[i][i];
            for (j = 0; j < 2 * N; j++) {
                augM[i][j] = filed.multi(augM[i][j], filed.mulinv(t1));
            }

            for (j = i + 1; j < N; j++) {
                t2 = augM[j][i];
                for (k = i; k < 2 * N; k++) {
                    augM[j][k] = filed.add(augM[j][k], filed.addinv(filed.multi(t2, augM[i][k])));
                }
            }
        }

        for (i = N - 1; i >= 0; i--) {
            for (j = i - 1; j >= 0; j--) {
                t3 = augM[j][i];
                for (k = i; k < 2 * N; k++) {
                    augM[j][k] = filed.add(augM[j][k], filed.addinv(filed.multi(t3, augM[i][k])));
                }
            }
        }

        for (i = 0; i < N; i++) {
            for (j = N; j < 2 * N; j++) {
                invM.matrix[i][j - N] = augM[i][j];
            }
        }

        return invM;
    }

    int getRank() {
        int n = this.n;
        int m = this.n;
        int[][] matrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = this.matrix[i][j];
            }
        }
        int r = 0, d = 0;
        int i, j, k;
        for (i = 0; i < m; i++) {
            k = d;

            for (j = d + 1; j < n; j++) {
                if (Math.abs(matrix[k][i]) < Math.abs(matrix[j][i])) {
                    k = j;
                }
            }

            if (k != d) {
                for (j = i; j < m; j++) {
                    int temp = matrix[d][j];
                    matrix[d][j] = matrix[k][j];
                    matrix[k][j] = temp;
                }
            }
            if (matrix[d][i] == 0) {
                continue;
            } else {
                r++;
                for (j = 0; j < n; j++) {
                    if (j != d) {
                        int temp = filed.multi(matrix[j][i], filed.mulinv(matrix[d][i]));
                        for (k = i; k < m; k++) {
                            matrix[j][k] = filed.add(matrix[j][k], filed.multi(temp, matrix[d][k]));
                        }
                    }
                }
                int temp = matrix[d][i];
                for (j = i; j < m; j++) {
                    matrix[d][j] = filed.multi(matrix[d][j], filed.mulinv(temp));
                }
                d = d + 1;
                if (d >= n) {
                    break;
                }
            }
        }
        return r;
    }

    public static void swap(int[] array, int i, int j) {
        if (array == null || i < 0 || j < 0 || i >= array.length || j >= array.length || i == j) {
            return;
        }
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }


    public Matrix multi(Matrix b) {
        Matrix ans = new Matrix(this.n);
        int n = this.n;
        for (int i = 0; i < n; i++) {
            for (int u = 0; u < n; u++) {
                for (int j = 0; j < n; j++) {
                    ans.matrix[i][u] = filed.add(ans.matrix[i][u], filed.multi(this.matrix[i][j], b.matrix[j][u]));
                }
            }
        }
        return ans;
    }

    public Matrix add(Matrix b) {
        Matrix ans = new Matrix(this.n);
        int n = this.n;
        for (int i = 0; i < n; i++) {
            for (int u = 0; u < n; u++) {
                ans.matrix[i][u] = filed.add(this.matrix[i][u], b.matrix[i][u]);
            }
        }
        return ans;
    }

    public int[] multiVec(int[] vec) {
        int n = this.n;
        int[] ans = new int[n];
        for (int i = 0; i < n; i++) {
            int sum = 0;
            for (int j = 0; j < n; j++) {
                sum = filed.add(sum, filed.multi(this.matrix[i][j], vec[j]));
            }
            ans[i] = sum;
        }
        return ans;
    }

    void trasnpose() {
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                int tmp = matrix[j][i];
                matrix[i][j] = matrix[j][i];
                matrix[j][i] = tmp;
            }
        }
    }

    boolean equal(Matrix b) {
        int n = b.n;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (this.matrix[i][j] != b.matrix[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    boolean isZero() {
        Matrix zero = new Matrix(this.n);
        return equal(zero);
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                s += (matrix[i][j] + ", ");
            }
            s += "\n";
        }
        return s;

    }

    public static Matrix getRandomInvertibleMatrix(int n) {

        int runs = 1000;
        Matrix mat = new Matrix(n);

        int[][] data = mat.matrix;
        for (int i = 0; i < n; i++) {
            data[i][i] = 1;
        }

        Random r = new Random();
        Filed filed = new Filed();

        for (int run = 0; run < runs; run++) {
            int type = r.nextInt(2);
            int row1 = r.nextInt(n);
            int row2 = r.nextInt(n);
            if (type == 0) {
                int[] tmp = data[row1];
                data[row1] = data[row2];
                data[row2] = tmp;
            } else {
                if (row1 != row2) {
                    for (int i = 0; i < n; i++) {
                        data[row2][i] = filed.add(data[row1][i], data[row2][i]);
                    }
                }
            }
        }

        return mat;
    }

    public static int[] int2vec(int x, int n) {

        boolean precondition = (n == 8 || n == 32);
        if (!precondition) {
            throw new AssertionError("n == 8 || n == 32!");
        }

        int[] vec = new int[n];
        for (int i = n - 1; i >= 0; i--) {
            vec[i] = x & 1;
            x >>>= 1;
        }
        return vec;
    }

    public static int vec2int(int[] vec, int n) {
        boolean precondition = (n == 8 || n == 32);
        if (!precondition) {
            throw new AssertionError("n == 8 || n == 32!");
        }
        int ans = 0;
        int tmp = 1;
        for (int i = n - 1; i >= 0; i--) {
            if (vec[i] == 1) {
                ans += tmp;
            }
            tmp <<= 1;
        }
        return ans;
    }

    public static Matrix array2Matrix(int[] array, int n) {
        Matrix mat = new Matrix(n);
        for (int i = 0; i < n; i++) {
            int[] vec = int2vec(array[i], n);
            for (int j = 0; j < n; j++) {
                mat.matrix[j][i] = vec[j];
            }
        }
        return mat;
    }

    public static Matrix I(int n) {
        Matrix I = new Matrix(n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    I.matrix[i][j] = 1;
                } else {
                    I.matrix[i][j] = 0;
                }
            }
        }
        return I;
    }

    public static Matrix concat8To32Matrix(Matrix[] matrixArray) {
        boolean precondition = (matrixArray.length == 4 && matrixArray[0].n == 8 && matrixArray[1].n == 8 && matrixArray[2].n == 8 && matrixArray[3].n == 8);
        if (!precondition) {
            throw new AssertionError("matrix size should be 8");
        }
        Matrix mat = new Matrix(32);
        for (int idx = 0; idx < 4; idx++) {
            int startRow = idx * 8;
            int startCol = idx * 8;
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    mat.matrix[startRow + i][startCol + j] = matrixArray[idx].matrix[i][j];
                }
            }
        }
        return mat;
    }

    public static Matrix[][] spiltMatrix32to8(Matrix mat) {
        Matrix[][] sub = new Matrix[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                sub[i][j] = new Matrix(8);
            }
        }

        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                sub[i / 8][j / 8].matrix[i % 8][j % 8] = mat.matrix[i][j];
            }
        }
        return sub;
    }
}