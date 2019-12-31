package whisper.server;

import whisper.entity.Client;
import whisper.entity.Message;
import whisper.util.WhisperUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class Server extends JFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel panel;
	private JLabel jLabel1, jLabel2, jLabel3;
	private JTextField jTextField;
	private JButton start;
	private JButton end;

	//构造服务器端的socket的读写操作
	private InputStream is;
	private OutputStream os;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	// *****

	private HashMap<String, Client> onlines;// 在线人数 string:用户名 client：用户名和socket

	// *****
	private ServerSocket serverSocket;

	//服务器无参函数
	public Server() {
		init();
	}

	/**
	 * 初始化界面
	 */
	public void init() {
		this.setTitle("聊天室服务器");
		this.setSize(400, 600);
		// this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		this.setLocationRelativeTo(null);

		// 新建面板
		panel = new JPanel();
		panel.setLayout(null);

		jLabel1 = new JLabel("端口");
		jLabel1.setFont(new Font("微软雅黑", Font.BOLD, 16));
		jLabel1.setBounds(110, 150, 50, 30);

		jTextField = new JTextField();
		jTextField.setText("8099");
		// 设置字体居中
		jTextField.setHorizontalAlignment(JTextField.CENTER);
		jTextField.setBounds(180, 150, 80, 30);

		// 启动服务器的按钮
		start = new JButton("启动服务器");
		start.setBounds(120, 250, 140, 35);
		start.addActionListener(this);

		panel.add(jLabel1);
		panel.add(jTextField);
		panel.add(start);

		this.add(panel);
		this.setVisible(true);
	}

	// 监听器。实现开启服务器和关闭服务器。
	@Override
	public void actionPerformed(ActionEvent e) {
		if ("启动服务器".equals(e.getActionCommand())) {
			startSever();//开启服务器
		} else if ("关闭服务器".equals(e.getActionCommand())) {
			System.exit(-1);
		}


	}

	/**
	 * 开启服务器
	 */
	private void startSever() {
		try {
			// 启动服务器
			serverSocket = new ServerSocket(8099);//设置端口
			System.out.println("服务器开启");

			new ServerThread().start();//避免阻塞，开启服务器线程

			onlines = new HashMap<String, Client>();// 初始化在线人数

			// 更新界面
			panel.removeAll();
			panel.setBackground(Color.green);
			jLabel2 = new JLabel("端口8099正在运行");
			jLabel2.setBounds(150, 100, 300, 50);
			jLabel3 = new JLabel("当前在线人数" + onlines.size());
			jLabel3.setBounds(150, 70, 300, 50);
			end = new JButton("关闭服务器");
			end.addActionListener(this);
			end.setBounds(120, 250, 140, 35);
			// 添加组件
			panel.add(jLabel2);
			panel.add(jLabel3);
			panel.add(end);
			// 更新界面
			panel.updateUI();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 服务器线程，为了防止阻塞
	 */
	class ServerThread extends Thread {
		Socket socket;
		//无参函数
		public ServerThread(){};
		//有参构造
		public ServerThread(Socket socket) {
			this.socket = socket;
		}
		@Override
		public void run() {
			while (true) {
				Socket socket;
				try {
					socket = serverSocket.accept();
					System.out.println("谁连接上" + socket);

					//为每个请求连接的客户端创建一个线程
					new clientThread(socket).start();
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("捕获客户端线程创建异常");
				}
			}
		}
	}

	/**
	 * 请求连接的客户端线程
	 */
	class clientThread extends Thread {
		Socket socket;
		@Override
		public void run() {
			while (true){
				try {
					is = socket.getInputStream();
					ois = new ObjectInputStream(is);
					Message clientMessage = (Message) ois.readObject();
					System.out.println("来自客户端的MSG："+clientMessage);
					// 根据type的值进行操作
					switch (clientMessage.getType()) {
						case 0://上下线更新
							//1.在线人数更新，2.向所有在线用户发送上线消息
							Client client = new Client();
							client.setName(clientMessage.getName());
							client.setSocket(socket);
							//人数加一
							onlines.put(clientMessage.getName(), client);
							jLabel3.setText("在线人数："+onlines.size());
							//2.向所有用户发送信息
							Message message = new Message();
							message.setType(0);
							message.setName(clientMessage.getName());
							message.setInfo("系统提示：\n"+WhisperUtil.getTimer()+"【"+clientMessage.getName()+"】上线了\n");
							HashSet<String> set = new HashSet<String>();
							set.addAll(onlines.keySet());//把所有在线的用户的用户名加入到set里面
							message.setClients(set);
							//信息封装好之后发送给所有在线用户
							sendAllOnlines(message);
							break;

						case 1://聊天
							//转发消息给对应的人
							sendSelect(clientMessage);

							break;
						case -1://下线
							//向客户端发送下线信息
							Message message1 = new Message();
							message1.setType(-1);
							ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
							oos.writeObject(message1);//叫客户端下线
							oos.flush();

							onlines.remove(clientMessage.getName());
							jLabel3.setText("在线人数："+onlines.size());

							Message message2 = new Message();
							message2.setType(0);
							message2.setInfo("系统提示:\n"+WhisperUtil.getTimer()+"["+clientMessage.getName()+"]下线了\n");
							HashSet<String> set1 = new HashSet<String>();
							set1.addAll(onlines.keySet());
							message2.setClients(set1);//发送更新完的在线人数
							sendAllOnlines(message2);

							return;
					}
					System.out.println(clientMessage.toString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}


		}
		//无参构造
		public clientThread() {
			// TODO Auto-generated constructor stub
		}
		//有参socket构造
		public clientThread(Socket socket) {

			this.socket = socket;
		}

	}

	/**
	 * 向选中的人发送信息
	 * @param message
	 */
	private void sendSelect(Message message) {
		Collection<Client> clients = onlines.values();//所有在线人数
		Iterator<Client> iterator = clients.iterator();
		HashSet<String> selectName = message.getClients();//选中的
		while (iterator.hasNext()){
			Client client = iterator.next();
			String name = client.getName();
			Iterator<String> stringIterator = selectName.iterator();
			while (stringIterator.hasNext()){
				String selectname = stringIterator.next();
				if (selectname.equals(name)){
					try {
						os = client.getSocket().getOutputStream();
						oos = new ObjectOutputStream(os);
						oos.writeObject(message);
						oos.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			/*if(selectName.toString().contains(name)){//3被包含在123
				System.out.println("选中的人的列表:"+selectName.toString());

			}*/
		}
	}

	/**
	 * 向所有在线用户发送信息
	 * @param message
	 */
	private void sendAllOnlines(Message message) {
		Collection<Client> clients = onlines.values();
		Iterator<Client> iterator = clients.iterator();
		while (iterator.hasNext()){//遍历
			Client client = iterator.next();
			try {
				os = client.getSocket().getOutputStream();
				oos = new ObjectOutputStream(os);
				oos.writeObject(message);
				oos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		new Server();

	}

}
