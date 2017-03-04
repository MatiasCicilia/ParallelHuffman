import com.sun.istack.internal.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ForkJoinPool;

/**
 * Created by Bruno on 20/2/2017.
 */
public class Huffman {

    public void encode(@NotNull InputStream inputStream, @NotNull OutputStream outputStream) throws IOException {
        String text = "";
        int bytes = inputStream.read();
        final int[] characters = new int[256];
        while (bytes != -1){
            ++characters[bytes];
            text += (char)bytes;
            bytes = inputStream.read();
        }
        ++characters[1]; //escape char

        PriorityQueue<Node<Tuple<Integer, Character>>> queue = buildTree(characters);
        HashMap<Character,Bits> map = new HashMap<>();
        buildAndWriteTable(outputStream, queue.remove(), new Bits(),map);
        writeCode(outputStream, text, map);
    }


    public void decode(@NotNull InputStream inputStream, @NotNull OutputStream outputStream) throws IOException {
        int bytes = inputStream.read();
        HashMap<String, Character> map = new HashMap<>();

        rebuildTable(inputStream, bytes, map);

        bytes = inputStream.read();
        int multiplier = 0;
        while(bytes != (char)1){
            multiplier += bytes;
            bytes = inputStream.read();
        }
        int length = 255*multiplier + inputStream.read();

        Bits bits = new Bits();
        bytes = inputStream.read();
        loop: //Only for Coverage. On line 110 Instead "break loop;", it was "return;", but the last line of the Decode Method didn't count for coverage because it never reached that line.
        while(bytes != -1){
            for (int i = 0; i < 8; i++) {
                bits.add(bitAt(bytes, i));
                if (map.containsKey(bits.toString())){
                    outputStream.write(map.get(bits.toString()));
                    bits = new Bits();
                    --length;
                    if (length == 0) break loop;
                }
            }
            bytes = inputStream.read();
        }
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


    private void buildAndWriteTable(OutputStream outputStream, Node<Tuple<Integer, Character>> tree, Bits bitsLeft, Map<Character,Bits> map) throws IOException {
        if (tree == null) return;
        if (tree.isLeaf())  {
            map.put(tree.value.secondValue,bitsLeft);
            outputStream.write(tree.value.secondValue);
            bitsLeft.writeInto(outputStream);
            return;
        }
        Bits bitsRight = bitsLeft.copy();
        bitsLeft.add(false);
        buildAndWriteTable(outputStream, tree.left, bitsLeft,map);
        bitsRight.add(true);
        buildAndWriteTable(outputStream, tree.right, bitsRight,map);
    }


    private void writeCode(OutputStream outputStream, String text, HashMap<Character, Bits> map) throws IOException {
        outputStream.write((char)1);
        int multiplier = text.length()/255;
        while(multiplier > 255){
            outputStream.write(255);
            multiplier -= 255;
        }
        outputStream.write(multiplier);
        outputStream.write((char)1);
        outputStream.write(text.length()%255);

        int m = Runtime.getRuntime().availableProcessors();
        BitsOutputStream[] outputInOrder = new BitsOutputStream[(text.length()%m == 0)? m : m + 1];
        ForkJoinPool.commonPool().invoke(new ParallelHuffman(outputStream, text, map, outputInOrder, 0, 0, text.length()/m));
        for (int i = 0; i < outputInOrder.length ; i++) {
            outputStream.write(outputInOrder[i].toByteArray());
        }

    }


    private boolean bitAt(int b, int pos){
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
