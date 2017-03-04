import java.io.ByteArrayOutputStream;

/**
 * Created by Bruno on 20/2/2017.
 */
public class BitsOutputStream {
    private final ByteArrayOutputStream output;
    private int rest = BYTE_SIZE;
    private byte current = 0;

    /** Output stream for Bits constructor. */
    public BitsOutputStream() { output = new ByteArrayOutputStream(1024*1024); }

    /** Write given bits into output. */
    public void write(Bits bits) {
        final int value = bits.getValue();
        int n = bits.getLength();
        while (n >= rest) {
            n -= rest;
            final int write = current | (value >> n);
            output.write(write);
            current = 0;
            rest = BYTE_SIZE;
        }
        if (n > 0) {
            rest -= n;
            current = (byte) (current | value << rest);
        }
    }

    /** Returns a new byte array. */
    public byte[] toByteArray() {
        if (rest < BYTE_SIZE) output.write(current);
        return output.toByteArray();
    }

    private static final int BYTE_SIZE = 8;
}
