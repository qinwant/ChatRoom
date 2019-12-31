package whisper.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

/**
 * 聊天信息封装类
 * @author 84165
 *
 */
public class Message implements Serializable{
	/**
	 * UID
	 */
	private static final long serialVersionUID = 1L;

	private int type;								//聊天类型:0.上下线更新;1.上线;-1.下线
	
	private HashSet<String> clients;				//存放选中的用户
	
	private HashMap<String, Client> onlines;	    //在线用户
	
	private String name;							//姓名
	
	private String info;							//存放信息
	
	private String timer;							//时间

	/*
	 * 对应的get和set方法
	 */
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public HashSet<String> getClients() {
		return clients;
	}

	public void setClients(HashSet<String> clients) {
		this.clients = clients;
	}

	public HashMap<String, Client> getOnlines() {
		return onlines;
	}

	public void setOnlines(HashMap<String, Client> onlines) {
		this.onlines = onlines;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getTimer() {
		return timer;
	}

	public void setTimer(String timer) {
		this.timer = timer;
	}

	@Override
	public String toString() {
		return "Message [type=" + type + ", clients=" + clients + ", onlines=" + onlines + ", name=" + name + ", info="
				+ info + ", timer=" + timer + "]";
	}
	
}
