package com.movieplatform.Controller;

import com.movieplatform.Entity.Movie;
import com.movieplatform.Entity.Payment;
import com.movieplatform.Repository.MovieRepository;
import com.movieplatform.Repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RequestMapping(path = "payments")
@RestController
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;


    @GetMapping
    public List<Payment> getall(){
        return paymentRepository.findAll();
    }


    @PostMapping
    public Payment create(@RequestBody Payment payment) {
        return paymentRepository.save(payment);
    }



}
