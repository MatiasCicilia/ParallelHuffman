import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Bruno on 22/2/2017.
 */
public class HuffmanTest {
    /*
    * A = 0
    * B = 10
    *  = 110 (frecuencia 1)
    * R = 111
    * testEncode para m = 2
    * */
    @Test
    public void testEncode() throws IOException {
        final Huffman huffman =  new Huffman();
        final InputStream input = new ByteArrayInputStream("ABARABAR".getBytes());
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        huffman.encode(input, output);
        Assert.assertArrayEquals(new byte[]{'A',1,0,'B',2,(byte)0b10000000,(char)1,3,(byte)0b11000000,'R',3,(byte)0b11100000,(char)1,0,(char)1,8,(byte)0b01001111,(byte)0b10000000,(byte)0b01001111,(byte)0b10000000}, output.toByteArray());
    }

    @Test
    public void testDecode() throws IOException {
        final String text = "En 1951, a David Huffman y a sus compañeros de clase de la asignatura “Teoría de la Información” se les permitió optar entre la realización de un examen final o la presentación de un trabajo. El profesor Robert. M. Fano asignó las condiciones del trabajo bajo la premisa de encontrar el código binario más eficiente. Huffman, ante la imposibilidad de demostrar qué código era más eficiente, se rindió y empezó a estudiar para el examen final. Mientras estaba en este proceso vino a su mente la idea de usar árboles binarios de frecuencia ordenada y rápidamente probó que éste era el método más eficiente. Con este estudio, Huffman superó a su profesor, quien había trabajado con el inventor de la teoría de la información Claude Shannon con el fin de desarrollar un código similar. Huffman solucionó la mayor parte de los errores en el algoritmo de codificación Shannon-Fano. La solución se basaba en el proceso de construir el árbol de abajo a arriba en vez de al contrario.";
        final Huffman huffman = new Huffman();
        final InputStream input = new ByteArrayInputStream(text.getBytes());
        final ByteArrayOutputStream auxOutput = new ByteArrayOutputStream();
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        huffman.encode(input, auxOutput);
        huffman.decode(new ByteArrayInputStream(auxOutput.toByteArray()), output);
        Assert.assertEquals(text, output.toString());
    }
}
