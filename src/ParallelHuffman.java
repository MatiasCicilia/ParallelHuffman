

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.concurrent.RecursiveAction;

/**
 * Created by Bruno on 20/2/2017.
 */
public class ParallelHuffman extends RecursiveAction {
    private OutputStream outputStream;
    private String text;
    private HashMap<Character, Bits> map;
    private BitsOutputStream[] outputInOrder;
    private int order;
    private int from;
    private int SEQUENTIAL_THRESHOLD;

    public ParallelHuffman(OutputStream outputStream, String text, HashMap<Character, Bits> map, BitsOutputStream[] outputInOrder, int order, int from, int SEQUENTIAL_THRESHOLD){
        this.outputStream = outputStream;
        this.text = text;
        this.map = map;
        this.outputInOrder = outputInOrder;
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
                ParallelHuffman parallelHuffman = new ParallelHuffman(outputStream, text, map, outputInOrder, order+1, from + SEQUENTIAL_THRESHOLD, SEQUENTIAL_THRESHOLD);
                parallelHuffman.fork();
                writeCode();
                parallelHuffman.join();
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
        bitsOutputStream.write(map.get((char)1));
        outputInOrder[order] = bitsOutputStream;
    }
}
