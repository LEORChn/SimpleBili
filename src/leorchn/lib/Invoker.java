/* Created by LEOR_Chn
 * License Copyright(R) LEOR_Chn (2014-2018)
 * LEOR_Chn Soft.
 */
package leorchn.lib;
import java.lang.reflect.*;
import java.util.*;
import static leorchn.lib.Activity1.pl;
/**	<pre>目前要注意的地方：
	对于返回数组的方法，需要将返回的 Object 强转为需求的数组类型才能使用
目前已实现：
	获取静态变量的值
	获取动态变量的值
	调用静态方法并获取非数组形式的返回值
	调用动态方法并获取非数组形式的返回值</pre>
*/
public class Invoker<T extends Object>{
	private boolean staticOnly=true;
	private T inst=null;
	private Class c=null;
	/**<pre>如果想通过此构造器获取一个实例，这里有一段示例：
	Invoker i=new Invoker("android.content.pm.ShortcutManager");
	i.setInstance(【实例：android.content.Context】.getSystemService(i.Class()));</pre>
	@param className 系统中可能存在的一个类的字符串。如果找到该类则将会允许访问该类的静态变量和静态方法。
	@exception ClassNotFoundException 上边托我给您带个话
	*/
	public Invoker(String className)throws ClassNotFoundException{
		c=Class.forName(className);
	}
	/**@param cls 由系统返回的类对象。以便之后访问该类的静态变量和静态方法。*/
	public Invoker(Class cls){
		c=cls;
	}
	/**也可以在之后使用 setInstance() 进行构建。
	@param instance 由系统返回（因为你不确定它是否存在）的实例，并访问该类的动态变量和动态方法
	*/
	public Invoker(T instance){ // 可访问动态，只能使用实例化对象
		setInstance(instance);
	}
	/**输入一个实例。这个方法会更改发起方法调用的类。
	<br>适用于复用本 Invoker 对象的场景。
		@param instance 将会刷新本 Invoker 对象的实例
	*/
	public void setInstance(T instance){
		inst=instance;
		if(staticOnly=instance==null);
		else c=inst.getClass();
	}
	/**输入一个兼容之前输入的类的实例。这个方法不会更改发起方法调用的类。
	<br>这或许会导致调用方法时调用的是这个实例的父类方法。
	<br>输入的实例通常不能是以之前输入的类的父类进行初始化的。
		@param instance 以之前输入的类，或者之前输入的类的子类进行初始化的实例
		@return true，如果输入成功
		<br>false，如果输入的实例不兼容，或者输入的实例是以之前输入的类的父类进行初始化的
	*/
	public boolean setCompatInstance(T instance){
		try{
			if(c.cast(instance)==null)throw new ClassCastException();
			inst=instance;
			staticOnly=false;
			return true;
		}catch(Throwable e){
			return false;
		}
	}
	/**应该也等效于 invoke("getClass")
	@return 正常情况下返回本 Invoker 对象所指向的静态类的类对象
	<br>null，如果构造本 Invoker 时传入 null 或者找不到所需的类
	*/
	public Class Class(){
		return c;
	}
	public <K extends Object>K get(String varName,K def){
		try{
			Field f=c.getDeclaredField(varName);
			f.setAccessible(true);
			Object val=f.get(staticOnly?c:inst);
			if(def.getClass().isAssignableFrom(val.getClass()))//checked
				return (K)val;
		}catch(Throwable e){
			if(def==null) return (K)e;
			else pl(E.trace(e));
		}
		return def;
	}
/**	<pre>用反射调用一个方法，自动判断参数类型。
注意：
	不适用于只有单个数组的参数的方法。
	事实上更推荐使用 findMethod(Class...).invoke(Object...)
示例：functionName(String, float, int[], String...)
	invoke("functionName", "arg0", 1.0f, new int[]{2,3}, new String[]{"arg4","arg5"})</pre>
	@param name 要调用的方法名称
	@param param 参数的合集
	@return 正常情况下返回原方法返回的数据。
	<br>异常，如果找不到原函数或调用原函数失败
	<br>如果原函数返回的是数组，请手动强制转换为数组形式，如 Object[]
*/
	public Object invoke(String name,Object...param){
		Class[]classes={};
		if(param.length>0){
			classes=new Class[param.length];
			for(int i=0,len=param.length;i<len;i++)
				classes[i]=param[i].getClass();
		}
		try{
			Method m=c.getDeclaredMethod(name, classes);
			m.setAccessible(true);
			return m.invoke(staticOnly?c:inst, param);
		}catch(Throwable e){
			return e;
		}
	}
	//todo：新增一个支持自定义类型的invoke方法
///**	<pre>用反射调用一个方法，仅适用于只有单个数组的参数的方法
/*注意：
	第二个参数必须为数组型Class；之后的参数必须兼容第二个参数成为其数组成员
示例：functionName(String[]) 以及 functionName(String...)
	invoke("functionName",null,new String[]{"arg0","arg1","arg2"})
	invoke("functionName",String[].class,"arg0","arg1","arg2",...,"arg99")</pre>
	@param name 要调用的方法名称
	@param arrayModeClass 参数3的数据类型的数组形式，如 String[].class
	@param arrayParam 兼容参数2数组成员的数据，如果调用 getClass 不等于 Object[].class ，会忽略参数2
	@return 原方法返回的数据，如果是正常情况下。
	<br>null，如果参数3初始化为 Object[] 并且参数2不是数组型 Class
	<br>异常，如果找不到原函数或调用原函数失败
	<br>如果原函数返回的是数组，请手动强制转换为数组形式，如 Object[]
	public Object invoke(String name,Class arrayModeClass,Object...arrayParam){
		Object[]a=arrayParam;
		if(a.getClass()==Object[].class){
			if(arrayModeClass==null) return null;
			Class ct=arrayModeClass.getComponentType();
			if(ct==null) return null;
			a=(Object[])Array.newInstance(ct, a.length);
			for(int i=0,len=arrayParam.length;i<len;i++)
				a[i]=arrayParam[i];
		}else arrayModeClass=a.getClass();
		try{
			Method m=c.getDeclaredMethod(name, arrayModeClass);
			m.setAccessible(true);
			return m.invoke(staticOnly?c:inst, new Object[]{a});
		}catch(Throwable e){
			return e;
		}
	}*/
	/**使用这个表示你准备好直接 new 一个该对象了。
	@param classes 参数类型的合集
	@return 正常情况下返回所找到的构造器，然后只要使用 invoke() 带上参数就行
	<br>null，如果构造本 Invoker 时发生问题，或者找不到构造器
	*/
	public FoundConstructor newInstance(Class...classes){
		try{
			return new FoundConstructor(c.getConstructor(classes));
		}catch(Throwable e){
			pl("---new instance fail：",E.trace(e));
			return null;
		}
	}
	/**精确查找一个方法。
	@param name 方法的名称
	@param classes 参数类型的合集
	@return 正常情况下返回所找到的方法，然后只要使用 invoke() 带上参数就行
	<br>null，如果构造本 Invoker 时发生问题，或者找不到构造器
	*/
	public FoundMethod findMethod(String name,Class...classes){
		try{
			Method m=c.getDeclaredMethod(name,classes);
			m.setAccessible(true);
			return new FoundMethod(m);
		}catch(Throwable e){
			pl(E.trace(e),inst);
			return null;
		}
	}
	class FoundConstructor{
		Constructor cn;
		FoundConstructor(Constructor cns){ cn=cns; }
		public boolean invoke(Object...param){
			try{
				setInstance((T)cn.newInstance(param));
				return true;
			}catch(Throwable e){
				return false;
			}
		}
	}
	class FoundMethod{
		Method m;
		FoundMethod(Method mt){ m=mt; }
		public Object invoke(Object...param){
			pl("----invoking ",staticOnly?"static ":"dynamic ",m.getName()," in Class ",c.getName());
			try{
				return m.invoke(staticOnly?c:inst, param);
			}catch(Throwable e){
				pl("----invoke ",m.getName()," fail: ",E.trace(e));
				return e;
			}
		}
	}
}
