package com.smart.controllers;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.models.Contact;
import com.smart.models.User;

@RestController
@CrossOrigin
public class SearchController {
	
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@GetMapping("/search/{query}")
	public ResponseEntity<?> search(@PathVariable("query") String query,Principal principal,Model model){
		
		System.out.println(query);
		
		String name=principal.getName();
		User user=this.userRepository.getUserByUserName(name);
		
		List<Contact> contacts = this.contactRepository.findByNameContainingAndUser(query, user);
		
		return ResponseEntity.ok(contacts);
	}

}
