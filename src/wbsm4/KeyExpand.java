package wbsm4;

public class KeyExpand {

    private static int shift(int input, int n) {
        return (input >>> (32 - n)) | (input << n);
    }

    private static byte[] splitInt(int n) {
        return new byte[]{(byte) (n >>> 24), (byte) (n >>> 16), (byte) (n >>> 8), (byte) n};
    }


    private static int jointBytes(byte byte_0, byte byte_1, byte byte_2, byte byte_3) {
        return ((byte_0 & 0xFF) << 24) | ((byte_1 & 0xFF) << 16) | ((byte_2 & 0xFF) << 8) | (byte_3 & 0xFF);
    }

    public static int applySbox(int in) {
        byte[] temp = splitInt(in);
        byte[] output = new byte[4];

        for (int i = 0; i < 4; i++) {
            output[i] = (byte)(int)Sbox.sbox[temp[i] & 0xFF];
        }
        return jointBytes(output[0], output[1], output[2], output[3]);
    }



    public static int[] keyExpand(int[] key){
        int[] keyR = new int[32];
        int[] keyTemp = new int[key.length];
        for(int i = 0; i < key.length;i++){
            keyTemp[i] = key[i];
        }
        int boxIn, boxOut;
        final int[] FK = {0xa3b1bac6, 0x56aa3350, 0x677d9197, 0xb27022dc};
        final int[] CK = {
                0x00070e15, 0x1c232a31, 0x383f464d, 0x545b6269,
                0x70777e85, 0x8c939aa1, 0xa8afb6bd, 0xc4cbd2d9,
                0xe0e7eef5, 0xfc030a11, 0x181f262d, 0x343b4249,
                0x50575e65, 0x6c737a81, 0x888f969d, 0xa4abb2b9,
                0xc0c7ced5, 0xdce3eaf1, 0xf8ff060d, 0x141b2229,
                0x30373e45, 0x4c535a61, 0x686f767d, 0x848b9299,
                0xa0a7aeb5, 0xbcc3cad1, 0xd8dfe6ed, 0xf4fb0209,
                0x10171e25, 0x2c333a41, 0x484f565d, 0x646b7279
        };

        for (int i = 0; i < 4; i++) {
            keyTemp[i] = keyTemp[i] ^ FK[i];
        }

        for (int i = 0; i < 32; i++) {
            boxIn = keyTemp[1] ^ keyTemp[2] ^ keyTemp[3] ^ CK[i];
            boxOut = applySbox(boxIn);
            keyR[i] = keyTemp[0] ^ boxOut ^ shift(boxOut, 13) ^ shift(boxOut, 23);
            keyTemp[0] = keyTemp[1];
            keyTemp[1] = keyTemp[2];
            keyTemp[2] = keyTemp[3];
            keyTemp[3] = keyR[i];
        }
        return keyR;
    }


    public static void main(String[] args) {
        int[] key = new int[]{0x01234567,0x89abcdef,0xfedcba98,0x76543210};
        int[] expanedKey = keyExpand(key);
        for(int i = 0; i < 6;i++){
            System.out.println(String.format("0x%X",expanedKey[i]));
        }
    }
}



