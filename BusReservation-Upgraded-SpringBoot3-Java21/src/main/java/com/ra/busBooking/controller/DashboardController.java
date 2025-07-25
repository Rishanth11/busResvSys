package com.ra.busBooking.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ra.busBooking.DTO.BookingsDTO;
import com.ra.busBooking.DTO.ReservationDTO;
import com.ra.busBooking.helper.ObjectCreationHelper;
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
    public String bookPage(@PathVariable int id,
                           @RequestParam("date") String travelDate,
                           Model model) {
        Optional<BusData> busDataOpt = busDataRepository.findById(id);
        if (busDataOpt.isEmpty()) {
            return "redirect:/dashboard";
        }

        BookingsDTO bookingDTO = ObjectCreationHelper.createBookingsDTO(busDataOpt.get());

        // ✅ Set the selected date passed from dashboard
        bookingDTO.setFilterDate(LocalDate.parse(travelDate)); 

        model.addAttribute("userDetails", returnUsername());
        model.addAttribute("record", bookingDTO);
        return "book";
    }


    @PostMapping("/booking")
    public String finalBooking(@ModelAttribute("record") BookingsDTO bookingDTO, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName(); // safe for all cases
        User foundUser = userRepository.findByEmail(username);

        if (foundUser != null) {
            userService.updateBookings(bookingDTO, foundUser);
        }

        return "redirect:/myBooking";
    }

    private String returnUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // always safe
        User foundUser = userRepository.findByEmail(username);
        return foundUser != null ? foundUser.getName() : "User";
    }
}