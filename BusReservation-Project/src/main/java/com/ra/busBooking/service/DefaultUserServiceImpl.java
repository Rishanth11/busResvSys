package com.ra.busBooking.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.DocumentException;
import com.ra.busBooking.DTO.BookingsDTO;
import com.ra.busBooking.DTO.UserRegisteredDTO;
import com.ra.busBooking.model.Bookings;
import com.ra.busBooking.model.Role;
import com.ra.busBooking.model.User;
import com.ra.busBooking.repository.BookingsRepository;
import com.ra.busBooking.repository.RoleRepository;
import com.ra.busBooking.repository.UserRepository;
import java.time.format.DateTimeFormatter;

@Service
public class DefaultUserServiceImpl implements DefaultUserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private BookingsRepository bookingRepository;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService; // ✅ Added

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("Invalid username or password.");
        }
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                mapRolesToAuthorities(user.getRole()));
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Set<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getRole()))
                .collect(Collectors.toList());
    }

    @Override
    public User save(UserRegisteredDTO userRegisteredDTO) {
        Role role = userRegisteredDTO.getRole().equals("USER")
                ? roleRepo.findByRole("USER")
                : roleRepo.findByRole("ADMIN");

        User user = new User();
        user.setEmail(userRegisteredDTO.getEmail_id());
        user.setName(userRegisteredDTO.getName());
        user.setPassword(passwordEncoder.encode(userRegisteredDTO.getPassword()));
        user.setRole(role);

        return userRepo.save(user);
    }

    @Override
    public Bookings updateBookings(BookingsDTO bookingDTO, User user) {
        Bookings booking = new Bookings();

        booking.setBusName(bookingDTO.getBusName());
        booking.setFilterDate(bookingDTO.getFilterDate());
        booking.setFromDestination(bookingDTO.getFromDestination());
        booking.setToDestination(bookingDTO.getToDestination());
        booking.setNoOfPersons(bookingDTO.getNoOfPersons());
        booking.setTotalCalculated(bookingDTO.getTotalCalculated());
        booking.setTime(bookingDTO.getTime());
        booking.setUserId(user.getId());
        booking.setTripStatus(true);
        booking.setPrice(bookingDTO.getPrice());

        String filename = generatePDFAndSendMail(bookingDTO, user);
        booking.setFileName(filename);

        return bookingRepository.save(booking);
    }

    private String generatePDFAndSendMail(BookingsDTO bookingDTO, User user) {
        int random = (int) (Math.random() * 90) + 10;
        String nameGenrator = user.getName() + "_ticket_" + random + ".pdf";
        try {
            createPdf(bookingDTO, user, nameGenrator);
            emailService.sendPdfEmail(user.getEmail(), new File(nameGenrator)); // ✅ Updated
            return nameGenrator;
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void createPdf(BookingsDTO booking, User user, String nameGenrator) throws DocumentException, IOException {
        Context context = new Context();
        context.setVariable("name", user.getName());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        if (booking.getFilterDate() != null) {
            context.setVariable("date", booking.getFilterDate().format(formatter));
        } else {
            context.setVariable("date", "N/A"); // or leave it empty if you prefer
        }

        context.setVariable("noOfPass", booking.getNoOfPersons());
        context.setVariable("From", booking.getFromDestination());
        context.setVariable("to", booking.getToDestination());
        context.setVariable("busName", booking.getBusName());

        String processHTML = templateEngine.process("gmail", context);

        try (OutputStream out = new FileOutputStream(nameGenrator)) {
            ITextRenderer ir = new ITextRenderer();
            ir.setDocumentFromString(processHTML);
            ir.layout();
            ir.createPDF(out, false);
            ir.finishPDF();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
