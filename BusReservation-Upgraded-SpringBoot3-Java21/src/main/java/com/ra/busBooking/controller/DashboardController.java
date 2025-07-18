package com.ra.busBooking.controller;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.ra.busBooking.DTO.BookingsDTO;
import com.ra.busBooking.DTO.ReservationDTO;
import com.ra.busBooking.helper.ObjectCreationHelper;
import com.ra.busBooking.model.Bookings;
import com.ra.busBooking.model.BusData;
import com.ra.busBooking.model.User;
import com.ra.busBooking.repository.BookingsRepository;
import com.ra.busBooking.repository.BusDataRepository;
import com.ra.busBooking.repository.UserRepository;
import com.ra.busBooking.service.DefaultUserService;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final DefaultUserService userService;

    @Autowired
    private BookingsRepository bookingsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusDataRepository busDataRepository;

    public DashboardController(DefaultUserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("reservation")
    public ReservationDTO reservationDTO() {
        return new ReservationDTO();
    }

    @GetMapping
    public String displayDashboard(Model model) {
        model.addAttribute("userDetails", returnUsername());
        return "dashboard";
    }

    @PostMapping
    public String filterBusData(@ModelAttribute("reservation") ReservationDTO reservationDTO, Model model) {
        System.out.println("Form submitted with values:");
        System.out.println("From: " + reservationDTO.getFrom());
        System.out.println("To: " + reservationDTO.getTo());
        System.out.println("Date: " + reservationDTO.getFilterDate());

        List<BusData> busData = busDataRepository.findByToFromAndDate(
                reservationDTO.getTo(),
                reservationDTO.getFrom(),
                reservationDTO.getFilterDate()
        );

        if (busData == null) {
            busData = Collections.emptyList(); // Avoid null in view
        }

        System.out.println("Buses found: " + busData.size());

        model.addAttribute("userDetails", returnUsername());
        model.addAttribute("busData", busData);
        model.addAttribute("reservation", reservationDTO);
        return "dashboard";
    }

    @GetMapping("/book/{id}")
    public String bookPage(@PathVariable int id, Model model) {
        Optional<BusData> busDataOpt = busDataRepository.findById(id);
        if (busDataOpt.isEmpty()) {
            return "redirect:/dashboard";
        }

        BookingsDTO bookingDTO = ObjectCreationHelper.createBookingsDTO(busDataOpt.get());

        model.addAttribute("userDetails", returnUsername());
        model.addAttribute("record", bookingDTO);
        return "book";
    }

    @PostMapping("/booking")
    public String finalBooking(@ModelAttribute("record") BookingsDTO bookingDTO, Model model) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.updateBookings(bookingDTO, user);
        return "redirect:/myBooking";
    }

    private String returnUsername() {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User foundUser = userRepository.findByEmail(user.getUsername());
        return foundUser != null ? foundUser.getName() : "User";
    }
}
