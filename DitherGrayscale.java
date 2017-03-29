import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.SplittableRandom;
import java.lang.Byte;

import javax.imageio.ImageIO;


public class DitherGrayscale {

    // private BufferedImage output;
    // private static int[] imgData; // direct access to underlying bufferedimage data
    // private static double[] luminosityMatrixFast; // internal representation for operations.
    // private static double[] cloneMatrix;    // Copy for non-destructive error-diffusion. Probably can refactor out. //MARK
    // private static int[] cloneMatrix;
    // private static int[] reds;
    // private static int[] greens;
    // private static int[] blues;
    //
    // private static int[] redsMutate;
    // private static int[] greensMutate;
    // private static int[] bluesMutate;

    private static SplittableRandom rand = new SplittableRandom();
    // public static double luminosityScale = 1.0;

    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;

    /*
    black 0, 0, 0
    gray 104, 104, 104
    dark blue 0, 35, 174
    light blue 95,114, 243
    green 0, 179, 63
    light green 0, 250, 131
    teal 0, 182, 183
    cyan 0, 253, 253
    red 200, 27, 24
    light red 255, 111, 110
    magenta 197, 46 , 175
    pink 255, 120, 245
    brown 196, 105, 43
    yellow 255, 252 135
    light gray 184 184 184
    white 255 255 255
    */

    // enum Dither {RANDOM, BAYER2X2, BAYER4X4, BAYER8X8, SIMPLE, FS};
    // Ditherable.Dither Dither = Ditherable.Dither;

    // DitherGrayscale (BufferedImage origImage) {
    //     output = new BufferedImage(origImage.getWidth(),
    //     origImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
    //
    //     luminosityMatrixFast = new double[origImage.getWidth() * origImage.getHeight()];
    //     // cloneMatrix = new int[luminosityMatrixFast.length];
    //
    //     reds = new int[luminosityMatrixFast.length];
    //     greens = new int[luminosityMatrixFast.length];
    //     blues = new int[luminosityMatrixFast.length];
    //
    //     redsMutate = new int[luminosityMatrixFast.length];
    //     greensMutate = new int[luminosityMatrixFast.length];
    //     bluesMutate = new int[luminosityMatrixFast.length];
    //
    //     for (int i = 0; i < origImage.getWidth(); i++) {
    //         for (int j = 0; j < origImage.getHeight(); j++) {
    //             int index = i + img.width * j;
    //             int color = origImage.getRGB(i, j);
    //
    //             int red = (color >>> 16) & 0xFF;
    //             int green = (color >>> 8) & 0xFF;
    //             int blue = (color >>> 0) & 0xFF;
    //             reds[index] =  (int) red;
    //             greens[index] = (int) green;
    //             blues[index] = (int) blue;
    //             redsMutate[index] =  (int) red;
    //             greensMutate[index] = (int) green;
    //             bluesMutate[index] = (int) blue;
    //             luminosityMatrixFast[index] = (red * 0.21f + green * 0.71f + blue * 0.07f) / 256;
    //             // cloneMatrix[index] = color;
    //         }
    //     }
    //     // imgData = ((DataBufferInt) output.getRaster().getDataBuffer()).getData();
    // }

    private static final double[] randomThreshold = { 0.25, 0.26, 0.27, 0.28, 0.29, 0.3, 0.31,
        0.32, 0.33, 0.34, 0.35, 0.36, 0.37, 0.38, 0.39, 0.4, 0.41, 0.42,
        0.43, 0.44, 0.45, 0.46, 0.47, 0.48, 0.49, 0.5, 0.51, 0.52, 0.53,
        0.54, 0.55, 0.56, 0.57, 0.58, 0.59, 0.6, 0.61, 0.62, 0.63, 0.64,
        0.65, 0.66, 0.67, 0.68, 0.69 };

        private static double[] simpleThreshold = {0.5};

        private static double[] bayer2x2 = //MARK unfinalized for control option to edit
        {0, 2, 3, 1};

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

        private static final double[] halftone =
        {0, 0, 0, 0, 0, 0, 0, 0,
        0, 4, 4, 4, 4, 4, 4, 0,
        0, 4, 63, 63, 63, 63, 63, 0,
        0, 4, 63, 63, 63, 63, 4, 0,
        0, 4, 63, 63, 63, 63, 4, 0,
        0, 4, 63, 63, 63, 63, 4, 0,
        0, 4, 4, 4, 4, 4, 4, 0,
        0, 0, 0, 0, 0, 0, 0, 0};

        static {
            for (int i = 0; i < bayer2x2.length; i++)
                bayer2x2[i] /= 4;
            for (int i = 0; i < bayer4x4.length; i++)
                bayer4x4[i] /= 16;
            for (int i = 0; i < bayer8x8.length; i++)
                bayer8x8[i] /= 64;
        }

        public static void dispatchDithering (ImageData img, Ditherable.Dither d) {
            switch (d){
                case RANDOM:
                    randomThresholdDither(img);
                    break;
                case BAYER2X2:
                    orderedDither2x2(img);
                    break;
                case BAYER4X4:
                    orderedDither4x4(img);
                    break;
                case BAYER8X8:
                    orderedDither8x8(img);
                    break;
                case SIMPLE:
                    simple(img);
                    break;
                case FS:
                    floydSteinberg(img);
                    break;
            }
            // return img.output;
        }


        private static final void simple(ImageData img) {
            // for (int i = 0; i < luminosityMatrixFast.length; i++) {
            //     imgData[i] = luminosityMatrixFast[i] * luminosityScale <= 0.5
            //     ? BLACK : WHITE;
            // }
            // IntStream.range(0, luminosityMatrixFast.length).parallel().forEach(i -> lumiTest(i, imgData));
            // IntStream.range(0, img.height).parallel().forEach(i -> lumiTest(i * img.width));
            IntStream.range(0, img.height).parallel().forEach(i -> parallelOrderedDither(img, i, i * img.width, Ditherable.Dither.SIMPLE));
        }

        private static final void randomThresholdDither(ImageData img) {
            // for (int i = 0; i < luminosityMatrixFast.length; i++) {
            //     imgData[i] = luminosityMatrixFast[i] * luminosityScale <=
            //     randomThreshold[rand.nextInt(randomThreshold.length)]
            //     ? BLACK : WHITE;
            // }
            IntStream.range(0, img.height).parallel().forEach(i -> parallelRandomDither(img, i * img.width));
        }

        /*  Threshold = COLOR(256/4, 256/4, 256/4); // Estimated precision of the palette
        For each pixel, Input, in the original picture:
        Factor  = ThresholdMatrix[xcoordinate % X][ycoordinate % Y];
        Attempt = Input + Factor * Threshold
        Color = FindClosestColorFrom(Palette, Attempt)
        Draw pixel using Color */
        private static final void orderedDither2x2(ImageData img) {
            // int bayerVerticalOffset = -1;
            // int simple = 1;
            // for (int i = 0; i < luminosityMatrixFast.length; i++) {
            //     if (i % img.width == 0) {
            //         bayerVerticalOffset++;
            //         if (bayerVerticalOffset == 2)
            //             bayerVerticalOffset = 0;
            //     }
            //     simple ^= 1;
            //     imgData[i] = luminosityMatrixFast[i] * luminosityScale <= bayer2x2[simple + bayerVerticalOffset]
            //     ? BLACK : WHITE;
            // }
            IntStream.range(0, img.height).parallel().forEach(i -> parallelOrderedDither(img, i, i * img.width, Ditherable.Dither.BAYER2X2));
        }

/*
        private static final void orderedDither2x2() {
            for (int i = 0; i < img.width; i++) {
                int r = i % 2;
                for (int j = 0; j < img.height; j++) {
                    int index = getIndex(j,i);
                    imgData[index] = luminosityMatrixFast[index] * luminosityScale <= bayer2x2[r + j%2]
                    ? BLACK : WHITE;
                }
        }
    }

        private static final int getIndex(int r, int c) {
            return r * img.width + c;
        }
*/

        private static final void orderedDither4x4(ImageData img) {
            // int bayerVerticalOffset = -4;
            // int simple = -1;
            // for (int i = 0; i < luminosityMatrixFast.length; i++) {
            //     if (i % img.width == 0) {
            //         bayerVerticalOffset += 4;
            //         if (bayerVerticalOffset == 16) {
            //             bayerVerticalOffset = 0;
            //         }
            //     }
            //     simple++;
            //     if (simple == 4) simple = 0;
            //     imgData[i] = luminosityMatrixFast[i] * luminosityScale <= bayer4x4[simple + bayerVerticalOffset]
            //     ? BLACK : WHITE;
            // }
            IntStream.range(0, img.height).parallel().forEach(i -> parallelOrderedDither(img, i, i * img.width, Ditherable.Dither.BAYER4X4));
        }


        private static final void orderedDither8x8(ImageData img) {
            // int bayerVerticalOffset = -8;
            // int simple = -1;
            // for (int i = 0; i < luminosityMatrixFast.length; i++) {
            //     if (i % img.width == 0) {
            //         bayerVerticalOffset += 8;
            //         if (bayerVerticalOffset == 64) {
            //             bayerVerticalOffset = 0;
            //         }
            //     }
            //     simple++;
            //     if (simple == 8) simple = 0;
            //         imgData[i] = luminosityMatrixFast[i] * luminosityScale <= bayer8x8[simple + bayerVerticalOffset]
            //     ? BLACK : WHITE;
            // }
            // IntStream.range(0, luminosityMatrixFast.length).parallel().forEach(i -> parallelOrderedDither(i, imgData));
            IntStream.range(0, img.height).parallel().forEach(i -> parallelOrderedDither(img, i, i * img.width, Ditherable.Dither.BAYER8X8));
        }

        private static final void parallelOrderedDither(ImageData img, int row, int i, Ditherable.Dither d) {
            double[] bayer;
            int bayerLength;
            int bayerVerticalOffset;
            int[] imgData = img.imgData;
            double[] luminosityMatrixFast = img.luminosityMatrixFast;
            double luminosityScale = img.luminosityScale;

            switch (d) {
                case SIMPLE:
                    bayer = simpleThreshold;
                    bayerLength = 1;
                    bayerVerticalOffset = 0;
                    break;
                case BAYER2X2:
                    bayer = bayer2x2;
                    bayerLength = 2;
                    bayerVerticalOffset = (row % 2);
                    break;
                case BAYER4X4:
                    bayer = bayer4x4;
                    bayerLength = 4;
                    bayerVerticalOffset = (row % 4) << 2;
                    break;
                case BAYER8X8:
                    bayer = bayer8x8;
                    bayerLength = 8;
                    bayerVerticalOffset = (row % 8) << 3;
                    break;
                default:
                    bayer = null;
                    bayerLength = 0;
                    bayerVerticalOffset = 0;
                    break;
            }

            int bayerHorizontalOffset = -1;
            for (int c = i; c < i + img.width; c++) {
                bayerHorizontalOffset++;
                if (bayerHorizontalOffset == bayerLength)
                    bayerHorizontalOffset = 0;
                    imgData[c] = luminosityMatrixFast[c] * luminosityScale <= bayer[bayerHorizontalOffset + bayerVerticalOffset]
                ? BLACK : WHITE;
                // imgData[c] = findClosestColorFromPalette(reds[c] + (int) (palette.length * (bayer[bayerHorizontalOffset + bayerVerticalOffset] - .5)),
                                                    //    greens[c] + (int) (palette.length * (bayer[bayerHorizontalOffset + bayerVerticalOffset] - .5)),
                                                        // blues[c] + (int) (palette.length * (bayer[bayerHorizontalOffset + bayerVerticalOffset] - .5)));
            }
        }

        private static final void parallelRandomDither(ImageData img, int i) {
            int[] imgData = img.imgData;
            double[] luminosityMatrixFast = img.luminosityMatrixFast;
            double luminosityScale = img.luminosityScale;
            SplittableRandom newRand = rand.split(); // unnecessary?
            for (int c = i; c < i + img.width; c++) {
                imgData[c] = luminosityMatrixFast[c] * luminosityScale <= randomThreshold[newRand.nextInt(randomThreshold.length)]
                ? BLACK : WHITE;
                // imgData[c] = findClosestColorFromPalette(cloneMatrix[c]);
            }
        }

        // private static final void parallelOrderedDither(int i, int[] imgData) {
        //     int bayerVerticalOffset = i / img.width;
        //     bayerVerticalOffset = bayerVerticalOffset - ((bayerVerticalOffset >>> 3) << 3);
        //     bayerVerticalOffset = bayerVerticalOffset << 3;
        //     imgData[i] = luminosityMatrixFast[i] * luminosityScale <= bayer8x8[i%8 + bayerVerticalOffset]
        //     ? BLACK : WHITE;
        // }


 //TODO TST FOR COLOR KEEP THIS MAYBE?
        // private static final void simple2TEST() {
        //     for (int r = 0; r < img.height; r++) {
        //         int index = r * img.width;
        //         for (int c = 0; c < img.width; c++) {
        //             imgData[index] = findClosestColorFromPalette(reds[index], greens[index], blues[index]);
        //             index++;
        //         }
        //     }
        // }

        // private static final void lumiTest(int i) {
        //     for (int c = i; c < i + img.width; c++) {
        //             imgData[c] = luminosityMatrixFast[c] * luminosityScale <= 0.5
        //         ? BLACK : WHITE;
        //     }
        // }


        private static void floydSteinberg(ImageData img) {
            double[] cloneMatrix = img.cloneMatrix;
            System.arraycopy(img.luminosityMatrixFast, 0, cloneMatrix, 0, img.length);
            for (int i = 0; i < img.height; i++) {
                for (int j = 0; j < img.width; j++ ) {
                    int index = i * img.width + j;
                    double oldPixel =  (img.cloneMatrix[index]);
                    img.imgData[index] = oldPixel * img.luminosityScale <= 0.5 ? BLACK : WHITE;
                    double newPixel = img.imgData[index] == BLACK ? 0 : 1;

                    // imgData[index] = (int) newPixel;
                    double quantError = oldPixel - newPixel;
                    quantError = quantError < 0 ? 0
                               : quantError > 1 ? 1
                               : quantError;

                    // System.out.println(oldPixel+ " " + newPixel + " " + quantError);
                    if (j + 1 < img.width) img.cloneMatrix[index + 1] += quantError * (7.0 / 16.0);
                    if (i + 1 == img.height) continue;
                    if (j > 0)                     img.cloneMatrix[index + img.width - 1] += quantError * (3.0 / 16.0);
                    /* no if */                    img.cloneMatrix[index + img.width    ] += quantError * (5.0 / 16.0);
                    if (j + 1 < img.width) img.cloneMatrix[index + img.width + 1] += quantError * (7.0 / 16.0);
                }
            }
        }

    /*    private static void floydSteinberg2() {

            int[] errorArrayR = new int[img.width + 1];
            int[] errorArrayG = new int[img.width + 1];
            int[] errorArrayB = new int[img.width + 1];

            for (int i = 0; i < img.height; i++) {
                int index = i * img.width;
                for (int j = 0; j < img.width - 1; j++ ) {
                     // i * output could be moved to outer loop

                    int r1 = (0x000000FF & reds[index]) + errorArrayR[j + 1];
                    int g1 = (0x000000FF & greens[index]) + errorArrayG[j + 1];
                    int b1 = (0x000000FF & blues[index]) + errorArrayB[j + 1];

                    r1 *= luminosityScale;
                    g1 *= luminosityScale;
                    b1 *= luminosityScale;

                    // System.out.println(r1 +" "+g1 + " "+b1 );

                    // int oldPixel = r1;
                    // oldPixel = oldPixel << 8;
                    // oldPixel += g1;
                    // oldPixel = oldPixel << 8;
                    // oldPixel += b1;

                    int r2 = 0;
                    int g2 = 0;
                    int b2 = 0;

                    int newPixel = BLACK;
                    if (r1 > 0x7F && g1 > 0x7F && b1 > 0x7F) {
                        newPixel = WHITE;
                    }

                    if (newPixel == WHITE) {
                        r2 = 0xFF;
                        g2 = 0xFF;
                        b2 = 0xFF;
                    }

                    int quantErrorR = r1 - r2;
                    int quantErrorG = g1 - g2;
                    int quantErrorB = b1 - b2;

                    // quantErrorR = quantErrorR < 0 ? 0
                    //             : quantErrorR > 0xFF ? 0xFF
                    //             : quantErrorR;
                    //
                    // quantErrorG = quantErrorG < 0 ? 0
                    //             : quantErrorG > 0xFF ? 0xFF
                    //             : quantErrorG;
                    //
                    // quantErrorB = quantErrorB < 0 ? 0
                    //             : quantErrorB > 0xFF ? 0xFF
                    //             : quantErrorB;


                    imgData[index] = newPixel;// | 0xFF000000;

                    //TODO CONVERT THIS TO RGB COMPARE EACH RGB


                    // if (i + 1 == img.height) continue;

                    // r  * 0.4375  7/16
                    // ll * 0.1875  3/16
                    // l  * 0.3125  5/16
                    // lr * 0.0625  1/16

                    // lower left pixel
                if (j != 0) {
                    errorArrayR[j - 1] += quantErrorR * 3 / 16;
                    errorArrayG[j - 1] += quantErrorG * 3 / 16;
                    errorArrayB[j - 1] += quantErrorB * 3 / 16;
                }

                // below
                    errorArrayR[j] += quantErrorR * 5 / 16;
                    errorArrayG[j] += quantErrorG * 5 / 16;
                    errorArrayB[j] += quantErrorB * 5 / 16;

                // lower right
                    errorArrayR[j + 1] = quantErrorR * 1 / 16;
                    errorArrayG[j + 1] = quantErrorG * 1 / 16;
                    errorArrayB[j + 1] = quantErrorB * 1 / 16;

                // right
                    errorArrayR[j + 2] += quantErrorR * 7 / 16;
                    errorArrayG[j + 2] += quantErrorG * 7 / 16;
                    errorArrayB[j + 2] += quantErrorB * 7 / 16;

                    index ++;
                }
            }
        }
        */


        /* FS
        for each y from top to bottom
            for each x from left to right
                  oldpixel  := pixel[x][y]
                  newpixel  := find_closest_palette_color(oldpixel)
                  pixel[x][y]  := newpixel
                  quant_error  := oldpixel - newpixel
                  pixel[x + 1][y    ] := pixel[x + 1][y    ] + quant_error * 7 / 16
                  pixel[x - 1][y + 1] := pixel[x - 1][y + 1] + quant_error * 3 / 16
                  pixel[x    ][y + 1] := pixel[x    ][y + 1] + quant_error * 5 / 16
                  pixel[x + 1][y + 1] := pixel[x + 1][y + 1] + quant_error * 1 / 16
                  */



}
                /*
        private static void floydSteinberg(ImageData img) {

            // restore from original
            // for (int i = 0; i < reds.length; i++) {
            //     redsMutate[i] = reds[i];
            //     greensMutate[i] = greens[i];
            //     bluesMutate[i] = blues[i];
            // }
            img.resetMutateArrays();

            for (int r = 0; r < img.height; r++) {
                int index = r * img.width;
                for (int c = 0; c < img.width; c++) {
                    int r1 = img.redsMutate[index];
                    int g1 = img.greensMutate[index];
                    int b1 = img.bluesMutate[index];

                    // int newPixel = findClosestColorFromPalette(r1, g1, b1);


                    int r2 = (newPixel >>> 16) & 0xFF;
                    int g2 = (newPixel >>> 8) & 0xFF;
                    int b2 = (newPixel >>> 0) & 0xFF;

                    int newPixel = BLACK;
                    if (r1 > 0x7F && g1 > 0x7F && b1 > 0x7F) {
                        newPixel = WHITE;
                    }

                    if (newPixel == WHITE) {
                        r2 = 0xFF;
                        g2 = 0xFF;
                        b2 = 0xFF;
                    }
                    img.imgData[index] = newPixel;

                    int quantErrorR = r1 - r2;
                    int quantErrorG = g1 - g2;
                    int quantErrorB = b1 - b2;


                    if (c + 1 < img.width) {
                        img.redsMutate[index + 1] += quantErrorR * (7./16);
                        img.greensMutate[index + 1] += quantErrorG * (7./16);
                        img.bluesMutate[index + 1] += quantErrorB * (7./16);
                    }
                    if (r + 1 == img.height) continue;

                    if (c > 0) {
                        img.redsMutate[index + img.width - 1] += quantErrorR * (3./16);
                        img.greensMutate[index + img.width - 1] += quantErrorG * (3./16);
                        img.bluesMutate[index + img.width - 1] += quantErrorB * (3./16);
                    }
                    // no if
                        img.redsMutate[index + img.width    ] += quantErrorR * (5./16);
                        img.greensMutate[index + img.width    ] += quantErrorG * (5./16);
                        img.bluesMutate[index + img.width    ] += quantErrorB * (5./16);

                    if (c + 1 < img.width) {
                        img.redsMutate[index + img.width + 1] += quantErrorR * (1./16);
                        img.greensMutate[index + img.width + 1] += quantErrorG * (1./16);
                        img.bluesMutate[index + img.width + 1] += quantErrorB * (1./16);
                    }
                    index++;
                }
            }
        }

        */

/*
        private static int findClosestColorFromPalette(int r, int g, int b) {
            RGB rgb = new RGB(r,g,b);
            RGB nearest = palette[0];

            for (RGB n : palette) {
                if (n.diff(rgb) < nearest.diff(rgb)) {
                    nearest = n;
                }
            }
            return nearest.toInt(nearest);
        }


        private static int findClosestColorFromPalette(int c) {
            RGB rgb = new RGB(c);

            // int newPixel = BLACK;
            // r *= luminosityScale;
            // g *= luminosityScale;
            // b *= luminosityScale;
            // // Random rand = new Random();
            // // int compareval = rand.nextInt((137 - 117) + 1) + 117;
            // if (r > 0x7F && g > 0x7F && b > 0x7F) {
            //     newPixel = WHITE;
            // }

            RGB nearest = palette[0];

            for (RGB n : palette) {
                if (n.diff(rgb) < nearest.diff(rgb)) {
                    nearest = n;
                }
            }
            return nearest.toInt(nearest);
        }
    }
*/

/*
class RGB {
    int r, g, b;
    public RGB(int color) {
        this.r = (color >>> 16) & 0xFF;
        this.g = (color >>> 8) & 0xFF;
        this.b = (color >>> 0) & 0xFF;
    }
    public RGB (int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public int toInt(RGB o) {
        int color = clamp(r);
        color = color << 8;
        color += clamp(g);
        color = color << 8;
        color += clamp(b);
        return 0xFF000000 | color;
    }

    public int diff(RGB o) {

        int dist_r = Math.abs(r - o.r);
        int dist_g = Math.abs(g - o.g);
        int dist_b = Math.abs(b - o.b);

        // 3d distance formula for better accuracy. Only care about magnitude.
        int sq_distance = (dist_r * dist_r) + (dist_g * dist_g) + (dist_b * dist_b);
        // return Math.abs(r - o.r) +  Math.abs(g - o.g) +  Math.abs(b - o.b);
        return sq_distance;
    }

    private int clamp(int c) {
        return Math.max(0, Math.min(255, c));
    }
}
*/
