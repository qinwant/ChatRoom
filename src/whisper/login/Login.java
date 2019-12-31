package whisper.login;

import whisper.client.ChatRoom;
import whisper.util.ImageUtil;
import whisper.util.JDBCUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;


public class Login extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel user;//用户名标签
	private JLabel pass;//密码标签
	private JTextField userName;//用户名文本框
	private JPasswordField password;//密码文本框
	private JLabel welcome;//欢迎登陆
	private JButton login;//登入按钮
	private JButton register;//注册按钮
	private ImageUtil imageUtil;
	private JFrame jFrame;


	public Login() {
		// 初始化
		init();

	}

	private void init() {
		jFrame = new JFrame("登陆页面");
		jFrame.setTitle("轻语聊天室");
		jFrame.setSize(500, 300);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);//监听事件发生后，当前窗体隐藏释放
		jFrame.setResizable(false);//设置此窗体是否可由用户调整大小。
		jFrame.setLocationRelativeTo(null);//设置居中
		this.setLayout(null);

		imageUtil = new ImageUtil("images/whisper.png");
		imageUtil.setBounds(200, 0, 300, 300);

		// 页面布局
		welcome = new JLabel("欢迎登陆");
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
		login = new JButton("登录");
		login.setBackground(Color.green);
		login.setBounds(280, 210, 80, 30);
		login.addActionListener(this);

		register = new JButton("注册");
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
		if ("登录".equals(e.getActionCommand())) {//getActionCommand()方法依赖于按钮上的字符串
			String name = userName.getText();
			char[] pass = password.getPassword();
			String passNew = new String(pass);//字符数组转字符串
			if ("".equals(name)) {
				JOptionPane.showMessageDialog(null, "用户名不能为空");
				
			}else if ("".equals(passNew)) {
				JOptionPane.showMessageDialog(null, "密码不能为空");
			}else {
				String sql = "select * from user where name = ? and password = ? ";
				List<Map<String, Object>> list = JDBCUtil.queryForList(sql, name,passNew);
				if (list!=null) {
					//验证是否重复登陆
					if (!isLoginRepeat(sql,name,passNew)){
						//连接服务器
						Socket socket =null;
						try {
							socket = new Socket("127.0.0.1",8099);
						} catch (UnknownHostException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						//登录成功的情况
						JOptionPane.showMessageDialog(null, "登录成功");
						String sqlupdate = "update user set state = ? where name = ?";
						int count = JDBCUtil.update(sqlupdate,"online",name);
						System.out.println("是否成功："+count);
						new ChatRoom(name,socket);//进入聊天界面---->传递两个参数给服务器
						jFrame.setVisible(false);//关闭登陆界面
					}else {
						JOptionPane.showMessageDialog(null, "您已在线，请勿重复登陆！");
					}
				}else {
					//登录不成功的情况
					JOptionPane.showMessageDialog(null, "用户名或密码错误");
					//清空内容
					userName.setText(null);
					password.setText(null);
				}
			}
			
		}else if ("注册".equals(e.getActionCommand())) {
			jFrame.setVisible(false);
			new Register();//跳转到注册界面
		}
		
	}

	/**
	 * 验证用户是否重复登陆
	 * @param sql
	 * @param name
	 * @param passNew
	 */
	private boolean isLoginRepeat(String sql, String name, String passNew) {
		String sqlNew = sql + " and state = ?";
		List<Map<String, Object>> list = JDBCUtil.queryForList(sqlNew, name,passNew,"online");
		if (list!=null){
			return true;
		}else {
			return false;
		}
	}

	public static void main(String[] args) {
		new Login();
	}

}
