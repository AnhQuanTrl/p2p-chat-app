package app.servercomm;


import app.utility.Metadata;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FetchWorker extends SwingWorker<Void, String> {
    private Socket socket;
    public void setCancel(Boolean cancel) {
        isCancel = cancel;
    }

    private Boolean isCancel = false;
    private List<ActionListener> actionListeners;
    private JFrame frame;
    public FetchWorker(Socket socket, JFrame frame) {
        this.socket = socket;
        this.frame = frame;
        actionListeners = new ArrayList<>(25);
    }
    public void addActionListeners(ActionListener listener) {
        actionListeners.add(listener);
    }
    @Override
    protected Void doInBackground() throws Exception {
        try (InputStream input = socket.getInputStream(); OutputStream out = socket.getOutputStream()) {
            PrintWriter writer = new PrintWriter(out, true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            while (!isCancel) {
                writer.println("/FETCH " + Metadata.getInstance().getUsername());
                String res = reader.readLine();
                publish(res);
                Thread.sleep(3000);
            }
            writer.println("/LOGOUT " + Metadata.getInstance().getUsername());
            Metadata.getInstance().setUsername("");
        }
        return null;
    }

    @Override
    protected void done() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void process(List<String> chunks) {
        for (String text : chunks) {
            text = text.substring(text.indexOf(" ")+1);
            ActionEvent evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, text);
            for (ActionListener listener : actionListeners) {
                listener.actionPerformed(evt);
            }
        }
    }
}
