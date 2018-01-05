/* Created by LEOR_Chn
 * License Copyright(R) LEOR_Chn (2014-2017)
 * LEOR_Chn Soft.
 */
package leorchn.lib;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
public class XML {
	private boolean usable=false;
	public Node node;
	public XML(String xml){
		try{
			node = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes()));
			usable=true;
		}catch(Exception e){}
	}
	public XML(Node n){
		if(n==null)return;
		node=n;
		usable=true;
	}
	public boolean canRead(){return usable;}
	public XML get(String path){
		String[] tp=path.replaceAll("\\\\","/").split("/",2);
		XML[]x=getList(tp[0]);
		return x.length==0?null:
			tp.length==2?x[0].get(tp[1]):x[0];
	}
	public XML[]getList(String key){
		if(!usable){
			Node nul=null;
			return new XML[]{new XML(nul)};
		}
		NodeList nl=node.getChildNodes();
		ArrayList<XML> na=new ArrayList<>();
		for(int i=0;i<nl.getLength();i++)if(nl.item(i).getNodeName().equals(key))na.add(new XML(nl.item(i)));//Node[] ns=new Node[na.size()];for(int i=0;i<na.size();i++)ns[i]=na.get(i);
		return na.toArray(new XML[na.size()]);//ns;
	}
	public String text(){return usable?node.getTextContent():"";}
	public String attr(String key){
		return usable?node.getAttributes().getNamedItem(key).getNodeValue():"";
	}
	/*public String toString(){
		return node;
	}*/
}
