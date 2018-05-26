package springmvc.practice.servlet;
/**
 * 2、重写httpServlet的get/post方法
 * 
 * 分析代码：
 * 3、包扫描的目的：
 * 根据package，扫描下面的子包以及子包下的类，拿到包下的所有类文件后，我们就
 * 可以得到文件名，有包名有文件名，我们就可以通过反射new出这些类的实例(newInstance)
 * 目的：扫描基包下的所有的类文件，根据文件获取类的完整类名：包名+类名，通过文件
 * 的方式去扫描
 * 
 * 5、把所有的类new出实例后，我们就要把类中的依赖关系注入进去
 * 拿到类的Class对象
 * 拿到field对象
 * 拿到field上面的annotation对象
 * 根据annotation对象拿annotation对象的属性
 * 把属性当key拿到map中的实例
 * 然后filed.set把实例设置进去
 * 
 * 4、建立一个url与类中方法的映射关系
 * 拿到类的Class对象
 * 拿到Method对象
 * 拿到Method上面的annotation对象
 * 把url和method对象然后map中
 */
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import springmvc.practice.annotation.Controller;
import springmvc.practice.annotation.Qualifier;
import springmvc.practice.annotation.RequestMapping;
import springmvc.practice.annotation.Service;
import springmvc.practice.controller.SpringmvcController;

public class DispathcherServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;
	List<String> packageNames = new ArrayList<>();
	Map<String, Object> instanceMap = new HashMap<>();
	Map<String, Object> handerMap = new HashMap<>();
	public DispathcherServlet() {
	}
	@Override
	public void init(ServletConfig config) throws ServletException {
		//初始化
		//包扫描，获取包中的文件
		scanPackage("springmvc.practice");
		try {
			filterAndInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//建立映射关系
		handerMap();
		//实现注入
		ioc();
	}
	private void ioc() {
		if(instanceMap.isEmpty()){
			return;
		}
		for(Map.Entry<String, Object> entry:instanceMap.entrySet()){
			//拿到里面所有的属性
			//返回 Field 对象的一个数组，这些对象反映此 Class 对象所表示的类或接口所声明的所有字段。
			Field[] fields = entry.getValue().getClass().getDeclaredFields();
			for(Field field : fields){
				//可访问私有属性
				field.setAccessible(true);
				if(field.isAnnotationPresent(Qualifier.class)){
					Qualifier qualifier =field.getAnnotation(Qualifier.class);
					String value= qualifier.value();
					//字段中如果这个字段是私有的，要对这个字段进行设置值
					field.setAccessible(true);
					try {
						//参数一 本对象		参数二 要注入的对象
						field.set(entry.getValue(), instanceMap.get(value));
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
			SpringmvcController wuqi = (SpringmvcController) instanceMap.get("wuqi");
			System.out.println(wuqi);
		}
	}
	
	//处理路径映射
	private void handerMap() {
		if(instanceMap.size()<=0){
			return;
		}
		//遍历
		for(Map.Entry<String, Object> entry : instanceMap.entrySet()){
			if(entry.getValue().getClass().isAnnotationPresent(Controller.class)){
				Controller controller = entry.getValue().getClass().getAnnotation(Controller.class);
				String ctvalue = controller.value();
				Method[] methods = entry.getValue().getClass().getMethods();
				for(Method method:methods){
					if(method.isAnnotationPresent(RequestMapping.class)){
						RequestMapping rm = method.getAnnotation(RequestMapping.class);
						String rmvalue=rm.value();
						handerMap.put("/"+ctvalue+"/"+rmvalue, method);
					}else{
						continue;
					}
				}
			}else{
				continue;
			}
		}
	}
	
	//加载类文件
	private void filterAndInstance() throws Exception {
		if(packageNames.size()<=0){
			return;
		}
		for(String className : packageNames){
			//反射获取新的类名，以""替换掉".class"，并且忽略前导空白和尾部空白
			//得到 包名+类名的class对象 尾部的.class被删除了
			Class<?> cName = Class.forName(className.replace(".class", "").trim());
			//如果指定类型的注释存在于此元素上，则返回 true，否则返回 false。
			if(cName.isAnnotationPresent(Controller.class)){
				//创建此 Class 对象所表示的类的一个新实例。
				Object instance = cName.newInstance();
				//如果存在该元素的指定类型的注释，则返回这些注释，否则返回 null。
				Controller controller = cName.getAnnotation(Controller.class);
				String key =controller.value();
				instanceMap.put(key, instance);
			}else if(cName.isAnnotationPresent(Service.class)){
				Object instance = cName.newInstance();
				Service service = cName.getAnnotation(Service.class);
				String key = service.value();
				instanceMap.put(key, instance);
			}else{
				continue;
			}
		}
	}
	
	//扫描包文件封装
	private void scanPackage(String Package) {
		//将所有的.转义，获取对应的路径
		URL url= this.getClass().getClassLoader().getResource("/"+replaceTo(Package));
		//基于这个路径资源(url)，构建一个文件对象
		String pathFile = url.getFile();
		File file = new File(pathFile);
		//获取该目录下的所有文件
		String[] fileList = file.list();
		for(String path:fileList){
			File eachFile = new File(pathFile+path);
			if(eachFile.isDirectory()){//判断是否是目录
				scanPackage(Package+"."+eachFile.getName());//递归
			}else{
				// 包名+类名.class
				packageNames.add(Package+"."+eachFile.getName());//List集合中存入包名+类名
			}
			
		}
	}
	private String replaceTo(String path) {
		return path.replaceAll("\\.", "/");
	}
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String url = req.getRequestURI();
		String context = req.getContextPath();
		String path = url.replace(context, "");
		Method method = (Method) handerMap.get(path);
		SpringmvcController controller = (SpringmvcController) instanceMap.get(path.split("/")[1]);
		try {
			method.invoke(controller, new Object[]{req,resp,null});
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}
