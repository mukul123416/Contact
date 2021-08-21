package com.smart.controllers;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.helper.Message;
import com.smart.models.User;
import com.smart.services.EmailService;

@Controller
public class ForgotController {
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	 @Autowired
	 private BCryptPasswordEncoder passwordEncoder;
	
	Random random=new Random();
	
	@RequestMapping("/forgot")
	public String openEmailForm(){
		return "forgot_email_form";
	}
	
	
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email,HttpSession session){
		
		System.out.println(email);
		
		//generating otp
		int otp = random.nextInt(999999);
		System.out.println(otp);
		
		//write code for send otp to email..
		String subject="OTP From SCM";
		String message=""
				+ "<div style='border:1px solid #e2e2e2; padding:20px'>"
				+ "<h1>"
				+ "OTP is "
				+ "<b>"+otp
				+ "</n>"
				+ "</h1>"
				+ "</div>";
		String to=email;
		boolean flag = this.emailService.sendEmail(message, subject, to);
		
		if(flag) {
			session.setAttribute("message", new Message("We have send OTP to your email..","alert-success"));
			session.setAttribute("OTP",otp);
			session.setAttribute("Email",email);
			return "verify_otp";
		}else {
			session.setAttribute("message", new Message("Check your email id !!","alert-danger"));
			return "forgot_email_form";
		}
		
	}
	
	
	
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp,HttpSession session) {
		
		int myOtp=(int) session.getAttribute("OTP");
		String email=(String) session.getAttribute("Email");
		
		if(myOtp==otp) {
			
			User user = this.userRepository.getUserByUserName(email);
			
			if(user==null) {
				session.setAttribute("message", new Message("User does't exists with this email !!","alert-danger"));
				return "forgot_email_form";
			}
			
			return "password_change_form";
			
			
		}else {
			session.setAttribute("message", new Message("You have entered wrong  otp !!","alert-danger"));
			return "verify_otp";
		}
	}
	
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword,HttpSession session) {
		
		String email=(String) session.getAttribute("Email");
		User user = this.userRepository.getUserByUserName(email);
		user.setPassword(passwordEncoder.encode(newpassword));
		this.userRepository.save(user);
		return "redirect:/signin?change=password changed successfully..";
		
	}
	

}
