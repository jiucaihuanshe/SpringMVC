package springmvc.practice.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import springmvc.practice.annotation.Controller;
import springmvc.practice.annotation.Qualifier;
import springmvc.practice.annotation.RequestMapping;
import springmvc.practice.service.impl.MyService;
import springmvc.practice.service.impl.SpringmvcService;
@Controller("wuqi")
public class SpringmvcController {
	@Qualifier("MyServiceImpl")
	MyService myService;
	@Qualifier("SpringmvcServiceImpl")
	SpringmvcService smService;
	@RequestMapping("insert")
	public String insert(HttpServletRequest request,HttpServletResponse response,String param){
		myService.insert(null);
		smService.insert(null);
		return null;
	}
	@RequestMapping("delete")
	public String delete(HttpServletRequest request,HttpServletResponse response,String param){
		myService.delete(null);
		smService.delete(null);
		return null;
	}
	@RequestMapping("update")
	public String update(HttpServletRequest request,HttpServletResponse response,String param){
		myService.update(null);
		smService.update(null);
		return null;
	}
	@RequestMapping("select")
	public String select(HttpServletRequest request,HttpServletResponse response,String param){
		myService.select(null);
		smService.select(null);
		return null;
	}
	
}
