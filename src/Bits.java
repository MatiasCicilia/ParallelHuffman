import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Bruno on 20/2/2017.
 */
public class Bits {
    private int value;
    private byte length;

    public Bits add(boolean bit) {
        value = (value << 1) | (bit ? 1 : 0);
        length++;
        return this;
    }

    public int getValue() { return value; }

    public int getLength() { return length; }

    public Bits copy() {
        final Bits bits = new Bits();
        bits.value = value;
        bits.length = length;
        return bits;
    }

    @Override public String toString() {
        final StringBuilder builder = new StringBuilder();
        int aux = 1 << length;
        while(aux > 1) {
            aux = aux >> 1;
            builder.append((value & aux) == 0 ? "0":"1");
        }
        return builder.toString();
    }

    public void writeInto(OutputStream stream) throws IOException {
        stream.write(length);
        final BitsOutputStream output = new BitsOutputStream();
        output.write(this);
        stream.write(output.toByteArray());
    }
}
