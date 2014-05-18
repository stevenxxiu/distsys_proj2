package simplestream;

import com.github.sarxos.webcam.ds.buildin.natives.Device;
import com.github.sarxos.webcam.ds.buildin.natives.DeviceList;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;
import org.bridj.Pointer;

public class ImageLocalReceiver implements ImageReceiverInterface {
    byte[] image;
    int width = 320;
    int height = 240;
    int fps = 30;

    public ImageLocalReceiver() {}

    public void start() {
        /*
        serve images forever
        */
        OpenIMAJGrabber grabber = new OpenIMAJGrabber();
        Pointer<DeviceList> devices = grabber.getVideoDevices();
        Device device = devices.get().asArrayList().get(0);
        boolean started = grabber.startSession(width, height, fps, Pointer.pointerTo(device));
        if (!started) {
            throw new RuntimeException("Not able to start native grabber!");
        }
        while(true) {
            grabber.nextFrame();
            image = grabber.getImage().getBytes(width * height * 3);
        }
    }

    @Override
    public byte[] getImage() {
        return image;
    }

    @Override
    public void setImage(byte[] image) {
        this.image = image;
    }
}
