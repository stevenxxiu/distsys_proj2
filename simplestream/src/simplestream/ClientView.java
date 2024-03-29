package simplestream;

import javax.swing.*;

public class ClientView {
    int width;
    int height;
    ImageReceiverInterface receiver;

    public ClientView(int width, int height, ImageReceiverInterface receiver){
        this.width = width;
        this.height = height;
        this.receiver = receiver;
    }

    class ViewThread implements Runnable {
        public void run(){
            Viewer viewer = new Viewer();
            JFrame frame = new JFrame("Simple Stream Viewer");
            frame.setSize(width, height);
            frame.setVisible(true);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.add(viewer);
            while(true) {
                viewer.ViewerInput(receiver.getNextImage());
                frame.repaint();
            }
        }
    }

    public void start(){
        new Thread(new ViewThread()).start();
    }
}
