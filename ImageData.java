/* A wrapper class to store image data conveniently
   For speed, this class isn't designed to manipulate
   its internal data in an OO fashion. The method calls
   would add too much overhead. As such, DitherGrayscale.java and
   DitherColor.java manipulate the internal data of this class
   directly. */
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;


public class ImageData {
    public BufferedImage output;
    public int width;
    public int height;
    public int length;
    public int[] imgData;   // direct access to underlying bufferedimage data
    public double[] luminosityMatrixFast;   // internal representation for operations.
    public double[] cloneMatrix; // Copy for non-destructive error-diffusion. Probably can refactor out. //MARK
    // public int[] cloneMatrix;

    // "Master" copies of the original image for non-destructive editing
    public int[] reds;
    public int[] greens;
    public int[] blues;

    // Used for destructive editing for error diffusion
    public int[] redsMutate;
    public int[] greensMutate;
    public int[] bluesMutate;

    // public SplittableRandom rand = new SplittableRandom();
    public double luminosityScale = 1.0;

    ImageData (BufferedImage origImage) {
        // TYPE_INT_ARGB so that JavaFX's ImageView doesn't completely
        // recreate the image data. Significant speedup.
        output = new BufferedImage(origImage.getWidth(),
        origImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        width = origImage.getWidth();
        height = origImage.getHeight();
        length = width * height;

        luminosityMatrixFast = new double[length];
        cloneMatrix = new double[length];

        reds = new int[length];
        greens = new int[length];
        blues = new int[length];

        redsMutate = new int[length];
        greensMutate = new int[length];
        bluesMutate = new int[length];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int index = i + width * j;
                int color = origImage.getRGB(i, j);

                int red = (color >>> 16) & 0xFF;
                int green = (color >>> 8) & 0xFF;
                int blue = (color >>> 0) & 0xFF;
                reds[index] =  (int) red;
                greens[index] = (int) green;
                blues[index] = (int) blue;
                redsMutate[index] =  (int) red;
                greensMutate[index] = (int) green;
                bluesMutate[index] = (int) blue;

                luminosityMatrixFast[index] = (red * 0.21f + green * 0.71f + blue * 0.07f) / 256;
                // cloneMatrix[index] = color;
            }
        }
        imgData = ((DataBufferInt) output.getRaster().getDataBuffer()).getData();
    }

    void resetMutateArrays() {
        for (int i = 0; i < length; i++) {
            redsMutate[i] = reds[i];
            greensMutate[i] = greens[i];
            bluesMutate[i] = blues[i];
        }
    }
}
