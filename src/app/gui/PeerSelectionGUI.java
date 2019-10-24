package app.gui;

import app.peer.preprocess.Server;
import app.servercomm.AddFriendWorker;
import app.servercomm.FetchWorker;
import app.servercomm.FriendQueryWorker;
import app.utility.Metadata;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.Socket;

public class PeerSelectionGUI implements Runnable {

    private JFrame frame;
    private Server server;
    private JTextField txtHost;
    private JTextField txtPort;
    private JTextField textField;
    private FetchWorker fetchWorker = null;
    public PeerSelectionGUI() {
    }

    @Override
    public void run() {
        server = new Server();
        server.execute();
        frame = new JFrame();
        frame.setTitle("Peer Selection");
        frame.setBounds(100, 100, 677, 492);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (fetchWorker != null) fetchWorker.setCancel(true);
            }
        });
        JPanel panel = new JPanel();
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        panel.setLayout(null);

        JPanel panel_1 = new JPanel();
        panel_1.setBounds(10, 10, 286, 347);
        panel_1.setBorder(new TitledBorder(null, "Choose A Person To Connect", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel.add(panel_1);
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.setFont(new Font("Tahoma", Font.PLAIN, 13));
        list.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        panel_1.add(list);
        panel_1.setLayout(null);

        JPanel panel_2 = new JPanel();
        panel_2.setBorder(new TitledBorder(null, "Log Out", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_2.setBounds(10, 368, 286, 76);
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
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 20, 266, 317);
        panel_1.add(scrollPane);
        DefaultListModel<String> searchListModel = new DefaultListModel<>();
        JList searchList = new JList(searchListModel);
        scrollPane.setViewportView(searchList);
        searchList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        searchList.setFont(new Font("Tahoma", Font.PLAIN, 13));
        searchList.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        JPanel panel_3 = new JPanel();
        panel_3.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "Search For Someone", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        panel_3.setBounds(300, 10, 353, 76);
        panel.add(panel_3);
        panel_3.setLayout(null);
        textField = new JTextField();
        textField.setBounds(10, 20, 333, 46);
        panel_3.add(textField);
        textField.setColumns(10);

        JPanel panel_4 = new JPanel();
        panel_4.setBorder(new TitledBorder(null, "Search Result", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_4.setBounds(300, 96, 353, 348);
        panel.add(panel_4);
        panel_4.setLayout(null);
        JScrollPane scrollPane_1 = new JScrollPane();
        scrollPane_1.setBounds(10, 21, 333, 317);
        panel_4.add(scrollPane_1);
        JTextArea listResult = new JTextArea();
        scrollPane_1.setViewportView(listResult);
        frame.setVisible(true);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    if (e.getClickCount() == 2) {
                        JList list = (JList) e.getSource();
                        int index = list.locationToIndex(e.getPoint());
                        String user = listModel.get(index);
                        String[] args = user.split(",");
                        if (!Metadata.getInstance().containUser(args[0])) {
                            Socket socket = null;
                            socket = new Socket(args[1].substring(1), 8989);
                            SwingUtilities.invokeLater(new ChatSessionGUI(socket, args[0]));
                        } else {
                            JFrame frame = Metadata.getInstance().findFrame(args[0]);
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
            socket = new Socket("192.168.1.167", 7000);
            fetchWorker = new FetchWorker(socket, frame);
            fetchWorker.execute();
            fetchWorker.addActionListeners(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    listModel.clear();
                    String[] users = e.getActionCommand().split(" ");
                    for (String user : users) {
                        listModel.addElement(user);
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
                    socket = new Socket("192.168.1.167", 7000);
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
//                        Socket socket = new Socket("192.168.1.167", 7000);
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
                server.cancel(true);
                SwingUtilities.invokeLater(new LoginGUI());
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
