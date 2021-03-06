package app.gui;

import app.peer.listener.SocketListener;
import app.servercomm.AddFriendWorker;
import app.servercomm.FetchWorker;
import app.utility.Metadata;
import app.utility.UserIP;
import app.utility.UserListCellRendered;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class PeerSelectionGUI implements Runnable {

    private JFrame frame;
    private SocketListener socketListener;
    private JTextField txtHost;
    private JTextField txtPort;
    private JTextField textField;
    private FetchWorker fetchWorker = null;
    public PeerSelectionGUI() {
    }

    @Override
    public void run() {
        socketListener = new SocketListener();
        socketListener.execute();
        frame = new JFrame();
        frame.setTitle("Peer Selection");
        frame.setBounds(100, 100, 325, 580);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (fetchWorker != null) fetchWorker.setCancel(true);
                socketListener.cancel(true);
                Metadata.getInstance().disposeAllFrame();
                frame.dispose();
            }
        });
        JPanel panel = new JPanel();
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        panel.setLayout(null);

        JPanel panel_1 = new JPanel();
        panel_1.setBounds(10, 10, 286, 347);
        panel_1.setBorder(new TitledBorder(null, "Choose A Person To Connect", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.add(panel_1);

         panel_1.setLayout(null);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(6, 15, 266, 317);
        panel_1.add(scrollPane);
        DefaultListModel<UserIP> listModel = new DefaultListModel<>();
        JList list = new JList(listModel);
        list.setCellRenderer(new UserListCellRendered());
        scrollPane.setViewportView(list);
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.setFont(new Font("Tahoma", Font.PLAIN, 13));
//        sl_panel_1.putConstraint(SpringLayout.NORTH, list, 10, SpringLayout.NORTH, panel_1);
//        sl_panel_1.putConstraint(SpringLayout.WEST, list, 10, SpringLayout.WEST, panel_1);
//        sl_panel_1.putConstraint(SpringLayout.SOUTH, list, 311, SpringLayout.NORTH, panel_1);
//        sl_panel_1.putConstraint(SpringLayout.EAST, list, -6, SpringLayout.EAST, panel_1);
        list.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        JPanel panel_2 = new JPanel();
        panel_2.setBorder(new TitledBorder(null, "Log Out", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_2.setBounds(10, 453, 286, 76);

        panel.add(panel_2);
        SpringLayout sl_panel_2 = new SpringLayout();
        panel_2.setLayout(sl_panel_2);

        JButton btnLogOut = new JButton("Log Out");
        sl_panel_2.putConstraint(SpringLayout.NORTH, btnLogOut, 10, SpringLayout.NORTH, panel_2);
        sl_panel_2.putConstraint(SpringLayout.WEST, btnLogOut, 10, SpringLayout.WEST, panel_2);
        sl_panel_2.putConstraint(SpringLayout.SOUTH, btnLogOut, -6, SpringLayout.SOUTH, panel_2);
        sl_panel_2.putConstraint(SpringLayout.EAST, btnLogOut, -12, SpringLayout.EAST, panel_2);
        btnLogOut.setFont(new Font("Tahoma", Font.PLAIN, 18));
        panel_2.add(btnLogOut);
        JPanel panel_3 = new JPanel();
        panel_3.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "Search For Someone", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        panel_3.setBounds(10, 367, 286, 76);
        panel.add(panel_3);
        panel_3.setLayout(null);
        textField = new JTextField();
        textField.setBounds(10, 20, 266, 46);
        panel_3.add(textField);
        textField.setColumns(10);
        frame.setVisible(true);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    if (e.getClickCount() == 2) {
                        JList list = (JList) e.getSource();
                        int index = list.locationToIndex(e.getPoint());
                        UserIP userIP = listModel.get(index);
                        if (!Metadata.getInstance().containUser(userIP.getUsername())) {
                            Socket socket = null;
                            socket = new Socket(userIP.getInetAddress(), 8989);
                            SwingUtilities.invokeLater(new ChatSessionGUI(socket, userIP.getUsername()));
                        } else {
                            JFrame frame = Metadata.getInstance().findFrame(userIP.getUsername());
                            frame.toFront();
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }
        });
        Socket socket = null;
        try {
            socket = new Socket(Metadata.getInstance().getHostIP(), 7000);
            fetchWorker = new FetchWorker(socket, frame);
            fetchWorker.execute();
            fetchWorker.addActionListeners(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    listModel.clear();
                    String[] userEntries = e.getActionCommand().split(" ");
                    for (String user : userEntries) {
                        String args[] = user.split(",");
                        try {
                            UserIP userIP;
                            if (args.length < 2) {
                                userIP = new UserIP(args[0], null);
                            }
                            else {
                                userIP = new UserIP(args[0], InetAddress.getByName(args[1].substring(1)));
                            }
                            listModel.addElement(userIP);
                        } catch (UnknownHostException ex) {
                            ex.printStackTrace();
                        }
//                        for (String user : users) {
////                            String[] args = user.split(",");
////                            listModel.addElement(new UserIP());
//                        }
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Socket socket = null;
                try {
                    socket = new Socket(Metadata.getInstance().getHostIP(), 7000);
                    AddFriendWorker addFriendWorker = new AddFriendWorker(socket, textField.getText(), frame);
                    addFriendWorker.execute();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
//        searchList.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                try {
//                    if (e.getClickCount() == 2) {
//                        JList list = (JList) e.getSource();
//                        int index = list.locationToIndex(e.getPoint());
//                        String friend = searchListModel.get(index);
//                        Socket socket = new Socket(Metadata.getInstance().getHostIP(), 7000);
//                        AddFriendWorker addFriendWorker = new AddFriendWorker(socket, friend, frame);
//                        addFriendWorker.execute();
//                    }
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                }
//
//            }
//        });
        btnLogOut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fetchWorker != null) fetchWorker.setCancel(true);
                socketListener.cancel(true);
                SwingUtilities.invokeLater(new LoginGUI());
                Metadata.getInstance().disposeAllFrame();
                frame.dispose();
            }
        });
    }
}
//        frame = new JFrame("Connect");
//        frame.setBounds(100, 100, 450, 300);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//        JPanel panel = new JPanel();
//        frame.getContentPane().add(panel);
//        SpringLayout sl_panel = new SpringLayout();
//        panel.setLayout(sl_panel);
//
//        JLabel lblHost = new JLabel("Enter host");
//        sl_panel.putConstraint(SpringLayout.NORTH, lblHost, 60, SpringLayout.NORTH, panel);
//        sl_panel.putConstraint(SpringLayout.WEST, lblHost, 82, SpringLayout.WEST, panel);
//        panel.add(lblHost);
//
//        txtHost = new JTextField();
//        sl_panel.putConstraint(SpringLayout.NORTH, txtHost, 0, SpringLayout.NORTH, lblHost);
//        sl_panel.putConstraint(SpringLayout.WEST, txtHost, 52, SpringLayout.EAST, lblHost);
//        panel.add(txtHost);
//        txtHost.setColumns(10);
//
//        JLabel lblPort = new JLabel("Enter port");
//        sl_panel.putConstraint(SpringLayout.NORTH, lblPort, 64, SpringLayout.SOUTH, lblHost);
//        sl_panel.putConstraint(SpringLayout.EAST, lblPort, 0, SpringLayout.EAST, lblHost);
//        panel.add(lblPort);
//
//        txtPort = new JTextField();
//        sl_panel.putConstraint(SpringLayout.WEST, txtPort, 0, SpringLayout.WEST, txtHost);
//        sl_panel.putConstraint(SpringLayout.SOUTH, txtPort, 0, SpringLayout.SOUTH, lblPort);
//        panel.add(txtPort);
//        txtPort.setColumns(10);
//
//        JButton btnSubmit = new JButton("Submit");
//        btnSubmit.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                try {
//                    newSession();
//                } catch (NumberFormatException ex) {
//                    ex.printStackTrace();
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                }
//            }
//        });
//        sl_panel.putConstraint(SpringLayout.WEST, btnSubmit, 144, SpringLayout.WEST, panel);
//        sl_panel.putConstraint(SpringLayout.SOUTH, btnSubmit, -31, SpringLayout.SOUTH, panel);
//        panel.add(btnSubmit);
//        frame.setVisible(true);
//
