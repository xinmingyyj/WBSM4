package utils;

public class GenL {
    public static int rotateLeft(int num, int shift) {

        shift &= 31;

        return (num << shift) | (num >>> (32 - shift));
    }

    public static int[] L(int[] vec) {
        int x = vec2int(vec);
        x = x ^ rotateLeft(x, 2) ^ rotateLeft(x, 10) ^ rotateLeft(x, 18) ^ rotateLeft(x, 24);
        return int2vec(x);
    }

    public static int vec2int(int[] vec) {
        int ans = 0;
        int tmp = 1;
        for (int i = 31; i >=0; i--) {
            if (vec[i] == 1) {
                ans += tmp;
            }
            tmp <<= 1;
        }
        return ans;
    }

    public static int[] int2vec(int x) {
        int[] vec = new int[32];
        for (int i = 31; i>=0; i--) {
            vec[i] = x & 1;
            x >>>= 1;
        }
        return vec;
    }

    public static Matrix genL() {
        Matrix l = new Matrix(32);
        for(int i = 0; i < 32;i++){
            int vec = 1 << (31 - i);
            int[] ans = L(int2vec(vec));

            for(int j = 0; j < 32;j++){
                l.matrix[j][i] = ans[j];
            }
        }
        return l;
    }

    public static Matrix[][] genLij(){
        return Matrix.spiltMatrix32to8(genL());
    }

}

