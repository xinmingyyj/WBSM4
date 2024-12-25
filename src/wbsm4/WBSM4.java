package wbsm4;

import utils.GetRandom;
import utils.Matrix;

import static utils.GenL.genL;

public class WBSM4 {
    private Matrix[] P;               //P[36]
    private Matrix[] P_inv;           //P_inv[36]
    private Matrix[][] Eij;           //Eij[33][4]
    private Matrix[][] Eij_inv;       //Eij_inv[33][4]
    private Matrix[] Ei_inv;          //Ei_inv[33]
    private Matrix[] Q;               //Q[33]
    private Matrix[] Q_inv;           //Q_inv[33]

    private int[] roundKey;           //roundKey

    private int[] p;                  //p[36]
    private int[] p_prime;            //p_prime[36]
    private int[] q;                  //q[33]
    private int[] e;                  //e[33];

    public Matrix[] B;               //B[33]
    public int[] b;                  //b[33]
    public Matrix[] C;               //C[33]
    public int[] c;                  //c[33]
    public Matrix[][] D;             //D[33][3]
    public int[][] d;                //d[33][3]
    public int[][][] Table;          //Table[33][4][256]

    public WBSM4(int[] key){
        // generate P P_inv
        this.P = new Matrix[36];
        this.P_inv = new Matrix[36];
        for(int i = 0; i < 36; i++) {
            this.P[i] = Matrix.getRandomInvertibleMatrix(32);
            this.P_inv[i] = P[i].inv();
        }
        // generate Eij Eij_inv
        this.Eij = new Matrix[33][4];
        this.Eij_inv = new Matrix[33][4];
        for(int i = 0; i < 33; i++){
            for(int j = 0; j < 4; j++){
                this.Eij[i][j] = Matrix.getRandomInvertibleMatrix(8);
                this.Eij_inv[i][j] = this.Eij[i][j].inv();
            }
        }
        // generate Ei_inv
        this.Ei_inv = new Matrix[33];
        for(int i = 0; i < 33; i++){
            this.Ei_inv[i] = Matrix.concat8To32Matrix(Eij_inv[i]);
        }

        //generate Q Q_inv
        this.Q = new Matrix[33];
        this.Q_inv = new Matrix[33];
        for(int i = 0; i < 33; i++){
            this.Q[i] = Matrix.getRandomInvertibleMatrix(32);
            this.Q_inv[i] = Q[i].inv();
        }

        // generate round key
        this.roundKey = KeyExpand.keyExpand(key);

        // generate p
        this.p = new int[36];
        for(int i = 0; i < 36; i++){
            this.p[i] = GetRandom.getRandom(32);
        }

        // generate p_prime
        this.p_prime = new int[36];
        for(int i = 0; i < 36; i++){
            this.p_prime[i] = GetRandom.getRandom(32);
        }

        // generate q
        this.q = new int[33];
        for(int i = 0; i < 33; i++){
            this.q[i] = GetRandom.getRandom(32);
        }

        // generate e
        this.e = new int[33];
        for(int i = 0; i < 33; i++){
            this.e[i] = GetRandom.getRandom(32);
        }

        // generate B
        this.B = new Matrix[33];
        for(int r = 1; r < 33; r++){
            this.B[r] = P[r+3].multi(Q[r].inv());
        }

        // generate b
        this.b = new int[33];
        for(int r = 1; r < 33; r++){
            this.b[r] = Matrix.vec2int(this.P[r+3].multi(this.Q[r].inv()).multiVec(Matrix.int2vec(this.q[r],
                    32)),32) ^ this.p[r+3] ^ this.p_prime[r+3];
        }

        // generate C
        this.C = new Matrix[33];
        for(int r = 1; r < 33; r++){
            this.C[r] = this.P[r+3].multi(P[r-1].inv());
        }

        // generate c
        this.c = new int[33];
        for(int r = 1; r < 33; r++){
            this.c[r] = Matrix.vec2int(this.P[r+3].multi(this.P[r-1].inv()).multiVec(Matrix.int2vec(this.p[r-1],32)),
                    32) ^ this.p_prime[r+3];
        }

        // generate D
        this.D = new Matrix[33][3];
        for(int r = 1; r < 33; r++){
            for(int j = 0; j < 3; j++){
                this.D[r][j] = this.Ei_inv[r].inv().multi(this.P[r + j].inv());
            }
        }

        // generate d
        this.d = new int[33][3];
        for(int r = 1; r < 33; r++){
            for(int j = 0; j < 3; j++){
                this.d[r][j] = Matrix.vec2int(
                        this.Ei_inv[r].inv().multi(this.P_inv[r + j]).multiVec(Matrix.int2vec(this.p[r + j],32)),32
                ) ^ this.e[r];
            }
        }

        this.Table = new int[33][4][256];
        Matrix L = genL();
        for(int r = 1; r < 33; r++) {
            Matrix[][] R = Matrix.spiltMatrix32to8(this.Q[r].multi(L));
            for(int i = 0; i < 4; i++){
                int qij = 0;
                if(i == 3) qij = this.q[r];
                int Kij = (this.roundKey[r-1] >>> (24 - i * 8)) & 0xff;

                for(int x = 0; x < 256; x++){
                    int tmp = Sbox.sbox[Matrix.vec2int(Eij_inv[r][i].multiVec(
                            Matrix.int2vec(x ^ ((this.e[r] >>> (24 - i * 8))) & 0xff  , 8)
                    ),8) ^ Kij];

                    Matrix[] Ri = new Matrix[]{R[0][i],R[1][i],R[2][i],R[3][i]};

                    int[] t = new int[4];
                    for(int ii = 0; ii < 4;ii++){
                        t[ii] = Matrix.vec2int(Ri[ii].multiVec(Matrix.int2vec(tmp,8)),8);

                    }
                    this.Table[r][i][x] = ((t[0] << 24) | (t[1] << 16) | (t[2] << 8) | (t[3])) ^ qij;
                }
            }
        }


    }

    // external input encoding
    public int[] externalEncoding(int[] X){
        for(int i = 0; i < 4; i++){
            X[i] = Matrix.vec2int(
                    this.P[i].multiVec(Matrix.int2vec(X[i],32)),32
            ) ^ this.p[i];
        }
        return X;
    }

    // output decoding
    public int[] decoding(int[] X_prime){
        for(int i = 32; i < 36; i++){
            X_prime[i - 32] = Matrix.vec2int(
                    this.P_inv[i].multiVec(Matrix.int2vec(X_prime[i - 32] ^ this.p[i], 32)),32
            );
        }
        return new int[]{X_prime[3],X_prime[2],X_prime[1],X_prime[0]};
    }

    // encrypt
    public int[] encrypt(int[] X){
        int[] X_prime = new int[36];
        for(int i = 0; i < 4;i++) X_prime[i] = X[i];
        for(int r = 1; r <= 32; r++){
            int yr = 0;
            for(int j = 0; j <= 2; j++){
                yr ^= Matrix.vec2int(this.D[r][j].multiVec(Matrix.int2vec(X_prime[r+j] ,32)),32) ^ this.d[r][j];
            }
            int zri = Table[r][0][(yr >>> 24) & 0xff] ^ Table[r][1][(yr >>> 16) & 0xff] ^
                    Table[r][2][(yr >>> 8) & 0xff] ^ Table[r][3][yr & 0xff];

            X_prime[r+3] = Matrix.vec2int(this.B[r].multiVec(Matrix.int2vec(zri,32)),32) ^ this.b[r]
                    ^ Matrix.vec2int(this.C[r].multiVec(Matrix.int2vec(X_prime[r-1],32)),32) ^ this.c[r];

        }
        return new int[]{X_prime[32],X_prime[33],X_prime[34],X_prime[35]};
    }


    public static void main(String[] args) {
        int[] key = new int[]{0x01234567,0x89abcdef,0xfedcba98,0x76543210};
        int[] plain = new int[]{0x01234567,0x89abcdef,0xfedcba98,0x76543210};
        int[] trueCipher = new int[]{0x681EDF34,0xD206965E,0x86B3E94F,0x536E4246};

        WBSM4 wbsm4 = new WBSM4(key);

        int[] cipher = wbsm4.decoding(wbsm4.encrypt(wbsm4.externalEncoding(plain)));

        for(int i = 0; i < 4;i++){
            System.out.println(String.format("0x%X", cipher[i]));
            System.out.println(cipher[i] == trueCipher[i]);
        }
    }
}

