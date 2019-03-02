package leorchn.lib;
import android.os.*;
import java.nio.channels.*;
import java.net.*;
import java.nio.*;

public class SocketClient extends AsyncTask<Void,Packs,Void> implements Runnable{
	public SocketClient(String ip,int port){
		this.ip=ip;
		this.port=port;
	}
	public void start(){
		this.executeOnExecutor(THREAD_POOL_EXECUTOR);
	}
	public static final int
		STATUS_CLOSED=0,
		STATUS_OPEN=1,//not using in client
		STATUS_LISTENING=2,//not using in client
		STATUS_CONNECTIONPENDING=3,
		STATUS_RESOLVINGHOST=4,
		STATUS_HOSTRESOLVED=5,
		STATUS_CONNECTING=6,
		STATUS_CONNECTED=7,
		STATUS_CLOSING=8,
		STATUS_ERROR=9,
		PACKT_STATUS=0,
		PACKT_MESSAGE=1,
		PACKT_RECEIVE=2;
	Handler h=new Handler(Looper.getMainLooper());
	SocketChannel sok;
	@Override protected void onPreExecute(){
		onConnecting();
		super.onPreExecute();
	}
	@Override protected Void doInBackground(Void[]p){
		try{
			publishProgress(new Packs(STATUS_CONNECTING));
			// 下面这行可能报错找不到ip，跳转到 SocketException
			sok=SocketChannel.open(new InetSocketAddress(ip,port));
			
			// 下面这行可能报错连接已关闭，跳转到 ClosedChannelException
			sok.configureBlocking(false);
			Selector sel=Selector.open();
			sok.register(sel, SelectionKey.OP_READ);
			if(connectpak != null && connectpak.length > 0)
				sok.write(ByteBuffer.wrap(connectpak));
			
			while(sel.select() > 0)
				for (SelectionKey sk : sel.selectedKeys()) {//遍历每个有可用IO操作Channel对应的SelectionKey
					if (sk.isReadable()) {//如果该SelectionKey对应的Channel中有可读的数据
						SocketChannel sc = (SocketChannel) sk.channel();// 使用NIO读取Channel中的数据
						ByteBuffer buffer = ByteBuffer.allocate(2048);
						int len=sc.read(buffer);//len=已取出数据包的大小，如果过大则为-1
						if(len<1){
							byte[]c=buffer.array();
							len=c.length;
							for(int i=len;i>0;i--) if(c[i-1]>0)break; else len--;
							//log(string("遇到神秘乱流 ",len," byte"));
						}else{
							buffer.flip();
							byte[]b=new byte[len];
							System.arraycopy(buffer.array(),0,b,0,len);
							//onReceive(b,b.length);//将接收到的完整数据包发出
						}
						sk.interestOps(SelectionKey.OP_READ);//为下一次读取作准备
					}
					sel.selectedKeys().remove(sk);//删除正在处理的SelectionKey
				}
			//onReadError(ex);
			//log("连接错误：\n连接被断开，稍后自动重试。");
			
			
		}catch(SocketException e){
			onError(EventListener.STEP_IP,e);
			onClose();
		}catch(Throwable e){
			onError(EventListener.STEP_READ,e);
		}//ReadError会使用主动关闭连接，然后使上线程走出循环并结束
		return null;
	}
	@Override protected void onProgressUpdate(Packs[]i) {
		
		super.onProgressUpdate(i);
	}
	@Override public void run(){
		if(sok.isOpen() && heartbeatpak != null){//连接打开时无限心跳包
			try{	
				sok.write(ByteBuffer.wrap(heartbeatpak));
				onSentHeartBeat();
			}catch(Throwable e){
				onError(EventListener.STEP_SEND,e);
			}
			h.postDelayed(this,Math.max(10000,heartbeatTimeout * 1000));//心跳包间隔至少10秒
		}
	}
	boolean reconnect;
	String ip;
	int port,
		heartbeatTimeout,
		reconnectTimeout;
	byte[]connectpak,
		heartbeatpak;
	EventListener el;
	public SocketClient setConnectPackage(byte[]pack){
		connectpak=pack;
		return this;
	}
	public SocketClient setHeartBeat(byte[]pack,int timeoutSecond){
		heartbeatpak=pack;
		heartbeatTimeout=timeoutSecond;
		return this;
	}
	public SocketClient setReconnect(boolean bool,int timeoutSecond){
		reconnect=bool;
		reconnectTimeout=timeoutSecond;
		return this;
	}
	public SocketClient setListener(EventListener e){
		el=e;
		return this;
	}
	// 以下 默认的继承式监听器
	protected void onConnecting(){
		if(el != null) el.onConnecting(this,ip,port);
	}
	protected void onConnected(){
		if(el != null) el.onConnected(this,ip,port);
	}
	protected void onError(int step,Throwable e){
		if(el != null) el.onError(this,ip,port,step,e);
	}
	protected void onClose(){
		if(el != null) el.onClose(this,ip,port);
	}
	protected void onSentHeartBeat(){
		if(el != null) el.onSentHeartBeat(this,ip,port);
	}
	protected void onReceive(byte[]bytes){
		if(el != null) el.onReceive(this,ip,port,bytes);
	}
	// 以上 默认的继承式监听器
	// 以下 外部监听器
	public static interface EventListener{
		public static final int
			STEP_NETWORK=1,
			STEP_IP=2,
			STEP_SEND=4,
			STEP_READ=8;
		public void onConnecting(SocketClient sc,String ip,int port)
		public void onConnected(SocketClient sc,String ip,int port)
		public void onError(SocketClient sc,String ip,int port,int step,Throwable e)
		public void onSentHeartBeat(SocketClient sc,String ip,int port)
		public void onReceive(SocketClient sc,String ip,int port,byte[]bytes)
		public void onClose(SocketClient sc,String ip,int port)
	}
}
class Packs{
	int type,
		status;
	String msg;
	byte[]rec;
	public Packs(int status){
		type=SocketClient.PACKT_STATUS;
		this.status=status;
	}
	public Packs(String msg){
		type=SocketClient.PACKT_MESSAGE;
		this.msg=msg;
	}
	public Packs(byte[]received){
		type=SocketClient.PACKT_RECEIVE;
		rec=received;
	}
}
