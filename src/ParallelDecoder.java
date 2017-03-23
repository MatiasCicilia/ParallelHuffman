import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.RecursiveAction;

/**
 * Created by Bruno on 13/3/2017.
 */
public class ParallelDecoder extends RecursiveAction {
    private final InputStream[] inputStream;
    private final ByteArrayOutputStream[] outputStream;
    private final HashMap<String, Character> map;
    private final int order;

    public ParallelDecoder(InputStream[] inputStream, ByteArrayOutputStream[] outputStream, HashMap<String, Character> map, int order) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.map = map;
        this.order = order;
    }

    @Override
    protected void compute() {
        try{
            if (order == inputStream.length - 1) readCode();
            else {
                ParallelDecoder parallelDecoder = new ParallelDecoder(inputStream, outputStream, map, order + 1);
                parallelDecoder.fork();
                readCode();
                parallelDecoder.join();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readCode() throws IOException {
        Bits bits = new Bits();
        int bytes = inputStream[order].read();
        while(bytes != -1){
            for (int i = 0; i < 8; i++) {
                bits.add(Huffman.bitAt(bytes, i));
                if (map.containsKey(bits.toString())){
                    if (map.get(bits.toString()) == (char)2) return;
                    outputStream[order - 1].write(map.get(bits.toString()));
                    bits = new Bits();
                }
            }
            bytes = inputStream[order].read();
        }
    }
}
