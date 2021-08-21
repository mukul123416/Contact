package com.smart.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="CONTACT")
public class Contact {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int cId;
	
	@NotBlank(message="User name can not be empty !!")
	@Size(min=3,max=12,message="User name must be between 3 - 12 characters !!")
	private String name;
	
	@NotBlank(message="Nick name can not be empty !!")
	@Size(min=3,max=12,message="User name must be between 3 - 12 characters !!")
	private String secondName;
	
	@NotBlank(message="Work name can not be empty !!")
	@Size(min=3,max=50,message="Work name must be between 3 - 50 characters !!")
	private String work;
	
	@NotBlank(message="Email can not be empty !!")
	@Email(regexp="^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$")
	private String email;
	
	@NotBlank(message="Phone number can not be empty !!")
	private String phone;
	
	private String image;
	
	@NotBlank(message="Description can not be empty !!")
	@Size(min=10,max=1000,message="Description must be between 10 - 1000 characters !!")
	@Column(length = 1000)
	private String description;
	
	@ManyToOne
	@JsonIgnore
	private User user;
	
	public Contact() {
		super();
		// TODO Auto-generated constructor stub
	}

	public int getcId() {
		return cId;
	}

	public void setcId(int cId) {
		this.cId = cId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSecondName() {
		return secondName;
	}

	public void setSecondName(String secondName) {
		this.secondName = secondName;
	}

	public String getWork() {
		return work;
	}

	public void setWork(String work) {
		this.work = work;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	

}
