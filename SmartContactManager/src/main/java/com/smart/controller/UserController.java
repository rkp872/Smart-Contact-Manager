package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository 	userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@ModelAttribute
	public void addCommonData(Model model,Principal principal)
	{
		String name = principal.getName();
		System.out.println(name);
		//get the user by username
		User userByUserName = userRepository.getUserByUserName(name);
		System.out.println("USER"+userByUserName);
		model.addAttribute("user",userByUserName);
	}
	
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal)
	{
		model.addAttribute("title","SCM | User Dashboard");
		return "normal/user-dashboard";
	}
	
	//open add contact handler
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("title","SCM | Add Contact");
		model.addAttribute("contact",new Contact());
		return "/normal/add-contact";
	}
	
	@PostMapping("/process-contact")
	public String processAddContactForm(@ModelAttribute Contact contact,@RequestParam("contact-image") MultipartFile file,Principal principal,Model model,HttpSession session)
	{
		try {
			String name=principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			//processing and uploading file
			
			if(file.isEmpty())
			{
				//empty task
				System.out.println("Image is empty");
				contact.setImage("default.png");
			}
			else
			{
				//file upload to folder and update the name to contact table in DB
				contact.setImage(file.getOriginalFilename());
				
				File file2 = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(file2.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is uploaded");
			}
			
			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);
			System.out.println("Adeed to database");
			session.setAttribute("message", new Message("Contact added successfully", "success"));
			System.out.println("DATA : "+contact);
			model.addAttribute("title","SCM | Add Contact");
			
			
		} catch (Exception e) {
			System.out.println("ERROR : "+e.getMessage());
			session.setAttribute("message", new Message("Something went wrong Try again", "danger"));
			e.printStackTrace();
		}
		return "/normal/add-contact";
	}
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model,Principal principal)
	{
		model.addAttribute("title","SCM | View Contact");
		String name = principal.getName();
		Pageable pageable = PageRequest.of(page, 5);
		User userByUserName = this.userRepository.getUserByUserName(name);
		Page<Contact> contacts = this.contactRepository.findContactsByUser(userByUserName.getId(),pageable);
		model.addAttribute("contacts",contacts);
		model.addAttribute("currentPage",page);
		model.addAttribute("totalPages",contacts.getTotalPages());
		
		return "normal/showContacts";
	}
	
	@RequestMapping("/{cid}/contact")
	public String showContactDetails(@PathVariable("cid") int cid,Model model,Principal principal)
	{
		System.out.println("CID"+cid);
		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();
		model.addAttribute("title","SCM | Contact Details");
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		if(user.getId()==contact.getUser().getId())
		{
			model.addAttribute("contact",contact);
		}
		
	
		return "normal/contact-details";
	}
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid,Model model,HttpSession session,Principal principal)
	{
		Optional<Contact> findById = this.contactRepository.findById(cid);
		Contact contact = findById.get();
		
		
		contact.setUser(null);
		//remove photo 
		//img
		//contact.getImage() create a path..then go and delete that image
		//this.contactRepository.delete(contact);
		
		User user = this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		
		session.setAttribute("message", new Message("Contact deleted succcesssfully", "success"));
		return "redirect:/user/show-contacts/0";
	}
	@PostMapping("/update-form/{cid}")
	public String updateFOrm(@PathVariable("cid") Integer cid,Model model)
	{
		model.addAttribute("title","SCM | Update Contact");
		Contact contact = this.contactRepository.findById(cid).get();
		model.addAttribute("contact",contact);
		return "normal/update-form";
	}
	
	@PostMapping("/process-update")
	public String updateContact(@ModelAttribute Contact contact,@RequestParam("contact-image")MultipartFile file,Model model,HttpSession session,Principal principal)
	{
		try {
			//old contactd details
			Contact oldContact = this.contactRepository.findById(contact.getCid()).get();
			
			//image
			if(!file.isEmpty())
			{
				//delete old photo
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile,oldContact.getImage());
				file1.delete();
				//update new photo
				File file2 = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(file2.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
			}
			else
			{
				contact.setImage(oldContact.getImage());
			}
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Your contact is updated", "success"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("COntact NAME : "+contact.getName());
		return "redirect:/user/"+contact.getCid()+"/contact";
	}
	
	@GetMapping("/profile")
	public String yourProfile(Model model)
	{
		model.addAttribute("title","SCM | Profile");
		return "normal/profile";
	}
	
	
}
