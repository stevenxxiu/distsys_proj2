package simplestream;

import java.awt.*;
import java.util.Random;

public class ImageRandomReceiver implements ImageReceiverInterface {
    final Object imageNotify;
    /**
     * Receives random images, for testing on systems without webcams.
     */

    byte[] image;
    int width, height, fps;

    public ImageRandomReceiver(int width, int height, int fps) {
        this.width = width;
        this.height = height;
        this.fps = fps;
        imageNotify = new Object();
    }

    public void start() {
        new Thread(new ReceiverThread()).start();
    }

    @Override
    public byte[] getImage() {
        synchronized (imageNotify) {
            return image.clone();
        }
    }

    @Override
    public byte[] getNextImage() {
        synchronized (imageNotify) {
            try {
                imageNotify.wait();
            } catch (InterruptedException e) {
            }
            return image.clone();
        }
    }

    class ReceiverThread implements Runnable {
        public void run() {
            /*
            serve images forever
            */
            try {
                Random random = new Random();
                float hue = random.nextFloat();
                image = new byte[width * height * 3];
                while (true) {
                    synchronized (imageNotify) {
                        for (int i = 0; i < width; i++) {
                            for (int j = 0; j < height; j++) {
                                Color c = Color.getHSBColor(hue, random.nextFloat(), random.nextFloat());
                                image[(i * height + j) * 3] = (byte) c.getRed();
                                image[(i * height + j) * 3 + 1] = (byte) c.getGreen();
                                image[(i * height + j) * 3 + 2] = (byte) c.getBlue();
                            }
                        }
                        imageNotify.notify();
                    }
                    Thread.sleep(1000 / fps);
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
