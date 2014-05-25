package simplestream;

public interface ImageReceiverInterface {
<<<<<<< HEAD
    public byte[] image = new byte[0];
=======
    // java doesn't allow fields to be specified in interfaces
    public byte[] getImage();

    public byte[] getNextImage();

    public void start();
>>>>>>> 8776f3f1d988350d2506496f5033ba509abe244e
}
