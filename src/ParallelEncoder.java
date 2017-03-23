

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.concurrent.RecursiveAction;

/**
 * Created by Bruno on 20/2/2017.
 */
public class ParallelEncoder extends RecursiveAction {
    private final OutputStream[] outputStream;
    private final String text;
    private final HashMap<Character, Bits> map;
    private final int order;
    private final int from;
    private int SEQUENTIAL_THRESHOLD;

    public ParallelEncoder(OutputStream[] outputStream, String text, HashMap<Character, Bits> map, /*BitsOutputStream[] outputInOrder,*/ int order, int from, int SEQUENTIAL_THRESHOLD){
        this.outputStream = outputStream;
        this.text = text;
        this.map = map;
        this.order = order;
        this.from = from;
        this.SEQUENTIAL_THRESHOLD = SEQUENTIAL_THRESHOLD;
    }

    @Override
    protected void compute() {
        try {
            if ((from + SEQUENTIAL_THRESHOLD) >= text.length()) {
                SEQUENTIAL_THRESHOLD = text.length() - from;
                writeCode();
            } else {
                ParallelEncoder parallelEncoder = new ParallelEncoder(outputStream, text, map, /*outputInOrder,*/ order+1, from + SEQUENTIAL_THRESHOLD, SEQUENTIAL_THRESHOLD);
                parallelEncoder.fork();
                writeCode();
                parallelEncoder.join();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void writeCode() throws IOException {
        BitsOutputStream bitsOutputStream = new BitsOutputStream();
        for (int i = from; i < from + SEQUENTIAL_THRESHOLD ; i++) {
            bitsOutputStream.write(map.get(text.charAt(i)));
        }
        bitsOutputStream.write(map.get((char)2));
        outputStream[order+1].write(bitsOutputStream.toByteArray());
    }
}
