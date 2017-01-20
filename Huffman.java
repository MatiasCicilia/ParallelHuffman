package anaydis.compression;

import anaydis.bit.Bits;
import anaydis.bit.BitsOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Created by matias on 11/5/16.
 */
public class Huffman implements Compressor {

    /*******************************************************************************************
     *                                                                                         *
     *                                 Encoding Method                                         *
     *                                                                                         *
     ******************************************************************************************/

    @Override
    public void encode(@NotNull InputStream input, @NotNull OutputStream output) throws IOException {

        /* Read InputStream & store ascii occurrences on its array spot */
        int[] charFrequencies = new int[256];
        int lastRead = input.read();
        String inputRead = "";
        while (lastRead != -1) {
            charFrequencies[lastRead]++;
            inputRead += (char)lastRead;
            lastRead = input.read();
        }

        /* Add Nodes with (Char, Frequency) to PriorityQueue */
        PriorityQueue<TrieNode> queue = new PriorityQueue<>(256);
        for (int i =0; i < charFrequencies.length; i++) {
            if (charFrequencies[i] != 0 ) {
                queue.add(new TrieNode(i, charFrequencies[i]));
            }
        }

        /* Dequeue two nodes and merge them  */
        while (queue.size() > 1) {
            queue.add(queue.poll().addFrequencies(queue.poll()));
        }

        /* Build (Character -> Bits) table */
        HashMap<Character, Bits> charactersTable = new HashMap<>();
        buildEncodingTable(queue.poll(), charactersTable, new Bits());

        /* First let it know the length of the message */
        writeInputLength(output, inputRead);

        /* Write table into OutputStream */
        writeTable(charactersTable, output);

        /* Write the input into the output, but encoded this time */
        writeEncoded(output, inputRead, charactersTable);
    }

    private void buildEncodingTable(TrieNode trie, HashMap<Character, Bits> map, Bits bits) {
        if (trie == null) return;
        if (trie.isLeaf()) {
            map.put((char)trie.chr, bits);
            return;
        }
        Bits right = bits.copy();
        bits.add(false);
        buildEncodingTable(trie.left, map, bits);
        right.add(true);
        buildEncodingTable(trie.right, map, right);
    }

    private void writeInputLength(@NotNull OutputStream output, String inputRead) throws IOException {
        int size = inputRead.length();
        while (size > 254) {
            output.write(254);
            size -= 254;
        }
        output.write(size);
        output.write(0xFF); //Write Escape-Char (Input length is over)
    }

    private void writeTable (HashMap<Character, Bits> table, OutputStream output) throws IOException{
        Set<Character> keySet= table.keySet();
        keySet.forEach(chr -> writeCode(output, chr, table.get(chr)));
        output.write(0xFF);
    }

    private void writeCode(OutputStream output, char chr, Bits bits) {
        try {
            output.write(chr);
            bits.writeInto(output);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeEncoded(@NotNull OutputStream output, String inputRead, HashMap<Character, Bits> charactersTable) throws IOException {
        BitsOutputStream bitsOutputStream = new BitsOutputStream();
        for (int i = 0; i < inputRead.length() ; i++) {
            char currentChar = inputRead.charAt(i);

            Bits charCode = charactersTable.get(currentChar);

            bitsOutputStream.write(charCode);
        }
        output.write(bitsOutputStream.toByteArray());
    }

    /*******************************************************************************************
     *                                                                                         *
     *                                 Decoding Method                                         *
     *                                                                                         *
     ******************************************************************************************/

    @Override
    public void decode(@NotNull InputStream input, @NotNull OutputStream output) throws IOException {
        /* First, recover the length of the message */
        int length = getInputLength(input);

        /* Build (Bits,Character) table */
        HashMap<String, Character> table = buildDecodingTable(input);

        /* Table has been built. Now it's time to decode & write  */
        writeDecoded(input, output, length, table);

    }

    private int getInputLength(@NotNull InputStream input) throws IOException {
        int length = input.read();
        int nextLength = 0;
        while (nextLength != 0xFF) {
            nextLength = input.read();
            if (nextLength != 0xFF) length += nextLength;
        }
        return length;
    }

    private void writeDecoded(@NotNull InputStream input, @NotNull OutputStream output, int length, HashMap<String, Character> table) throws IOException {
        Bits codedChar = new Bits();
        byte byteRead = (byte)input.read();

        while (length > 0) {

            /* This for iterates through the byte that was just read */
            for (int i = 7; i >= 0; i--) {
                boolean toAdd = bitAt(byteRead, i) == 1;
                codedChar.add(toAdd);

                /* If the Bits built so far matches on the table, write it. */

                if (table.get(codedChar.toString()) != null & length > 0) {
                    output.write(table.get(codedChar.toString()));
                    codedChar = new Bits(); //Reset Bits
                    --length; //Decrement length
                }
            }
            byteRead = (byte)input.read();

        }
    }

    @NotNull
    private HashMap<String, Character> buildDecodingTable(@NotNull InputStream input) throws IOException {
        HashMap<String, Character> table = new HashMap<>();

        Bits aux;

        int charRead = input.read();
        int bitsLength;
        byte byteRead;

        while (charRead != 0xFF) {
            bitsLength = input.read();
            byteRead = (byte)input.read();
            aux = new Bits();

            /* Build Bits that will go on table */

            for (int i = 0; i < bitsLength; i++) {
                if ((i) % 8 == 0 & i != 0) {
                    byteRead = (byte)input.read();
                }
                boolean toAdd = bitAt(byteRead, 7 - (i % 8)) == 1;
                aux.add(toAdd);
            }
            table.put(aux.toString(), (char)charRead);
            charRead = input.read();
        }
        return table;
    }

    private int bitAt(byte n, int k) {
        return (n >> k) & 1;
    }

    /*******************************************************************************************
     *                                                                                         *
     *                            Auxiliar Data Structure (Trie BurrowsNode)                          *
     *                                                                                         *
     ******************************************************************************************/

    private class TrieNode implements Comparable<TrieNode>{
        TrieNode left, right;
        int chr;
        int amount;

        TrieNode(int chr, int amount) {
            this.chr = chr;
            this.amount = amount;
        }

        TrieNode(int chr, int amount, TrieNode left, TrieNode right) {
            this.chr = chr;
            this.amount = amount;
            this.left = left;
            this.right = right;
        }


        TrieNode addFrequencies(TrieNode b) {
            return new TrieNode(0xFF, this.amount + b.amount, b, this);
        }

        @Override
        public int compareTo(@NotNull TrieNode o) {
            return this.amount - o.amount;
        }

        public boolean isLeaf() {
            return this.left == null && this.right == null;
        }
    }
}
