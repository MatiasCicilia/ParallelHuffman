package anaydis.compression;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Created by matias on 11/9/16.
 */
public class HuffmanTest extends AbstractCompressorTest{
    @Override
    public Compressor getCompressor() {
        return new Huffman();
    }
}
