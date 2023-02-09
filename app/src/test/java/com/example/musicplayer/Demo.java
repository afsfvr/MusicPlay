package com.example.musicplayer;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

/**
 * @Date: 2023/1/13 15:22
 * @Description:
 * @Author: wuwenzong
 */
public class Demo {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Test
    public void main() {
    }

    public String a(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2) {
        return new String(b(paramArrayOfByte1, paramArrayOfByte2), StandardCharsets.UTF_8);
    }

    private byte[] b(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2) {
        int i = paramArrayOfByte1.length;
        int j = paramArrayOfByte2.length;
        int k = 0;
        int n;
        for (int m = 0; k < i; m = n + 1) {
            n = m;
            if (m >= j) {
                n = 0;
            }
            paramArrayOfByte1[k] = ((byte) (byte) (paramArrayOfByte1[k] ^ paramArrayOfByte2[n]));
            k++;
        }
        return paramArrayOfByte1;
    }
}