package com.smart.controllers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import com.razorpay.*;

import com.smart.dao.ContactRepository;
import com.smart.dao.MyOrderRepository;
import com.smart.dao.UserRepository;
import com.smart.helper.Message;
import com.smart.models.Contact;
import com.smart.models.MyOrder;
import com.smart.models.User;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	 @Autowired
	 private BCryptPasswordEncoder passwordEncoder;
	 
	 @Autowired
	 private MyOrderRepository myOrderRepository;
	
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
		
		String userName=principal.getName();
		
		User user = userRepository.getUserByUserName(userName);
		
		model.addAttribute("user", user);
		model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}
	
	
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model,Principal principal) {
		
        String userName=principal.getName();
		
		User user = userRepository.getUserByUserName(userName);
		
		model.addAttribute("user", user);
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		return "normal/add_contact_form";
	}
	
	
	
	@PostMapping("/process-contact")
	public String processContact(@Valid @ModelAttribute("contact") Contact contact,BindingResult result1,Principal principal, @RequestParam("profileImage") MultipartFile file,Model model,HttpSession session) {
		
		try {
			
		String name=principal.getName();
		User user=this.userRepository.getUserByUserName(name);
		
		if(file.isEmpty()) {
			System.out.println("File is empty");
			contact.setImage("contact.png");
			throw new Exception("File Can't be empty");
		}
		else {
			contact.setImage(file.getOriginalFilename());
			File saveFile = new ClassPathResource("static/img").getFile();
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			System.out.println("Image is uploaded");
			
		}
		
		 if(result1.hasErrors()) 
			{
			    model.addAttribute("user", user);
			    return "normal/add_contact_form";
			}
		
		contact.setUser(user);
		
		user.getContacts().add(contact);
		
		this.userRepository.save(user);
		
		System.out.println("Added to data base");
		
		System.out.println("Data "+contact);
		
		model.addAttribute("user",user);
		
		session.setAttribute("message", new Message("Successfully Added !!","alert-success"));
		return "normal/add_contact_form";
		
		}catch(Exception e) {
			 String name=principal.getName();
			 User user=this.userRepository.getUserByUserName(name);
			 model.addAttribute("user",user);
			 session.setAttribute("message", new Message("Something Went wrong !!" + e.getMessage(),"alert-danger"));
			 e.getStackTrace();
			 return "normal/add_contact_form";
		}
		
	}
	
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model m,Principal principal) {
		
		String name=principal.getName();
		User user=this.userRepository.getUserByUserName(name);
		
		Pageable pageable=PageRequest.of(page, 2);
		
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		m.addAttribute("user",user);
		m.addAttribute("title","Show Contacts");
		return "normal/show_contacts";
	}
	
	
	@RequestMapping("/contact/{cId}")
    public String showContactDetail(@PathVariable("cId") Integer cId,Model m,Principal principal) {
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		String name=principal.getName();
		User user=this.userRepository.getUserByUserName(name);
		m.addAttribute("user",user);
		
		if(user.getId()==contact.getUser().getId()) {
			m.addAttribute("contact", contact);
			m.addAttribute("title",contact.getName());
		}
		
    	return "normal/contact_detail";
    }
	
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId,Model model,HttpSession session) {
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		contact.setUser(null);
		this.contactRepository.delete(contact);
		
		session.setAttribute("message", new Message("Contact deleted successfully !!","alert-success"));
		
		return "redirect:/user/show-contacts/0";
	}
	
	
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cid,Model model,Principal principal) {
		
		String name=principal.getName();
		User user=this.userRepository.getUserByUserName(name);
		model.addAttribute("user",user);
		
		Contact contact = this.contactRepository.findById(cid).get();
		model.addAttribute("contact", contact);
		
		model.addAttribute("title","Update Conatct");
		return "normal/update_form";
	}
	
	
	
	@RequestMapping(value="/process-update",method=RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model model,HttpSession session,Principal principal) {
		
		try {
			
			//old contact details
			Contact oldcontactDetails = this.contactRepository.findById(contact.getcId()).get();
			
			if(!file.isEmpty()) {
				
				//delete old photo
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile,oldcontactDetails.getImage());
				file1.delete();
				
				//update new photo
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
				
			}else {
				contact.setImage(oldcontactDetails.getImage());
			}
			
			String name=principal.getName();
			User user=this.userRepository.getUserByUserName(name);
			contact.setUser(user);
			model.addAttribute("user",user);
			
			this.contactRepository.save(contact);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		session.setAttribute("message", new Message("Your contact is updated...","alert-success"));
		return "redirect:/user/contact/"+contact.getcId()+"";
	}
	
	
	@GetMapping("/profile")
	public String yourProfile(Model model,Principal principal) {
		
		String name=principal.getName();
		User user=this.userRepository.getUserByUserName(name);
		model.addAttribute("user",user);
		
		model.addAttribute("title","Profile Page");
		return "normal/profile";
	}
	
	
	@RequestMapping("/settings")
	public String settings(Model model,Principal principal) {
		
		String userName=principal.getName();
		
		User user = userRepository.getUserByUserName(userName);
		
		model.addAttribute("user", user);
		model.addAttribute("title","Settings");
		return "normal/settings";
	}
	
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,Model model,HttpSession session,Principal principal) {
		
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		System.out.println(user.getPassword());
		
		if(this.passwordEncoder.matches(oldPassword,user.getPassword())) {
			
			user.setPassword(this.passwordEncoder.encode(newPassword));
			this.userRepository.save(user);
			session.setAttribute("message", new Message("Your password successfully changed..","alert-success"));
			
		}else {
			session.setAttribute("message", new Message("Please Enter correct old password !!","alert-danger"));
			return "redirect:/user/settings";
		}
		
		return "redirect:/user/index";
	}
	
	
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data,Principal principal) throws RazorpayException {
		
		System.out.println(data);
		
		int amt = Integer.parseInt(data.get("amount").toString());
		
		var client=new RazorpayClient("rzp_test_CzxcPqUG4CiCM9","L2JWHQbJWNfeA1WeI7jqKiLI");
		
		JSONObject ob = new JSONObject();
		ob.put("amount", amt*100);
		ob.put("currency", "INR");
		ob.put("receipt", "txn_234567");
		
		Order order = client.Orders.create(ob);
		System.out.println(order);
		
		//save the order in database
		MyOrder myOrder = new MyOrder();
		myOrder.setAmount(order.get("amount"));
		myOrder.setOrderId(order.get("id"));
		myOrder.setStatus(order.get("status"));
		myOrder.setPaymentId(null);
		myOrder.setReceipt(order.get("receipt"));
		myOrder.setUser(this.userRepository.getUserByUserName(principal.getName()));
		this.myOrderRepository.save(myOrder);
		
		return order.toString();
	}
	
	
	@PostMapping("/update_order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data){
		
		MyOrder myorder = this.myOrderRepository.findByOrderId(data.get("Order_id").toString());
		
		myorder.setPaymentId(data.get("Payment_id").toString());
		myorder.setStatus(data.get("Status").toString());
		
		this.myOrderRepository.save(myorder);
		
		return ResponseEntity.ok(Map.of("msg","updated"));
		
		
	}

}
