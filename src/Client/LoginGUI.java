package Client;

import javax.swing.*;

public class LoginGUI extends JFrame {

	protected ChatClient client;
    protected JLabel userLabel;
    protected JTextField userText;
    protected JLabel passwordLabel;
    protected JPasswordField passwordText;
	protected JTextArea errorMsg;
	protected JButton createAccButton;
    protected JButton loginButton;
    protected JButton singupButton;

    
    public LoginGUI(ChatClient client) {

		this.client = client;

		this.setTitle("JavaChatRoom, Connecting to server");
		this.setSize(400, 300);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();
		this.add(panel);
		panel.setLayout(null);

		// user
		this.userLabel = new JLabel("User");
		this.userLabel.setBounds(10, 21, 80, 25);
		panel.add(this.userLabel);

		this.userText = new JTextField();
		this.userText.setBounds(100, 21, 165, 25);
		panel.add(userText);

		// password
		this.passwordLabel = new JLabel("Password");
		this.passwordLabel.setBounds(10, 50, 80, 25);
		panel.add(this.passwordLabel);

		this.passwordText = new JPasswordField();
		this.passwordText.setBounds(100, 50, 165, 25);
		panel.add(this.passwordText);
        
        // error message
		this.errorMsg = new JTextArea();
		this.errorMsg.setBounds(10, 140, 300, 100);
		this.errorMsg.setLineWrap(true);
		this.errorMsg.getWrapStyleWord();
		this.errorMsg.setEditable(false);
        panel.add(this.errorMsg);

		// create account button (visible if client wants to sign up)
		this.createAccButton = new JButton("Create Account");
		this.createAccButton.setBounds(10, 80, 130, 25);
		this.createAccButton.addActionListener(e -> {

			String user = userText.getText();
            @SuppressWarnings("deprecation")
            String password = passwordText.getText();

            if (user.length() == 0 || password.length() == 0)  this.putErrorMsg("ENTER NAME AND PASSWORD");
            else  											   this.client.sendMessage(user + ", " + password, this.client.out, true);
		});
		this.createAccButton.setVisible(false);
		this.createAccButton.invalidate();
		panel.add(this.createAccButton);

		// login button
		this.loginButton = new JButton("Login");
		this.loginButton.setBounds(10, 80, 80, 25);
		this.loginButton.addActionListener(e -> {

            String user = userText.getText();
            @SuppressWarnings("deprecation")
            String password = passwordText.getText();

            if (user.length() == 0 || password.length() == 0)  this.putErrorMsg("ENTER NAME AND PASSWORD");
            else  											   this.client.sendMessage(user + ", " + password, this.client.out, true);
        });
		panel.add(this.loginButton);

		// singup button
        this.singupButton = new JButton("Sign Up");
		this.singupButton.setBounds(10, 110, 80, 25);
		this.singupButton.addActionListener(e -> {

			// say to server that client wants to sing up
			this.client.sendMessage("/singup", this.client.out, true);

			// hide the login and singup button
			this.loginButton.setVisible(false);
			this.loginButton.invalidate();
			
			this.singupButton.setVisible(false);
			this.singupButton.invalidate();

			// show the createAcc button
			this.createAccButton.setVisible(true);
			this.createAccButton.validate();
		});
		panel.add(this.singupButton);

        this.getRootPane().setDefaultButton(loginButton);

		this.setVisible(true);
		this.setLocationRelativeTo(null); // odpre se na sredini
	}

	public void putErrorMsg(String message) {
		this.errorMsg.setText(" ");
		this.errorMsg.append(message);
	}
}
