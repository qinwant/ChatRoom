package whisper.entity;

import java.net.Socket;

/**
 * 客户端的用户名和socket
 */
public class Client {
	private String name;
	private Socket socket;
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	

}
