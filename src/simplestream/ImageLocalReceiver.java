package simplestream;

import com.github.sarxos.webcam.ds.buildin.natives.Device;
import com.github.sarxos.webcam.ds.buildin.natives.DeviceList;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;
import org.bridj.Pointer;

public class ImageLocalReceiver implements ImageReceiverInterface {
    byte[] image;
    Object imageNotify = new Object();
    int width, height, fps;

    public ImageLocalReceiver(int width, int height, int fps) {
        this.width = width;
        this.height = height;
        this.fps = fps;
    }

    class ReceiverThread implements Runnable {
        public void run() {
            /*
            serve images forever
            */
            OpenIMAJGrabber grabber = new OpenIMAJGrabber();
            Device device;
            Pointer<DeviceList> devices = grabber.getVideoDevices();
            try{
                device = devices.get().asArrayList().get(0);
            }catch(IndexOutOfBoundsException e){
                System.out.println("No webcam found");
                return;
            }
            boolean started = grabber.startSession(width, height, fps, Pointer.pointerTo(device));
            if (!started) {
                throw new RuntimeException("Not able to start native grabber");
            }
            while(true) {
                grabber.nextFrame();
                synchronized (imageNotify){
                    image = grabber.getImage().getBytes(320 * 240 * 3);
                    imageNotify.notify();
                }
            }
        }
    }

    public void start() {
        new Thread(new ReceiverThread()).start();
    }

    @Override
    public byte[] getImage() {
        return image;
    }

    @Override
    public byte[] getNextImage() {
        synchronized (imageNotify){
            try {
                imageNotify.wait();
            }catch(InterruptedException e){}
        }
        return image;
    }
}
