package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController {
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	public UserRepository getUserRepository() {
		return userRepository;
	}
	public void setUserRepository(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@GetMapping("/")
	public String home(Model model)
	{
		model.addAttribute("title","SCM | Home");
		return "home";
	}
	
	@GetMapping("/about")
	public String about(Model model)
	{
		model.addAttribute("title","SCM | About");
		return "about";
	}

	@GetMapping("/signup")
	public String signup(Model model)
	{
		model.addAttribute("title","SCM | SignUp");
		model.addAttribute("user",new User());
		return "signup";
	}
	@PostMapping("/doRegister")
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult bresult ,@RequestParam(value="agreement",defaultValue = "false")boolean agreement, Model model,HttpSession session)
	{
		try 
		{
			if(!agreement)
			{
				System.out.println("You have not agreed terms and condition");
				throw new Exception("You have not agreed terms and condition");
			}
			if(bresult.hasErrors())
			{
				System.out.println(bresult.toString());
				model.addAttribute("user",user);
				return "signup";
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("defaul.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			
			
			User result = this.userRepository.save(user);
			System.out.println(result);
			
			model.addAttribute("title","SCM | SignUp");
			model.addAttribute("user",new User());
			session.setAttribute("message", new Message("Successfully registered ","alert-success"));		
		} 
		catch (Exception e) 
		{
			model.addAttribute("user",user);
			session.setAttribute("message", new Message("Something went wrong -"+ e.getMessage(),"alert-danger"));
			model.addAttribute("title","SCM | SignUp");
			return "signup";	
		}
		return "signup";
	}
	
	@GetMapping("/signIn")
	public String customLogin(Model model)
	{
		model.addAttribute("title","SCM | SignIn");
		return "Login";
	}
	
}
