package client;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.html.*;

import java.util.ArrayList;
import java.util.Arrays;

public class ClientGui extends Thread{

  final JTextPane jtextFilDiscu = new JTextPane();
  final JTextPane jtextListUsers = new JTextPane();
  final JTextField jtextInputChat = new JTextField();
  private String oldMsg = "";
  private Thread read;
  private String serverName;
  private int PORT;
  private String name;
  BufferedReader input;
  PrintWriter output;
  Socket server;
  public String namenick;

  public ClientGui() {
    this.serverName = "localhost";
    this.PORT = 8790;
    this.name = "nickname";

    String fontfamily = "Arial, sans-serif";
    Font font = new Font(fontfamily, Font.PLAIN, 15);

    final JFrame jfr = new JFrame("Chat");
    jfr.getContentPane().setLayout(null);
    jfr.setSize(700, 500);
    jfr.setResizable(false);
    //jfr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    jtextFilDiscu.setBounds(25, 25, 490, 320);
    jtextFilDiscu.setFont(font);
    jtextFilDiscu.setMargin(new Insets(6, 6, 6, 6));
    jtextFilDiscu.setEditable(false);
    JScrollPane jtextFilDiscuSP = new JScrollPane(jtextFilDiscu);
    jtextFilDiscuSP.setBounds(25, 25, 490, 320);

    jtextFilDiscu.setContentType("text/html");
    jtextFilDiscu.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

    jtextListUsers.setBounds(520, 25, 156, 320);
    jtextListUsers.setEditable(true);
    jtextListUsers.setFont(font);
    jtextListUsers.setMargin(new Insets(6, 6, 6, 6));
    jtextListUsers.setEditable(false);
    JScrollPane jsplistuser = new JScrollPane(jtextListUsers);
    jsplistuser.setBounds(520, 25, 156, 320);

    jtextListUsers.setContentType("text/html");
    jtextListUsers.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

    jtextInputChat.setBounds(0, 350, 400, 50);
    jtextInputChat.setFont(font);
    jtextInputChat.setMargin(new Insets(6, 6, 6, 6));
    
    final JScrollPane jtextInputChatSP = new JScrollPane(jtextInputChat);
    jtextInputChatSP.setBounds(25, 350, 650, 50);

    final JButton btnSend = new JButton("Send");
    btnSend.setFont(font);
    btnSend.setBounds(575, 410, 100, 35);
    
    final JButton btnFile = new JButton("File");
    btnFile.setFont(font);
    btnFile.setBounds(300, 410, 100, 35);

    final JButton btnDisconnect = new JButton("Disconnect");
    btnDisconnect.setFont(font);
    btnDisconnect.setBounds(25, 410, 130, 35);

    jtextInputChat.addKeyListener(new KeyAdapter() {
      // send message on Enter
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          sendMessage();
        }

        // Get last message typed
        if (e.getKeyCode() == KeyEvent.VK_UP) {
          String currentMessage = jtextInputChat.getText().trim();
          jtextInputChat.setText(oldMsg);
          oldMsg = currentMessage;
        }

//        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
//          String currentMessage = jtextInputChat.getText().trim();
//          jtextInputChat.setText(oldMsg);
//          oldMsg = currentMessage;
//        }
      }
    });

    // Click on send button
    btnSend.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        sendMessage();
      }
    });

    // Connection view
    final JTextField jtfName = new JTextField(this.name);
    final JTextField jtfport = new JTextField(Integer.toString(this.PORT));
    final JTextField jtfAddr = new JTextField(this.serverName);
    final JButton jcbtn = new JButton("Connect");

    // check if those field are not empty
    jtfName.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, jcbtn));
    jtfport.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, jcbtn));
    jtfAddr.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, jcbtn));

    jcbtn.setFont(font);
    jtfAddr.setBounds(25, 380, 135, 40);
    jtfName.setBounds(375, 380, 135, 40);
    jtfport.setBounds(200, 380, 135, 40);
    jcbtn.setBounds(575, 380, 100, 40);

    jtextFilDiscu.setBackground(Color.LIGHT_GRAY);
    jtextListUsers.setBackground(Color.LIGHT_GRAY);

    jfr.add(jcbtn);
    jfr.add(jtextFilDiscuSP);
    jfr.add(jsplistuser);
    jfr.add(jtfName);
    jfr.add(jtfport);
    jfr.add(jtfAddr);
    jfr.setVisible(true);

    appendToPane(jtextFilDiscu, "<h4>Commands can be used in chat:</h4>"
        +"<ul>"
        +"<li><b>@nickname</b>Send private message to @nickname</li>"
        +"<li><b>:)</b> Smile</li>"
        +"<li><b>:D</b> LOL</li>"
        +"<li><b>;)</b> Blink</li>"
        +"<li><b>:(</b> Sad</li>"
        +"<li><b>:o</b> WOW</li>"
        +"<li><b>-_-</b> Really ??</li>"
        +"</ul><br/>");

    // On connect
    jcbtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        try {
          namenick = name = jtfName.getText();
          String port = jtfport.getText();
          serverName = jtfAddr.getText();
          PORT = Integer.parseInt(port);

          appendToPane(jtextFilDiscu, "<span>Connecting to " + serverName + " on port " + PORT + "...</span>");
          server = new Socket(serverName, PORT);

          appendToPane(jtextFilDiscu, "<span>Connected to " +
              server.getRemoteSocketAddress()+"</span>");

          input = new BufferedReader(new InputStreamReader(server.getInputStream()));
          output = new PrintWriter(server.getOutputStream(), true);

          // send nickname to server
          output.println(name);

          // create new Read Thread
          read = new Read();
          read.start();
          jfr.remove(jtfName);
          jfr.remove(jtfport);
          jfr.remove(jtfAddr);
          jfr.remove(jcbtn);
          jfr.add(btnSend);
          jfr.add(btnFile);
          jfr.add(jtextInputChatSP);
          jfr.add(btnDisconnect);
          jfr.revalidate();
          jfr.repaint();
          jtextFilDiscu.setBackground(Color.WHITE);
          jtextListUsers.setBackground(Color.WHITE);
        } catch (Exception ex) {
          appendToPane(jtextFilDiscu, "<span>Could not connect to Server</span>");
          JOptionPane.showMessageDialog(jfr, ex.getMessage());
        }
      }

    });
    
    btnFile.addActionListener(new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser choose = new JFileChooser();
			int option = choose.showOpenDialog(jfr);
			if(option == JFileChooser.APPROVE_OPTION) {
				File selectedFile = choose.getSelectedFile();
				
				output.println("Sent a file: " + selectedFile.getName());
				
				output.println("FI%LE " + selectedFile.getAbsolutePath());
			}
			
		}
	});

    btnDisconnect.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent ae) {
        jfr.add(jtfName);
        jfr.add(jtfport);
        jfr.add(jtfAddr);
        jfr.add(jcbtn);
        jfr.remove(btnSend);
        jfr.remove(jtextInputChatSP);
        jfr.remove(btnDisconnect);
        jfr.remove(btnFile);
        jfr.revalidate();
        jfr.repaint();
        read.interrupt();
        jtextListUsers.setText(null);
        jtextFilDiscu.setBackground(Color.LIGHT_GRAY);
        jtextListUsers.setBackground(Color.LIGHT_GRAY);
        appendToPane(jtextFilDiscu, "<span>Connection closed.</span>");
        output.close();
      }
    });
    

  }

  // check if if all field are not empty
//  public class TextListener implements DocumentListener{
//    JTextField jtf1;
//    JTextField jtf2;
//    JTextField jtf3;
//    JButton jcbtn;
//
//    public TextListener(JTextField jtf1, JTextField jtf2, JTextField jtf3, JButton jcbtn){
//      this.jtf1 = jtf1;
//      this.jtf2 = jtf2;
//      this.jtf3 = jtf3;
//      this.jcbtn = jcbtn;
//    }
//
//    public void changedUpdate(DocumentEvent e) {}
//
//    public void removeUpdate(DocumentEvent e) {
//      if(jtf1.getText().trim().equals("") ||
//          jtf2.getText().trim().equals("") ||
//          jtf3.getText().trim().equals("")
//          ){
//        jcbtn.setEnabled(false);
//      }else{
//        jcbtn.setEnabled(true);
//      }
//    }
//    public void insertUpdate(DocumentEvent e) {
//      if(jtf1.getText().trim().equals("") ||
//          jtf2.getText().trim().equals("") ||
//          jtf3.getText().trim().equals("")
//          ){
//        jcbtn.setEnabled(false);
//      }else{
//        jcbtn.setEnabled(true);
//      }
//    }
//
//  }

  public void sendMessage() {
    try {
      String message = jtextInputChat.getText().trim();
      if (message.equals("")) {
        return;
      }
      this.oldMsg = message;
      output.println(message);
      jtextInputChat.requestFocus();
      jtextInputChat.setText(null);
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(null, ex.getMessage());
      System.exit(0);
    }
  }

  public static void main(String[] args) throws Exception {
    ClientGui client = new ClientGui();
  }

  // read new incoming messages
  class Read extends Thread {
    public void run() {
      String message;
      while(!Thread.currentThread().isInterrupted()){
        try {
          message = input.readLine();
          if(message != null){
            if (message.charAt(0) == '[') {
              message = message.substring(1, message.length()-1);
              ArrayList<String> ListUser = new ArrayList<String>(
                  Arrays.asList(message.split(", "))
                  );
              jtextListUsers.setText(null);
              for (String user : ListUser) {
                appendToPane(jtextListUsers, "@" + user);
              }
            } else{
              if(message.startsWith("<b>Welcome</b>")){
//                System.out.println("!dungba!");
              }else {
//                System.out.println(message);
                String[] messfile, nickname;
                nickname = message.split("</span>");
                nickname = nickname[0].split(">");
                messfile = message.split(": ");
                messfile = messfile[1].split("</sinpan>");
//                System.out.println(messfile[0]);
//                System.out.println(nickname[2]);
                if(messfile[0].startsWith("FI%LE")){
                  String[] name = messfile[0].substring(6).split("/");
                  String mess = nickname[2] + " send "+ name[name.length-1] +"\n Do you want to download it ?";
//                  System.out.println(mess);
//                  System.out.println(namenick);
                  if(!nickname[2].equals(namenick)){
                    if (JOptionPane.showConfirmDialog(new JFrame(),
                            mess, "Download",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    	
                    	JFileChooser choose = new JFileChooser();
                    	int option = choose.showSaveDialog(jtextFilDiscu);
                    	
   
            				
                      InputStream inStream = null;
                      OutputStream outStream = null;

                      try{

                        File afile = new File(messfile[0].substring(6));
                        File bfile = new File(name[name.length-1]);

                        inStream = new FileInputStream(afile);
                        outStream = new FileOutputStream(bfile);

                        byte[] buffer = new byte[1024];

                        int length;
                        //copy the file content in bytes
                        while ((length = inStream.read(buffer)) > 0){
                        	
                        	if(option == JFileChooser.APPROVE_OPTION) {
                        		outStream.write(buffer, 0, length);
                        		File fileToSave = choose.getSelectedFile();
                			}

//                         outStream.write(buffer, 0, length);

                        }

                        inStream.close();
                        outStream.close();

                        message = "File download complete";
                      }catch(IOException e){
                        e.printStackTrace();
                      }
                    }else{
                      message = "";
                    }
                  }else{
                    message = "File upload complete";
                  }
                }
              }
              appendToPane(jtextFilDiscu, message);
            }
          }
        }
        catch (IOException ex) {
          System.err.println("Failed to parse incoming message");
        }
      }
    }
  }

  private void appendToPane(JTextPane tp, String msg){
    HTMLDocument doc = (HTMLDocument)tp.getDocument();
    HTMLEditorKit editorKit = (HTMLEditorKit)tp.getEditorKit();
    try {
      editorKit.insertHTML(doc, doc.getLength(), msg, 0, 0, null);
      tp.setCaretPosition(doc.getLength());
    } catch(Exception e){
      e.printStackTrace();
    }
  }
}