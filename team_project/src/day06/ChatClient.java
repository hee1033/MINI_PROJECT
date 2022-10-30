package day06;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

import org.json.JSONObject;

import view.CategoryView;

public class ChatClient {
	
	static Scanner scanner = new Scanner(System.in);
	//필드
	Socket socket;
	DataInputStream dis;
	DataOutputStream dos;
	String chatName;//socket name
	String choice;//mainboard 
	String data;//메인 결과
	boolean flag;//로그인
	
	public void connect() throws IOException{
		socket=new Socket("localhost",50041);
		dis=new DataInputStream(socket.getInputStream());
		dos=new DataOutputStream(socket.getOutputStream());
		System.out.println("[client] complete server connect");
	}
	
	public void receive() {
		try {
			String message="";
			String json = dis.readUTF();
			JSONObject root = new JSONObject(json);
			String check = root.getString("check");
			switch (check) {
			case "complete":
				this.choice = root.getString("choice");
				flag=root.getBoolean("flag");
				message = root.getString("message");
				System.out.println(message);
				break;
			case "menuNo":
				data = root.getString("data");
				break;
			}
		} catch (IOException e) {
			System.out.println("[client] server disconnected.");
			System.exit(0);
		}
	}
	
	public void send(String json) throws IOException {
		dos.writeUTF(json);
		dos.flush();
	}
	
	public void exit() throws IOException{
		System.out.println("BYE~");
		socket.close();
	}
	
	public static void main(String[] args) {
		try {
			ChatClient chatClient = new ChatClient();
			chatClient.connect();
			System.out.print("입장 id : ");
			chatClient.chatName=scanner.nextLine();
			//chatName을 비회원 DB생성 후 중복은 안되게 해서 들어올수 있게 만들어야 한다....
			JSONObject jsonObject= new JSONObject();
			jsonObject.put("check", "incoming");
			jsonObject.put("Name", chatClient.chatName);
			jsonObject.put("choice", "first");
			String json=jsonObject.toString();
			//SocketClient .send로 json변수를 보낸다.
			chatClient.send(json);
			chatClient.receive();
			
			while(true) {
				//가장 마지막의 choice를 이용해 다시 main으로 돌아오기 - "first" 주기
				if(chatClient.choice.equals("first")) {
					MainBoard.mainBoard();
					jsonObject= new JSONObject();
					jsonObject.put("check", "mainBoard");
					jsonObject.put("choice", "x");
					jsonObject.put("flag", chatClient.flag);
					json=jsonObject.toString();
					chatClient.send(json);
					chatClient.receive();
				}
				else {
					//menuNo에 값이 없을 경우
					System.out.print("MenuNo = ");
					String menuNo=scanner.nextLine();
					if(!menuNo.equals("1")&&!menuNo.equals("2")&&!menuNo.equals("3")&&!menuNo.equals("4")&&!menuNo.equals("5")&&!menuNo.equals("6")&&!menuNo.equals("7")&&!menuNo.equals("8")) {
						jsonObject= new JSONObject();
						jsonObject.put("check", "mainBoard");
						jsonObject.put("choice", "first");
						jsonObject.put("flag", chatClient.flag);
						json=jsonObject.toString();
						chatClient.send(json);
						chatClient.receive();
					}
					else {
						if(menuNo.equals("8")) {
							break;
						}else {
							JSONObject updata = new JSONObject();
							jsonObject= new JSONObject();
							jsonObject.put("check", "menuNo");
							jsonObject.put("menuNo", menuNo);
							jsonObject.put("listNo", "0");
							jsonObject.put("updata", updata);
							json=jsonObject.toString();
							chatClient.send(json);
							chatClient.receive();
							switch(chatClient.data) {
							case "category":
								CategoryView.seeCategory(chatClient);
								break;
							case "best":
								break;
							case "event":
								break;
							case "search":
								break;
							case "mypage":
								if(chatClient.flag==false) {
									System.out.println("로그인이 필요합니다.");
									UserView.board(chatClient);
								}else {
									MyPageView.board(chatClient);
								}
								break;
							case "qna":
								break;
							case "user":
								if(chatClient.flag==false) {
									UserView.board(chatClient);	
								}
								else {
									System.out.println("\n**로그인이 되어있습니다**\n다시 MenuNo를 선택해주세요.\n");
								}
								break;
							}
						}
					}
				}
				
			}
			scanner.close();
			chatClient.exit();
		}catch(Exception e) {
			System.out.println("[client] 서버 연결 안됨");
		}
		System.out.println("[client] 종료.");
	}

	


	
}