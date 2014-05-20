package simplestream;

public interface ImageReceiverInterface {
    // java doesn't allow fields to be specified in interfaces
    public byte[] getImage();

    public byte[] getNextImage();

    public void start();
}
