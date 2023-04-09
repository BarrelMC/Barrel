package org.barrelmc.barrel.utils;

import com.github.steveice10.mc.protocol.data.game.chunk.BitStorage;
import com.github.steveice10.mc.protocol.data.game.chunk.DataPalette;
import com.github.steveice10.mc.protocol.data.game.chunk.palette.SingletonPalette;

import java.security.SignatureException;

public class Utils {

    public static byte[] toByteArray(long value) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (int) (value & 0xFFL);
            value >>= 8L;
        }

        return result;
    }

    public static String lengthCutter(String bedrockName, int length) {
        if (bedrockName == null) {
            return "null";
        }

        if (bedrockName.length() > length) {
            return bedrockName.substring(0, length);
        } else {
            return bedrockName;
        }
    }

    public static void fillPalette(DataPalette dataPalette) {
        fillPalette(dataPalette, 0);
    }

    public static void fillPalette(DataPalette dataPalette, int state) {
        BitStorage bitStorage = dataPalette.getStorage();
        dataPalette.setPalette(new SingletonPalette(0));
        for (int i = 0; i < bitStorage.getSize(); i++) {
            bitStorage.set(i, state);
        }
    }

    public static byte[] DERToJOSE(byte[] derSignature, Utils.AlgorithmType algorithmType) throws SignatureException {
        // DER Structure: http://crypto.stackexchange.com/a/1797
        boolean derEncoded = derSignature[0] == 0x30 && derSignature.length != algorithmType.ecNumberSize * 2;
        if (!derEncoded) {
            throw new SignatureException("Invalid DER signature format.");
        }

        final byte[] joseSignature = new byte[algorithmType.ecNumberSize * 2];

        //Skip 0x30
        int offset = 1;
        if (derSignature[1] == (byte) 0x81) {
            //Skip sign
            offset++;
        }

        //Convert to unsigned. Should match DER length - offset
        int encodedLength = derSignature[offset++] & 0xff;
        if (encodedLength != derSignature.length - offset) {
            throw new SignatureException("Invalid DER signature format.");
        }

        //Skip 0x02
        offset++;

        //Obtain R number length (Includes padding) and skip it
        int rLength = derSignature[offset++];
        if (rLength > algorithmType.ecNumberSize + 1) {
            throw new SignatureException("Invalid DER signature format.");
        }
        int rPadding = algorithmType.ecNumberSize - rLength;
        //Retrieve R number
        System.arraycopy(derSignature, offset + Math.max(-rPadding, 0), joseSignature, Math.max(rPadding, 0), rLength + Math.min(rPadding, 0));

        //Skip R number and 0x02
        offset += rLength + 1;

        //Obtain S number length. (Includes padding)
        int sLength = derSignature[offset++];
        if (sLength > algorithmType.ecNumberSize + 1) {
            throw new SignatureException("Invalid DER signature format.");
        }
        int sPadding = algorithmType.ecNumberSize - sLength;
        //Retrieve R number
        System.arraycopy(derSignature, offset + Math.max(-sPadding, 0), joseSignature, algorithmType.ecNumberSize + Math.max(sPadding, 0), sLength + Math.min(sPadding, 0));

        return joseSignature;
    }

    public enum AlgorithmType {
        ECDSA256(32), ECDSA384(48);

        public int ecNumberSize;

        AlgorithmType(int ecNumberSize) {
            this.ecNumberSize = ecNumberSize;
        }
    }
}
