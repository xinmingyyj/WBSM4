package wbsm4;

import utils.GetRandom;
import utils.Matrix;

import static utils.GenL.genL;

public class WBSM4 {
    private static final int MATRIX_SIZE_32 = 32;
    private static final int MATRIX_SIZE_8 = 8;
    private static final int ROUND_COUNT = 33;

    private Matrix[] P;               // P[36]
    private Matrix[] P_inv;           // P_inv[36]
    private Matrix[][] Eij;           // Eij[33][4]
    private Matrix[][] Eij_inv;       // Eij_inv[33][4]
    private Matrix[] Ei_inv;          // Ei_inv[33]
    private Matrix[] Q;               // Q[33]
    private Matrix[] Q_inv;           // Q_inv[33]

    private int[] roundKey;           // roundKey
    private int[] p;                  // p[36]
    private int[] p_prime;            // p_prime[36]
    private int[] q;                  // q[33]
    private int[] e;                  // e[33]

    public Matrix[] B;               // B[33]
    public int[] b;                  // b[33]
    public Matrix[] C;               // C[33]
    public int[] c;                  // c[33]
    public Matrix[][] D;             // D[33][3]
    public int[][] d;                // d[33][3]
    public int[][][] Table;          // Table[33][4][256]

    public WBSM4(int[] key) {
        // Generate matrices and values
        P = new Matrix[36];
        P_inv = new Matrix[36];
        Eij = new Matrix[ROUND_COUNT][4];
        Eij_inv = new Matrix[ROUND_COUNT][4];
        Ei_inv = new Matrix[ROUND_COUNT];
        Q = new Matrix[ROUND_COUNT];
        Q_inv = new Matrix[ROUND_COUNT];

        generateMatrices();
        roundKey = KeyExpand.keyExpand(key);

        p = generateRandomArray(36);
        p_prime = generateRandomArray(36);
        q = generateRandomArray(33);
        e = generateRandomArray(33);

        // Generate B, b, C, c, D, and d
        B = new Matrix[ROUND_COUNT];
        b = new int[ROUND_COUNT];
        C = new Matrix[ROUND_COUNT];
        c = new int[ROUND_COUNT];
        D = new Matrix[ROUND_COUNT][3];
        d = new int[ROUND_COUNT][3];

        generateBAndBPrime();
        generateCAndCPrime();
        generateDAndDPrime();

        Table = new int[ROUND_COUNT][4][256];
        generateTable();
    }

    private void generateMatrices() {
        // Generate P, P_inv, Eij, Eij_inv, Q, and Q_inv
        for (int i = 0; i < 36; i++) {
            P[i] = Matrix.getRandomInvertibleMatrix(MATRIX_SIZE_32);
            P_inv[i] = P[i].inv();
        }
        for (int i = 0; i < ROUND_COUNT; i++) {
            for (int j = 0; j < 4; j++) {
                Eij[i][j] = Matrix.getRandomInvertibleMatrix(MATRIX_SIZE_8);
                Eij_inv[i][j] = Eij[i][j].inv();
            }
            Ei_inv[i] = Matrix.concat8To32Matrix(Eij_inv[i]);
            Q[i] = Matrix.getRandomInvertibleMatrix(MATRIX_SIZE_32);
            Q_inv[i] = Q[i].inv();
        }
    }

    private void generateBAndBPrime() {
        for (int r = 1; r < ROUND_COUNT; r++) {
            B[r] = P[r + 3].multi(Q[r].inv());
            b[r] = Matrix.vec2int(P[r + 3].multi(Q[r].inv()).multiVec(Matrix.int2vec(q[r], MATRIX_SIZE_32)), MATRIX_SIZE_32)
                    ^ p[r + 3] ^ p_prime[r + 3];
        }
    }

    private void generateCAndCPrime() {
        for (int r = 1; r < ROUND_COUNT; r++) {
            C[r] = P[r + 3].multi(P[r - 1].inv());
            c[r] = Matrix.vec2int(P[r + 3].multi(P[r - 1].inv()).multiVec(Matrix.int2vec(p[r - 1], MATRIX_SIZE_32)), MATRIX_SIZE_32)
                    ^ p_prime[r + 3];
        }
    }

    private void generateDAndDPrime() {
        for (int r = 1; r < ROUND_COUNT; r++) {
            for (int j = 0; j < 3; j++) {
                D[r][j] = Ei_inv[r].inv().multi(P[r + j].inv());
                d[r][j] = Matrix.vec2int(Ei_inv[r].inv().multi(P_inv[r + j]).multiVec(Matrix.int2vec(p[r + j], MATRIX_SIZE_32)), MATRIX_SIZE_32)
                        ^ e[r];
            }
        }
    }

    private void generateTable() {
        Matrix L = genL();
        for (int r = 1; r < ROUND_COUNT; r++) {
            Matrix[][] R = Matrix.spiltMatrix32to8(Q[r].multi(L));
            for (int i = 0; i < 4; i++) {
                int qij = (i == 3) ? q[r] : 0;
                int Kij = (roundKey[r - 1] >>> (24 - i * 8)) & 0xff;
                for (int x = 0; x < 256; x++) {
                    int tmp = Sbox.sbox[Matrix.vec2int(Eij_inv[r][i].multiVec(Matrix.int2vec(x ^ ((e[r] >>> (24 - i * 8))) & 0xff, 8)), 8) ^ Kij];
                    Matrix[] Ri = new Matrix[]{R[0][i], R[1][i], R[2][i], R[3][i]};

                    int[] t = new int[4];
                    for (int ii = 0; ii < 4; ii++) {
                        t[ii] = Matrix.vec2int(Ri[ii].multiVec(Matrix.int2vec(tmp, 8)), 8);
                    }
                    Table[r][i][x] = ((t[0] << 24) | (t[1] << 16) | (t[2] << 8) | (t[3])) ^ qij;
                }
            }
        }
    }

    private int[] generateRandomArray(int size) {
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = GetRandom.getRandom(MATRIX_SIZE_32);
        }
        return array;
    }

    public int[] externalEncoding(int[] X) {
        for (int i = 0; i < 4; i++) {
            X[i] = Matrix.vec2int(P[i].multiVec(Matrix.int2vec(X[i], MATRIX_SIZE_32)), MATRIX_SIZE_32) ^ p[i];
        }
        return X;
    }

    public int[] decoding(int[] X_prime) {
        for (int i = 32; i < 36; i++) {
            X_prime[i - 32] = Matrix.vec2int(P_inv[i].multiVec(Matrix.int2vec(X_prime[i - 32] ^ p[i], MATRIX_SIZE_32)), MATRIX_SIZE_32);
        }
        return new int[]{X_prime[3], X_prime[2], X_prime[1], X_prime[0]};
    }

    public int[] encrypt(int[] X) {
        int[] X_prime = new int[36];
        System.arraycopy(X, 0, X_prime, 0, 4);
        for (int r = 1; r <= 32; r++) {
            int yr = 0;
            for (int j = 0; j <= 2; j++) {
                yr ^= Matrix.vec2int(D[r][j].multiVec(Matrix.int2vec(X_prime[r + j], MATRIX_SIZE_32)), MATRIX_SIZE_32) ^ d[r][j];
            }
            int zri = Table[r][0][(yr >>> 24) & 0xff] ^ Table[r][1][(yr >>> 16) & 0xff]
                    ^ Table[r][2][(yr >>> 8) & 0xff] ^ Table[r][3][yr & 0xff];

            X_prime[r + 3] = Matrix.vec2int(B[r].multiVec(Matrix.int2vec(zri, MATRIX_SIZE_32)), MATRIX_SIZE_32)
                    ^ b[r] ^ Matrix.vec2int(C[r].multiVec(Matrix.int2vec(X_prime[r - 1], MATRIX_SIZE_32)), MATRIX_SIZE_32) ^ c[r];
        }
        return new int[]{X_prime[32], X_prime[33], X_prime[34], X_prime[35]};
    }

    public static void main(String[] args) {
        int[] key = new int[]{0x01234567, 0x89abcdef, 0xfedcba98, 0x76543210};
        int[] plain = new int[]{0x01234567, 0x89abcdef, 0xfedcba98, 0x76543210};
        int[] trueCipher = new int[]{0x681EDF34, 0xD206965E, 0x86B3E94F, 0x536E4246};

        WBSM4 wbsm4 = new WBSM4(key);
        int[] cipher = wbsm4.decoding(wbsm4.encrypt(wbsm4.externalEncoding(plain)));

        for (int i = 0; i < 4; i++) {
            System.out.println(String.format("0x%X", cipher[i]));
            System.out.println(cipher[i] == trueCipher[i]);
        }
    }
}
