package whisper.client;

import whisper.entity.Message;
import whisper.util.ImageUtil;
import whisper.util.JDBCUtil;
import whisper.util.WhisperUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.*;

public class ChatRoom extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JFrame jFrame;
	private ImageUtil imageUtil;
	// 发送框，信息显示框，好友列表框的滚动条
	private JScrollPane sendMsg, reciveMsg, friendList;
	// 信息接收框、信息发送框
	private JTextArea reciveInfo, sendInfo;
	// 显示指定数组中的元素
	private JList<String> list;
	// 用于显示当前用户
	private JLabel currentUser;
	// 发送和关闭按钮
	private JButton send, close;
	// 用于显示当前用户的名字
	private String name;
	
	//由客户端传过来的socket决定
	private Socket socket;

	//构造对socket的读写操作
	private InputStream is;
	private OutputStream os;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;

	private Vector onlines;//在线人数
	//定义声音
	private File fileOnline,fileOffline;
	private URL urlOnline,urlOffline;
	private AudioClip soundOnline,soundOffline;


	private AbstractListModel abstractListModel;

	/**
	 * 无参函数
	 */
	public ChatRoom() {
		init();
	}

	/**
	 * 有参构造
	 * @param name 用户名
	 * @param socket 客户端socket
	 */
	public ChatRoom(String name,Socket socket) {
		this.name = name;
		this.socket = socket;//传参
		onlines = new Vector();//在线人数
		init();

		//封装消息类
		Message message = new Message();
		message.setType(0);//设置上下线更新
		message.setTimer(WhisperUtil.getTimer());//时间
		message.setName(name);//姓名
		//发送消息给服务器
		sendToServer(message);

		//声音提示
		try {
			fileOnline = new File("sounds/叮.wav");
			fileOffline = new File("sounds/呃欧.wav");
			urlOnline = fileOnline.toURI().toURL();
			urlOffline = fileOffline.toURI().toURL();
			soundOnline = Applet.newAudioClip(urlOnline);
			soundOffline = Applet.newAudioClip(urlOffline);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		new ClientThread().start();
		jFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				int count = JOptionPane.showConfirmDialog(null,"您确定要离开吗?");
				if(count==JOptionPane.YES_OPTION){
					Message message = new Message();
					message.setType(-1);
					message.setName(name);
					message.setTimer(WhisperUtil.getTimer());
					sendToServer(message);
					String sql = "update user set state = ? where name = ?";
					JDBCUtil.update(sql,"offline",name);
				}
			}
		});
	}
	class ClientThread extends Thread{
		@Override
		public void run() {
			while (true){
				try {
					is = socket.getInputStream();
					ois = new ObjectInputStream(is);
					Message serverMessage = (Message) ois.readObject();
					switch (serverMessage.getType()){
						case 0:
							onlines.clear();//避免重复
							reciveInfo.append(serverMessage.getInfo());//聊天框上线提醒
							//显示在线用户列表
							HashSet<String> clients = serverMessage.getClients();
							Iterator<String> iterator = clients.iterator();
							while (iterator.hasNext()){
								String username = iterator.next();
								if(name.equals(username)){
									username = username+"（我）";
									onlines.add(username);
								}else {
									onlines.add(username);
								}

							}
							abstractListModel = new AbstractListModel() {
								@Override
								public int getSize() {
									return onlines.size();
								}

								@Override
								public Object getElementAt(int index) {
									return onlines.get(index);
								}
							};
							list.setModel(abstractListModel);//将model加到list显示出来
							//reciveInfo.append(serverMessage.getInfo());

							soundOnline.play();//上线提醒
							break;
						case 1:
							reciveInfo.append(serverMessage.getTimer()+" "+serverMessage.getName()+"对我说："+serverMessage.getInfo()+"\n");

							//System.exit(0);
							break;
						case -1:

							soundOffline.play();
							this.sleep(1000);
							System.exit(-1);
							break;
					}

				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}
	}
	//发送消息给服务器
	private void sendToServer(Message message) {
		try {
			//向socket写入message信息供服务器读取
			os = socket.getOutputStream();
			oos = new ObjectOutputStream(os);
			oos.writeObject(message);
			oos.flush();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 初始化聊天室布局
	 */
	private void init() {
		jFrame = new JFrame("轻语聊天室");
		jFrame.setSize(800, 600);
		jFrame.setDefaultCloseOperation(jFrame.DO_NOTHING_ON_CLOSE);
		jFrame.setResizable(false);
		jFrame.setLocationRelativeTo(null);
		this.setLayout(null);

		imageUtil = new ImageUtil("images/bc.jpg");
		imageUtil.setBounds(0, 0, 800, 600);

		// 页面布局
		// 接收框的布局
		reciveInfo = new JTextArea();
		reciveInfo.setEditable(false);
		// 自动换行
		reciveInfo.setLineWrap(true);
		reciveInfo.setFont(new Font("宋体", Font.BOLD, 15));
		// 添加到滚动条里
		reciveMsg = new JScrollPane();
		reciveMsg.setBounds(30, 10, 520, 380);
		reciveMsg.setViewportView(reciveInfo);

		// 发送框布局
		sendInfo = new JTextArea();
		sendInfo.setLineWrap(false);
		sendInfo.setFont(new Font("宋体", Font.BOLD, 15));
		// 添加到滚动条里
		sendMsg = new JScrollPane(sendInfo);
		sendMsg.setBounds(30, 400, 520, 120);
		// 发送按钮
		send = new JButton("发送");
		send.setBackground(Color.green);
		send.setBounds(450, 520, 100, 30);
		send.addActionListener(this);
		// 关闭按钮
		close = new JButton("关闭");
		close.setBackground(Color.yellow);
		close.setBounds(345, 520, 100, 30);
		close.addActionListener(this);

		// 当前用户的名字
		currentUser = new JLabel("你好," + name + "!");
		// 设置字体水平居中
		currentUser.setHorizontalAlignment(JLabel.CENTER);
		currentUser.setFont(new Font("楷体", Font.BOLD, 20));
		currentUser.setBounds(560, 10, 230, 50);

		// 好友列表
		list = new JList<>();
		// 设置选择模式。
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);// 表示可以选择不相邻的几项
		list.setFont(new Font("华文行楷", Font.BOLD, 18));
		list.setBackground(Color.white);
		// 设置虚边框样式
		Border etch = BorderFactory.createEtchedBorder();
		list.setBorder(BorderFactory.createTitledBorder(etch, "在线用户:", TitledBorder.LEADING, TitledBorder.TOP,
				new Font("楷体", Font.BOLD, 20), Color.green));

		friendList = new JScrollPane();
		friendList.setViewportView(list);
		friendList.setBounds(560, 60, 230, 330);

		this.add(send);
		this.add(close);
		this.add(currentUser);
		this.add(reciveMsg);
		this.add(sendMsg);
		this.add(friendList);
		this.add(imageUtil);

		jFrame.add(this);
		jFrame.setVisible(true);
	}

	
	@Override
	public void actionPerformed(ActionEvent e) {
		if("关闭".equals(e.getActionCommand())){
			//下线操作
			int count = JOptionPane.showConfirmDialog(null, "确定离开？");
			if(count==JOptionPane.YES_OPTION){
				Message message = new Message();
				message.setType(-1);
				message.setTimer(WhisperUtil.getTimer());
				message.setName(name);
				sendToServer(message);
			}
		}else if("发送".equals(e.getActionCommand())){
			/*
			没有选择聊天对象，提示
			选择自己为聊天对象，提示
			输入内容为空，提示
			 */
			String msg = sendInfo.getText();//获取消息内容
			java.util.List<String> selectedName = list.getSelectedValuesList();
			if("".equals(msg)){
				JOptionPane.showMessageDialog(null,"不能发送空消息");
			}else if(selectedName.size()<=0){
				JOptionPane.showMessageDialog(null,"请选择聊天对象");
				return ;
			}else if (selectedName.toString().contains("（我）")){
				JOptionPane.showMessageDialog(null,"不能与自己聊天");
				return;
			}
			/*
			满足发送条件：
			1.向服务器发送消息
					消息内容：1.时间 2.姓名 3.消息内容 4.类型 5.发送给谁
			2.服务器转发
			 */
			Message message  =new Message();
			message.setType(1);
			message.setTimer(WhisperUtil.getTimer());
			message.setName(name);
			message.setInfo(msg);
			HashSet<String> set = new HashSet<String>();
			set.addAll(selectedName);
			message.setClients(set);//选择聊天的对象
			sendToServer(message);
			//清空发送框内容
			sendInfo.setText(null);
			//添加自己对谁发了信息
			reciveInfo.append(message.getTimer()+"我对"+selectedName+"说:"+message.getInfo()+"\n");
		}
		
	}
	
	public static void main(String[] args) {
		new ChatRoom();
	}
	
}
