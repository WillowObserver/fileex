package com.sam.task;

import org.nutz.json.Json;
import org.nutz.lang.Mirror;
import org.nutz.lang.util.NutType;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * ConvertSocketService
 * 转换服务的socket监听,根据获取到的信息,
 * 反射调用不同的类进行处理
 */
public class ConvertSocketService {
	public static String FTP_IP = null;
	public static String FTP_PORT = null;
	public static String FTP_PASSWORD = null;
	public static String FTP_USERNAME = null;
	public static String SOCKET_PORT = null;

	static {
		FileInputStream in = null;
		try {
			Properties properties = new Properties();
			in = new FileInputStream(System.getProperty("user.dir") + "/src/base.properties");
			properties.load(in);
			FTP_IP = properties.getProperty("FTP_IP");
			FTP_PORT = properties.getProperty("FTP_PORT");
			FTP_PASSWORD = properties.getProperty("FTP_PASSWORD");
			FTP_USERNAME = properties.getProperty("FTP_USERNAME");
			SOCKET_PORT = properties.getProperty("SOCKET_PORT");
			System.out.println("读取配置信息成功！");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("读取配置信息失败！");
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		ServerSocket ss = new ServerSocket(Integer.valueOf(SOCKET_PORT));
		while (true) {
			System.out.println("服务器启动,等待连接中....");
			Socket s = ss.accept();
			BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
			//获取连接数据
			String mess = br.readLine();
			Thread t = null;
			Thread daemoThread = null;
			try {
				Map maps = (Map) Json.fromJson(NutType.map(String.class, NutType.list(NutType.map(String.class, String.class))), mess);
				//反射查找类,调对应构造方法
				Class cls = Class.forName(maps.get("handler").toString());
				Object born = Mirror.me(cls).born(mess, s, FTP_IP, FTP_PORT, FTP_USERNAME, FTP_PASSWORD);
				//任务线程
				t = new Thread((Thread) born);
				//守护线程
				Daemon daemon = new Daemon(t, 120);//120秒超时时间
				daemoThread = new Thread(daemon);
				daemoThread.setDaemon(true);
				//执行这两个线程
				t.start();
				daemoThread.start();
			} catch (Exception e) {
				e.printStackTrace();
				if (daemoThread != null) {
					daemoThread.interrupt();
				}
				if (t != null) {
					t.interrupt();
				}
			}
		}
	}


	/**
	 * 守护线程
	 */
	static class Daemon implements Runnable {
		List<Runnable> tasks = new ArrayList<Runnable>();
		private Thread thread;
		private int time;

		public Daemon(Thread r, int t) {
			thread = r;
			time = t;
		}

		public void addTask(Runnable r) {
			tasks.add(r);
		}

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(time * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				thread.interrupt();
			}
		}

	}
}
