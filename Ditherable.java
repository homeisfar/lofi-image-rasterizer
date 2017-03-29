public interface Ditherable {

    enum Dither {RANDOM, BAYER2X2, BAYER4X4, BAYER8X8, SIMPLE, FS};

    public void dispatchDithering(ImageData img, Dither d);
}
