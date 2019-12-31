package whisper.login;

import whisper.util.ImageUtil;
import whisper.util.JDBCUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

public class Register extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel user;
	private JLabel pass;
	private JTextField userName;
	private JPasswordField password;
	private JLabel welcome;
	private JButton login;
	private JButton register;
	private ImageUtil imageUtil;
	private JFrame jFrame;

	public Register() {

		init();
	}

	private void init() {
		jFrame = new JFrame("登陆页面");
		jFrame.setTitle("轻语聊天室");
		jFrame.setSize(500, 300);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setResizable(false);
		jFrame.setLocationRelativeTo(null);
		this.setLayout(null);

		// 页面布局
		imageUtil = new ImageUtil("images/whisper.png");
		imageUtil.setBounds(200, 0, 300, 300);

		welcome = new JLabel("欢迎注册");
		welcome.setFont(new Font("楷体", Font.BOLD, 36));
		welcome.setBounds(120, 5, 200, 100);

		user = new JLabel("用户");
		user.setBounds(60, 100, 50, 30);

		pass = new JLabel("密码");
		pass.setBounds(60, 140, 50, 30);

		userName = new JTextField(10);
		userName.setBounds(120, 100, 140, 30);

		password = new JPasswordField(10);
		password.setBounds(120, 140, 140, 30);

		// 按钮
		login = new JButton("注册");
		login.setBackground(Color.green);
		login.setBounds(280, 210, 80, 30);
		login.addActionListener(this);

		register = new JButton("取消");
		register.setBackground(Color.orange);
		register.setBounds(380, 210, 80, 30);
		register.addActionListener(this);

		this.add(welcome);
		this.add(user);
		this.add(userName);
		this.add(pass);
		this.add(password);
		this.add(login);
		this.add(register);
		this.add(imageUtil);

		jFrame.add(this);
		jFrame.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if ("注册".equals(e.getActionCommand())) {
			//获取账号密码
			String name;
			char[] pass;
			name = userName.getText();
			pass = password.getPassword();
			//转成字符数组
			String passNew = new String(pass);
			//逻辑判断，判空
			if ("".equals(name)) {
				JOptionPane.showMessageDialog(null, "用户名不能为空");
				
			}else if ("".equals(passNew)) {
				JOptionPane.showMessageDialog(null, "密码不能为空");
			}else {
				//1.用户名已存在的判断[查询数据库]
				//2.不存在则成功注册[name,passNew插入数据库]
				String sqlCheck = "select * from user where name = ?";
				List<Map<String, Object>> list = JDBCUtil.queryForList(sqlCheck,name);
				if (list!=null) {//用户存在
					JOptionPane.showMessageDialog(null, "用户已存在");
				} else {
					String sqlAdd = "insert into user values (?,?,?)";
					int count = JDBCUtil.update(sqlAdd, name,passNew,"offline");
					if (count!=0) {
						JOptionPane.showMessageDialog(null, "注册成功");
						new Login();
						//注册页面消失
						jFrame.setVisible(false);
					}

				}
			}
		} else if("取消".equals(e.getActionCommand())){
			//注册页面消失
			jFrame.setVisible(false);
			//跳转登陆界面
			new Login();
		}
		
		
	}

	public static void main(String[] args) {
		new Register();
	}

}
