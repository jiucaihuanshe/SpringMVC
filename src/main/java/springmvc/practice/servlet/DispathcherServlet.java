package springmvc.practice.servlet;
/**
 * 2����дhttpServlet��get/post����
 * 
 * �������룺
 * 3����ɨ���Ŀ�ģ�
 * ����package��ɨ��������Ӱ��Լ��Ӱ��µ��࣬�õ����µ��������ļ������Ǿ�
 * ���Եõ��ļ������а������ļ��������ǾͿ���ͨ������new����Щ���ʵ��(newInstance)
 * Ŀ�ģ�ɨ������µ����е����ļ��������ļ���ȡ�����������������+������ͨ���ļ�
 * �ķ�ʽȥɨ��
 * 
 * 5�������е���new��ʵ�������Ǿ�Ҫ�����е�������ϵע���ȥ
 * �õ����Class����
 * �õ�field����
 * �õ�field�����annotation����
 * ����annotation������annotation���������
 * �����Ե�key�õ�map�е�ʵ��
 * Ȼ��filed.set��ʵ�����ý�ȥ
 * 
 * 4������һ��url�����з�����ӳ���ϵ
 * �õ����Class����
 * �õ�Method����
 * �õ�Method�����annotation����
 * ��url��method����Ȼ��map��
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
		//��ʼ��
		//��ɨ�裬��ȡ���е��ļ�
		scanPackage("springmvc.practice");
		try {
			filterAndInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//����ӳ���ϵ
		handerMap();
		//ʵ��ע��
		ioc();
	}
	private void ioc() {
		if(instanceMap.isEmpty()){
			return;
		}
		for(Map.Entry<String, Object> entry:instanceMap.entrySet()){
			//�õ��������е�����
			//���� Field �����һ�����飬��Щ����ӳ�� Class ��������ʾ�����ӿ��������������ֶΡ�
			Field[] fields = entry.getValue().getClass().getDeclaredFields();
			for(Field field : fields){
				//�ɷ���˽������
				field.setAccessible(true);
				if(field.isAnnotationPresent(Qualifier.class)){
					Qualifier qualifier =field.getAnnotation(Qualifier.class);
					String value= qualifier.value();
					//�ֶ����������ֶ���˽�еģ�Ҫ������ֶν�������ֵ
					field.setAccessible(true);
					try {
						//����һ ������		������ Ҫע��Ķ���
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
	
	//����·��ӳ��
	private void handerMap() {
		if(instanceMap.size()<=0){
			return;
		}
		//����
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
	
	//�������ļ�
	private void filterAndInstance() throws Exception {
		if(packageNames.size()<=0){
			return;
		}
		for(String className : packageNames){
			//�����ȡ�µ���������""�滻��".class"�����Һ���ǰ���հ׺�β���հ�
			//�õ� ����+������class���� β����.class��ɾ����
			Class<?> cName = Class.forName(className.replace(".class", "").trim());
			//���ָ�����͵�ע�ʹ����ڴ�Ԫ���ϣ��򷵻� true�����򷵻� false��
			if(cName.isAnnotationPresent(Controller.class)){
				//������ Class ��������ʾ�����һ����ʵ����
				Object instance = cName.newInstance();
				//������ڸ�Ԫ�ص�ָ�����͵�ע�ͣ��򷵻���Щע�ͣ����򷵻� null��
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
	
	//ɨ����ļ���װ
	private void scanPackage(String Package) {
		//�����е�.ת�壬��ȡ��Ӧ��·��
		URL url= this.getClass().getClassLoader().getResource("/"+replaceTo(Package));
		//�������·����Դ(url)������һ���ļ�����
		String pathFile = url.getFile();
		File file = new File(pathFile);
		//��ȡ��Ŀ¼�µ������ļ�
		String[] fileList = file.list();
		for(String path:fileList){
			File eachFile = new File(pathFile+path);
			if(eachFile.isDirectory()){//�ж��Ƿ���Ŀ¼
				scanPackage(Package+"."+eachFile.getName());//�ݹ�
			}else{
				// ����+����.class
				packageNames.add(Package+"."+eachFile.getName());//List�����д������+����
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
