package anaydis.compression;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by matias on 11/15/16.
 */
public abstract class AbstractCompressorTest {
    private static final String ISHMAEL = "Call me Ishmael.  Some years ago--never mind how long" +
            "precisely--having little or no money in my purse, and nothing" +
            "particular to interest me on shore, I thought I would sail about a" +
            "little and see the watery part of the world.  It is a way I have of" +
            "driving off the spleen and regulating the circulation.  Whenever I" +
            "find myself growing grim about the mouth; whenever it is a damp," +
            "drizzly November in my soul; whenever I find myself involuntarily" +
            "pausing before coffin warehouses, and bringing up the rear of every" +
            "funeral I meet; and especially whenever my hypos get such an upper" +
            "hand of me, that it requires a strong moral principle to prevent me" +
            "from deliberately stepping into the street, and methodically knocking" +
            "people's hats off--then, I account it high time to get to sea as soon" +
            "as I can.  This is my substitute for pistol and ball.  With a" +
            "philosophical flourish Cato throws himself upon his sword; I quietly" +
            "take to the ship.  There is nothing surprising in this.  If they but" +
            "knew it, almost all men in their degree, some time or other, cherish" +
            "very nearly the same feelings towards the ocean with me";

    public abstract Compressor getCompressor();

    @Test
    public void testCompressor() throws IOException {
        Compressor compressor = getCompressor();

        InputStream input = new ByteArrayInputStream(ISHMAEL.getBytes());
        ByteArrayOutputStream encoded = new ByteArrayOutputStream();

        compressor.encode(input, encoded);

        byte[] encodedArray = encoded.toByteArray();

        InputStream coded = new ByteArrayInputStream(encodedArray);
        ByteArrayOutputStream decoded = new ByteArrayOutputStream();

        compressor.decode(coded, decoded);

        byte[] decodedArray = decoded.toByteArray();

        Assertions.assertThat(new String(decodedArray)).isEqualTo(ISHMAEL);
    }
}
