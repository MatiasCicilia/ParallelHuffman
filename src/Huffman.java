import com.sun.istack.internal.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ForkJoinPool;

/**
 * Created by Bruno on 20/2/2017.
 */
public class Huffman {

    public  ByteArrayOutputStream[] encode(@NotNull InputStream inputStream) throws IOException {
        String text = "";
        int bytes = inputStream.read();
        final int[] characters = new int[256];
        while (bytes != -1){
            ++characters[bytes];
            text += (char)bytes;
            bytes = inputStream.read();
        }
        ++characters[2]; //escape character

        int m = Runtime.getRuntime().availableProcessors();
        ByteArrayOutputStream[] outputStream = new ByteArrayOutputStream[((text.length()%m == 0)? m : m + 1) + 1];
        for (int i = 0; i < outputStream.length ; i++) {
            outputStream[i] = new ByteArrayOutputStream();
        }
        PriorityQueue<Node<Tuple<Integer, Character>>> queue = buildTree(characters);
        HashMap<Character,Bits> map = new HashMap<>();
        buildAndWriteTable(outputStream, queue.remove(), new Bits(),map);
        writeCode(outputStream, text, map, m);
        return outputStream;
    }


    public ByteArrayOutputStream decode(@NotNull InputStream[] inputStream) throws IOException {
        int bytes = inputStream[0].read();
        HashMap<String, Character> map = new HashMap<>();

        rebuildTable(inputStream[0], bytes, map);

        bytes = inputStream[0].read();
        int multiplier = 0;
        while(bytes != (char)1){
            multiplier += bytes;
            bytes = inputStream[0].read();
        }
        int length = 255*multiplier + inputStream[0].read();

        int m = Runtime.getRuntime().availableProcessors();
        ByteArrayOutputStream[] outputStream = new ByteArrayOutputStream[((length%m == 0)? m : m + 1) /*+ 1*/];
        for (int i = 0; i < outputStream.length ; i++) {
            outputStream[i] = new ByteArrayOutputStream();
        }
        ForkJoinPool.commonPool().invoke(new ParallelDecoder(inputStream, outputStream, map, 1));

        ByteArrayOutputStream o = new ByteArrayOutputStream();
        for(ByteArrayOutputStream os : outputStream){
            o.write(os.toByteArray());
        }
        return o;
    }


    @NotNull
    private PriorityQueue<Node<Tuple<Integer, Character>>> buildTree(int[] characters) {
        PriorityQueue<Node<Tuple<Integer, Character>>> queue = new PriorityQueue<>((o1, o2) -> o1.value.firstValue.compareTo(o2.value.firstValue));

        for (int i = 0; i < characters.length ; i++) {
            if (characters[i] != 0){
                Node<Tuple<Integer, Character>> node = new Node<>(new Tuple<>(characters[i], (char)i));
                queue.add(node);
            }
        }

        while (queue.size() > 1){
            Node<Tuple<Integer,Character>> node1 = queue.remove();
            Node<Tuple<Integer,Character>> node2 = queue.remove();
            Node<Tuple<Integer,Character>> node = new Node<>(new Tuple<>(node1.value.firstValue + node2.value.firstValue,null),node1,node2);
            queue.add(node);
        }
        return queue;
    }


    private void buildAndWriteTable(OutputStream[] outputStream, Node<Tuple<Integer, Character>> tree, Bits bitsLeft, Map<Character,Bits> map) throws IOException {
        if (tree == null) return;
        if (tree.isLeaf())  {
            map.put(tree.value.secondValue,bitsLeft);
            outputStream[0].write(tree.value.secondValue);
            bitsLeft.writeInto(outputStream[0]);
            return;
        }
        Bits bitsRight = bitsLeft.copy();
        bitsLeft.add(false);
        buildAndWriteTable(outputStream, tree.left, bitsLeft,map);
        bitsRight.add(true);
        buildAndWriteTable(outputStream, tree.right, bitsRight,map);
    }


    private void writeCode(OutputStream[] outputStream, String text, HashMap<Character, Bits> map, int m) throws IOException {
        outputStream[0].write((char)1);
        int multiplier = text.length()/255;
        while(multiplier > 255){
            outputStream[0].write(255);
            multiplier -= 255;
        }
        outputStream[0].write(multiplier);
        outputStream[0].write((char)1);
        outputStream[0].write(text.length()%255);

        ForkJoinPool.commonPool().invoke(new ParallelEncoder(outputStream, text, map, 0, 0, text.length()/m));
    }


    static boolean bitAt(int b, int pos){
        return (b << pos & 0b10000000) == 0b10000000;
    }


    private void rebuildTable(@NotNull InputStream inputStream, int bytes, HashMap<String, Character> map) throws IOException {
        while(bytes != (char)1){
            Bits bits = new Bits();
            char chr = (char)bytes;
            int length = inputStream.read();

            while (length > 8){
                byte code = (byte)inputStream.read();
                for (int i = 0; i < 8; i++) {
                    bits.add(bitAt(code,i));
                }
                length -= 8;
            }

            byte code = (byte)inputStream.read();
            for (int i = 0; i < length; i++) {
                bits.add(bitAt(code,i));
            }

            map.put(bits.toString(), chr);
            bytes = inputStream.read();
        }
    }


    private class Node<Value>{
        private Node<Value> left;
        private Node<Value> right;
        private Value value;

        private Node(Value value){
            this.value = value;
        }

        private Node(Value value, Node<Value> left, Node<Value> right){
            this.value = value;
            this.left = left;
            this.right = right;
        }

        private boolean isLeaf(){
            return left == null && right == null;
        }
    }
}
