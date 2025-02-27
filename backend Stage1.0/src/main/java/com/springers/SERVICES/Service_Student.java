package com.springers.SERVICES;

import org.springframework.transaction.annotation.Transactional;
import com.springers.ENTITIES.AccountStatus;
import com.springers.ENTITIES.Offer;
import com.springers.ENTITIES.OfferApplication;
import com.springers.ENTITIES.OfferApplication.OfferApplicationId;
import com.springers.ENTITIES.Specialization;
import com.springers.ENTITIES.Student;
import com.springers.ENTITIES.StudentStatus;
import com.springers.REPOSITORIES.*;

import jakarta.persistence.EntityNotFoundException;

import java.beans.Encoder;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class Service_Student implements I_Service_Student{
	@Autowired
	StudentRepo StdRepo;
	
	@Autowired
	OfferRepo offerRepo;
	
	@Autowired
	OfferApplicationRepo offerAppRepo;
	
	@Autowired
	Service_EmailSender ServiceEmail;

	@Override
	public void ajouter_Student(Student std){
	    Student std2 = StdRepo.save(std);
	    System.out.println(std2);
	}

	@Override
	public void supprimer_Student(Long id){
		StdRepo.deleteById(id);
	}

	@Override
	public List<Student> afficher_Students(){
		List<Student> students = (List<Student>) StdRepo.findAll();
		return students;
	}
	
	@Override
	public Student getStudentById(Long id) {
        return StdRepo.findById(id).orElse(null);
    }
	
	@Override
	public List<Student> searchStudentsByUsername(String usernameQuery) {
		List<Student> students = (List<Student>) StdRepo.findByUsernameContainingIgnoreCase(usernameQuery);
		return students;
    }

	@Override
	public List<Student> searchStudentsByEmail(String emailQuery) {
        return StdRepo.findByEmailContainingIgnoreCase(emailQuery);
    }

	@Override
    public List<Student> searchStudentsByTelephone(String telephoneQuery) {
        return StdRepo.findByTelephoneContainingIgnoreCase(telephoneQuery);
    }
	
	@Override
	public List<Student> searchStudentsByDateOfBirth(String dateOfBirthQuery) {
		Date dateOfBirth = Date.valueOf(dateOfBirthQuery);
        return StdRepo.findByDateOfBirth(dateOfBirth);
    }
	
	@Override
    public List<Student> filterStudents(String status, String specialization, String accountStatus, LocalDate dobMin, LocalDate dobMax) {
		// Convert string status to enum
	    StudentStatus studentStatusEnum = null;
	    if (status != null && !status.isEmpty()) {
	        studentStatusEnum = StudentStatus.valueOf(status.toUpperCase());
	    }
	    
	 // Convert string specialization to enum
	    Specialization specializationEnum = null;
	    if (specialization != null && !specialization.isEmpty()) {
	        specializationEnum = Specialization.valueOf(specialization.toUpperCase());
	    }

	    // Convert string accountStatus to enum
	    AccountStatus accountStatusEnum = null;
	    if (accountStatus != null && !accountStatus.isEmpty()) {
	        accountStatusEnum = AccountStatus.valueOf(accountStatus.toUpperCase());
	    }

	    // Call repository method with enum values
	    return StdRepo.filterStudents(studentStatusEnum, specializationEnum, accountStatusEnum, dobMin, dobMax);
	}
    
	@Override
	@Transactional
    public void suspendAccount(Long studentId) {
        // Retrieve the student entity from the database
		Student student = StdRepo.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + studentId));


        // Set the account status to SUSPENDED
        student.setAccountStatus(AccountStatus.SUSPENDED);

    }
	
	@Override
	@Transactional
	public void activateAccount(Long studentId) {
	    Student student = StdRepo.findById(studentId)
	            .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + studentId));
	    student.setAccountStatus(AccountStatus.ACTIVE);
	}
	
	@Override
	@Transactional
    public void editStudent(Long studentId, Map<String, Object> studentData) {
        // Retrieve the student entity from the database
        Student student = StdRepo.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + studentId));

     // Update the student entity with the new data
        if (studentData.containsKey("username")) {
            student.setUsername((String) studentData.get("username"));
        }
        if (studentData.containsKey("email")) {
            student.setEmail((String) studentData.get("email"));
        }
        if (studentData.containsKey("telephone")) {
            student.setTelephone((String) studentData.get("telephone"));
        }
        if (studentData.containsKey("password") && !studentData.get("password").equals("")) {
        	BCryptPasswordEncoder Bcrypt = new BCryptPasswordEncoder();
        	String password =(String) studentData.get("password");
        	System.out.print("mot de passe est :"+studentData.get("password"));
        	student.setPassword(Bcrypt.encode(password));
        	ServiceEmail.sendemail(student.getEmail(), "Modification de mot de passe de l'application Stage1.0\n ",
        			"votre mot de pass a été modifé par un administrateur\n "
            		+ "Votre nom d'utilisateur est :" + student.getUsername() + "\nVotre nouveau Mot de passe  est :" + studentData.get("password"));
            	System.out.print("email sent succesfully to :" + student.getUsername() +"/n mot de passe est :"+studentData.get("password"));
        }
        if (studentData.containsKey("dateOfBirth")) {
            // Assuming the dateOfBirth is sent as a String in the format "yyyy-MM-dd"
            LocalDate dateOfBirth = LocalDate.parse((String) studentData.get("dateOfBirth"));
            student.setDateOfBirth(dateOfBirth);
        }
        if (studentData.containsKey("studentStatus")) {
            // Convert String to StudentStatus enum
            StudentStatus studentStatus = StudentStatus.valueOf((String) studentData.get("studentStatus"));
            student.setStudentStatus(studentStatus);
        }
        if (studentData.containsKey("specialization")) {
            // Convert String to Specialization enum
            Specialization specialization = Specialization.valueOf((String) studentData.get("specialization"));
            student.setSpecialization(specialization);
        }
        StdRepo.save(student);
    }
	
	@Override
	public void ReserveOffer(Long offerId,Long studentId) {
		Offer offer = offerRepo.findById(offerId)
                .orElseThrow(() -> new EntityNotFoundException("offer not found with id: " + offerId));
		Student std = StdRepo.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found with id: " + studentId));
		
        OfferApplicationId appId = new OfferApplicationId();
        appId.setOfferId(offerId);
        appId.setStudentId(studentId);
       
        Integer NbLimite = 5;
		
        if(!(offer.getSpecialization().equals(std.getSpecialization()))) {
			System.out.printf(" les specialité <>" + studentId +"\n");
			return;
		}
		if(offerAppRepo.findById(appId).isPresent()) {
			System.out.printf("L'offre id exist deja " + offerId+"\n");
			return;
		} 
		if(!offer.getIsActive()) {
			System.out.printf("l'offre n'est pas disponible pour le moment " + offerId +"\n");
			return;
		}
		if(offer.getDeadline().isBefore(LocalDate.now())) {
			System.out.printf("Date limite a eté passé " + offerId +"\n");
			return;
		}
		if(offerAppRepo.findByStudentOfferUserId(studentId,PageRequest.of(0, 5)).size() > NbLimite) {
			System.out.printf("Nombre d'offre a depasser la limite pour student " + studentId +"\n");
			return;
		}
		if(offer.getOfferApplications().size() > offer.getNbPlaces()) {
			System.out.printf("vous ete dans la list d'attent pour cette offre \n");
		}
		OfferApplication newOfferApp = new OfferApplication();
		newOfferApp.setId(appId);
		newOfferApp.setApplicationDate(LocalDate.now());
		newOfferApp.setApplicationTime(LocalTime.now());
		
		offerAppRepo.save(newOfferApp);
		System.out.printf("offer ajouté avec success \n");
		
	}
	
	public List<Offer> AfficherListOffre(Long studentId,int page,int size) {
		List<OfferApplication> applications = offerAppRepo.findByStudentOfferUserId(studentId,PageRequest.of(page, size));
		List<Offer> offers =new ArrayList<>();
		applications.forEach(p -> {offers.add(p.getOfferApplication());});
		return offers;	
	}
	
	public List<Offer> AfficherListOffre(Long studentId) {
		List<OfferApplication> applications = offerAppRepo.findByStudentOfferUserId(studentId);
		List<Offer> offers =new ArrayList<>();
		applications.forEach(p -> {offers.add(p.getOfferApplication());});
		return offers;
	}
	
	public Integer calculateRankInOffer(Long studentId,Long offerId) {
		OfferApplicationId appId = new OfferApplicationId(offerId,studentId);
		
		OfferApplication application = offerAppRepo.findById(appId).orElse(null);
		if(application != null) {
			List<OfferApplication> applications = offerAppRepo.findByOfferApplicationId(offerId);
	        return applications.indexOf(application);
		}else {
			return -1;
		}
	}
	
	
}

