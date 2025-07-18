package com.ra.busBooking.service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

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

    // ✅ Use the PasswordEncoder interface, not BCrypt directly
    @Autowired
    private PasswordEncoder passwordEncoder;

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
        Role role;
        if (userRegisteredDTO.getRole().equals("USER")) {
            role = roleRepo.findByRole("USER");
        } else {
            role = roleRepo.findByRole("ADMIN");
        }

        User user = new User();
        user.setEmail(userRegisteredDTO.getEmail_id());
        user.setName(userRegisteredDTO.getName());
        user.setPassword(passwordEncoder.encode(userRegisteredDTO.getPassword()));
        user.setRole(role);

        return userRepo.save(user);
    }

    @Override
    public Bookings updateBookings(BookingsDTO bookingDTO, UserDetails user) {
        Bookings booking = new Bookings();
        String email = user.getUsername();
        User users = userRepo.findByEmail(email);
        booking.setBusName(bookingDTO.getBusName());
        booking.setFilterDate(bookingDTO.getFilterDate());
        booking.setFromDestination(bookingDTO.getFromDestination());
        booking.setToDestination(bookingDTO.getToDestination());
        booking.setNoOfPersons(bookingDTO.getNoOfPersons());
        booking.setTotalCalculated(bookingDTO.getTotalCalculated());
        booking.setTime(bookingDTO.getTime());
        booking.setUserId(users.getId());
        booking.setTripStatus(true);
        String filename = generatePDFAndSendMail(bookingDTO, users);
        booking.setFileName(filename);
        return bookingRepository.save(booking);
    }

    private String generatePDFAndSendMail(BookingsDTO bookingDTO, User users) {
        int random = (int) (Math.random() * 90) + 10;
        String nameGenrator = users.getName() + "_ticket_" + random + ".pdf";
        try {
            createPdf(bookingDTO, users, nameGenrator);
            sendEmail(bookingDTO, users, nameGenrator);
            return nameGenrator;
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void sendEmail(BookingsDTO bookingDTO, User users, String nameGenrator) {
        try {
            final String username = ""; // sender email
            final String password = ""; // app password

            Properties props = new Properties();
            props.put("mail.smtp.auth", true);
            props.put("mail.smtp.starttls.enable", true);
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props,
                    new jakarta.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(users.getEmail()));
            message.setSubject("Bus Ticket");

            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("Your ticket is attached.");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            messageBodyPart = new MimeBodyPart();
            String filename = nameGenrator;
            DataSource source = new FileDataSource(filename);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(filename);
            multipart.addBodyPart(messageBodyPart);

            message.setContent(multipart);

            Transport.send(message);

            System.out.println("Sent message successfully.");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void createPdf(BookingsDTO booking, User users, String nameGenrator) throws DocumentException, IOException {
        Context context = new Context();
        context.setVariable("name", users.getName());
        context.setVariable("date", booking.getFilterDate());
        context.setVariable("noOfPass", booking.getNoOfPersons());
        context.setVariable("From", booking.getFromDestination());
        context.setVariable("to", booking.getToDestination());
        context.setVariable("busName", booking.getBusName());

        String processHTML = templateEngine.process("template", context);

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
