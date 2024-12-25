package utils;

import java.util.Random;

public class GetRandom {
    public static Integer getRandom(int len) {
        Integer ans = 0;
        Random r = new Random();
        for (int i = 0; i < len; i++) {
            ans = (Integer) ((ans << 1) + r.nextInt(2));
        }
        return ans;
    }
}
