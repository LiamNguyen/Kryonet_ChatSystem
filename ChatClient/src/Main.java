import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import packets.*;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Main implements ActionListener{

    private final JFrame frame = new JFrame();
    private final JTextField textField = new JTextField(25);
    private final JButton sendButton = new JButton("Send");
    private final JLabel lbl = new JLabel();
    private JPanel topPanel = new JPanel();
    private JTextPane tPane = new JTextPane();

    private Client client;
    private String username;

    public Main() {
        client = new Client();
        client.start();
        try {
            client.connect(5000, "127.0.0.1", 23110, 23111);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Cannot connect to server");
            return;
        }

        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof Packet) {
                    if (object instanceof PacketClientConnected) {
                        PacketClientConnected connectedPacket = (PacketClientConnected) object;
                        appendToPane(tPane, connectedPacket.connectedClientName + " has connected to chat server\n", Color.GREEN);
                        System.out.println("test");
                    } else if (object instanceof PacketClientDisconnected) {
                        PacketClientDisconnected disconnectedPacket = (PacketClientDisconnected) object;
                        appendToPane(tPane, disconnectedPacket.disconnectedClientName + " has disconnected from chat server\n", Color.RED);
                    } else if (object instanceof PacketChatDialog) {
                        PacketChatDialog chatPacket = (PacketChatDialog) object;
                        appendToPane(tPane, chatPacket.chatClientName + ": " + chatPacket.chatMessage + "\n", Color.BLACK);
                    }
                }
            }
        });

        client.getKryo().register(Packet.class);
        client.getKryo().register(PacketConnect.class);
        client.getKryo().register(PacketClientConnected.class);
        client.getKryo().register(PacketClientDisconnected.class);
        client.getKryo().register(PacketChatDialog.class);

        while(true) {
            username = JOptionPane.showInputDialog("Enter your username: ");
            if (!username.equalsIgnoreCase("")) {
                break;
            }
            JOptionPane.showMessageDialog(null, "Please enter your username");
        }

        PacketConnect connectPacket = new PacketConnect();
        connectPacket.username = username;
        client.sendTCP(connectPacket);

        frame.setTitle(username + "'s Chat Dialog");
        frame.setSize(450, 375);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        sendButton.addActionListener(this);
        JScrollPane areaScrollPane = new JScrollPane(tPane);
        areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        areaScrollPane.setPreferredSize(new Dimension(425, 275));

        lbl.setText("Conversation is created at " + getDateTimeNow());
        topPanel.add(lbl);
        topPanel.add(areaScrollPane);
        topPanel.add(textField);
        topPanel.add(sendButton);

        frame.add(topPanel);
        frame.setVisible(true);

        textField.requestFocus();
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String message = textField.getText();
                    appendToPane(tPane, username + ": " + message + "\n", Color.BLACK);
                    textField.setText("");
                    PacketChatDialog chatPacket = new PacketChatDialog();
                    chatPacket.chatClientName = username;
                    chatPacket.chatMessage = message;
                    client.sendTCP(chatPacket);
                }
            }
        });
    }

    public static void main(String[] args) {
        new Main();
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        String message = textField.getText();
        appendToPane(tPane, username + ": " + message + "\n", Color.BLACK);
        textField.setText("");
        PacketChatDialog chatPacket = new PacketChatDialog();
        chatPacket.chatClientName = username;
        chatPacket.chatMessage = message;
        client.sendTCP(chatPacket);
    }

    private void appendToPane(JTextPane tp, String msg, Color c)
    {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        int len = tp.getDocument().getLength();
        tp.setCaretPosition(len);
        tp.setCharacterAttributes(aset, false);
        tp.replaceSelection(msg);
    }

    public String getDateTimeNow() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
}

