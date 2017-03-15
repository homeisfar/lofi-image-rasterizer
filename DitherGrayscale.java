import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

import javax.imageio.ImageIO;

public class DitherGrayscale {

    private BufferedImage original;
    private static BufferedImage output;
    private static double[][] luminosityMatrix;
    private static float[] luminosityMatrixFast;
    // private static double  lum

    DitherGrayscale (BufferedImage origImage) {
        original = origImage;
        output = new BufferedImage(origImage.getWidth(),
        origImage.getHeight(), BufferedImage.TYPE_INT_RGB);

        // luminosityMatrix = new double[origImage.getWidth()][origImage.getHeight()];
        luminosityMatrixFast = new float[origImage.getWidth() * origImage.getHeight()];

        for (int i = 0; i < origImage.getWidth(); i++) {
            for (int j = 0; j < origImage.getHeight(); j++) {

                int color = origImage.getRGB(i, j);

                int red = (color >>> 16) & 0xFF;
                int green = (color >>> 8) & 0xFF;
                int blue = (color >>> 0) & 0xFF;
                // luminosityMatrix[i][j] = (red * 0.21f + green * 0.71f + blue * 0.07f) / 255;
                luminosityMatrixFast[i + output.getWidth() * j] = (red * 0.21f + green * 0.71f + blue * 0.07f) / 255;
            }
        }
    }
    public static double luminosityScale = 1.0;
    private static final double[] randomThreshold = { 0.25, 0.26, 0.27, 0.28, 0.29, 0.3, 0.31,
        0.32, 0.33, 0.34, 0.35, 0.36, 0.37, 0.38, 0.39, 0.4, 0.41, 0.42,
        0.43, 0.44, 0.45, 0.46, 0.47, 0.48, 0.49, 0.5, 0.51, 0.52, 0.53,
        0.54, 0.55, 0.56, 0.57, 0.58, 0.59, 0.6, 0.61, 0.62, 0.63, 0.64,
        0.65, 0.66, 0.67, 0.68, 0.69 };

        // private static final double[][] bayer2x2 = {{0.25, 0.40}, {0.55, 0.70}};

        private static final double[] bayer2x2 =
        {0, 0.5, 0.75, 0.25};

        private static final double[] bayer4x4 =
        {0, 8, 2, 10,
        12, 4, 14, 6,
        3, 11, 1, 9,
        15, 7, 13, 5};

        private static final double[] bayer8x8 =
        {0, 48, 12, 60, 3, 51, 15, 63,
        32, 16, 44, 28, 35, 19, 47, 31,
        8, 56, 4, 52, 11, 59, 7, 55,
        40, 24, 36, 20, 43, 27, 39, 23,
        2, 50, 14, 62, 1, 49, 13, 61,
        34, 18, 46, 30, 33, 17, 45, 29,
        10, 58, 6, 54, 9, 57, 5, 53,
        42, 26, 38, 22, 41, 25, 37, 21};

        static {
            for (int i = 0; i < bayer4x4.length; i++) {
                    bayer4x4[i] /= 16;
            }
            for (int i = 0; i < bayer8x8.length; i++) {
                    bayer8x8[i] /= 64;
            }
        }

        static final BufferedImage randomThresholdDither() {
            Random rn = new Random();
            int[] imgData = ((DataBufferInt) output.getRaster().getDataBuffer()).getData();
            for (int i = 0; i < luminosityMatrixFast.length; i++) {
                imgData[i] = luminosityMatrixFast[i] * luminosityScale <=
                randomThreshold[rn.nextInt(randomThreshold.length)]
                ? 0x000000 : 0xFFFFFF;
            }
            return output;
        }


        /*  Threshold = COLOR(256/4, 256/4, 256/4); // Estimated precision of the palette
        For each pixel, Input, in the original picture:
        Factor  = ThresholdMatrix[xcoordinate % X][ycoordinate % Y];
        Attempt = Input + Factor * Threshold
        Color = FindClosestColorFrom(Palette, Attempt)
        Draw pixel using Color */
        static final BufferedImage orderedDither2x2() {
            int[] imgData = ((DataBufferInt) output.getRaster().getDataBuffer()).getData();
            int bayerVerticalOffset = -1;
            int simple = 1;
            for (int i = 0; i < luminosityMatrixFast.length; i++) {
                if (i % output.getWidth() == 0) {
                    bayerVerticalOffset++;
                    if (bayerVerticalOffset == 2)
                        bayerVerticalOffset = 0;
                }
                simple ^= 1;
                imgData[i] = luminosityMatrixFast[i] * luminosityScale <= bayer2x2[simple + bayerVerticalOffset]
                ? 0x000000 : 0xFFFFFF;
            }
            return output;
        }

/*
        static final BufferedImage orderedDither2x2() {
            int[] imgData = ((DataBufferInt) output.getRaster().getDataBuffer()).getData();
            for (int i = 0; i < output.getWidth(); i++) {
                int r = i % 2;
                for (int j = 0; j < output.getHeight(); j++) {
                    int index = getIndex(j,i);
                    imgData[index] = luminosityMatrixFast[index] * luminosityScale <= bayer2x2[r + j%2]
                    ? 0x000000 : 0xFFFFFF;
                }
        }
        return output;
    }


        private static final int getIndex(int r, int c) {
            return r * output.getWidth() + c;
        }
*/

        static final BufferedImage orderedDither4x4() {
            int[] imgData = ((DataBufferInt) output.getRaster().getDataBuffer()).getData();
            int bayerVerticalOffset = -4;
            for (int i = 0; i < luminosityMatrixFast.length; i++) {
                if (i % output.getWidth() == 0) {
                    bayerVerticalOffset += 4;
                    if (bayerVerticalOffset == 16) {
                        bayerVerticalOffset = 0;
                    }
                }
                imgData[i] = luminosityMatrixFast[i] * luminosityScale <= bayer4x4[i%4 + bayerVerticalOffset]
                ? 0x000000 : 0xFFFFFF;
            }
            return output;
        }

        static final BufferedImage orderedDither8x8() {
            int[] imgData = ((DataBufferInt) output.getRaster().getDataBuffer()).getData();
            int bayerVerticalOffset = -8;
            int simple = -1;
            for (int i = 0; i < luminosityMatrixFast.length; i++) {
                if (i % output.getWidth() == 0) {
                    bayerVerticalOffset += 8;
                    if (bayerVerticalOffset == 64) {
                        bayerVerticalOffset = 0;
                    }
                }
                simple++;
                if (simple == 8) simple = 0;
                    imgData[i] = luminosityMatrixFast[i] * luminosityScale <= bayer8x8[simple + bayerVerticalOffset]
                ? 0x000000 : 0xFFFFFF;
            }
            return output;
        }

        static final BufferedImage simple() {
            int[] imgData = ((DataBufferInt) output.getRaster().getDataBuffer()).getData();
                for (int i = 0; i < luminosityMatrixFast.length; i++) {
                    imgData[i] = luminosityMatrixFast[i] * luminosityScale <= 0.5
                    ? 0x000000 : 0xFFFFFF;
                }
            return output;
        }
    }
