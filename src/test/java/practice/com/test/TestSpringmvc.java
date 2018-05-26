package practice.com.test;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import springmvc.practice.annotation.Controller;
import springmvc.practice.annotation.RequestMapping;

@Controller
public class TestSpringmvc {
	@RequestMapping("test")
	public void test1(HttpServletRequest request,HttpServletResponse response,String param){
		System.out.println(param);
		try {
			response.getWriter().write("method success!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
