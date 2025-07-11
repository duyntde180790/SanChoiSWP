package com.example.SanChoi247.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.SanChoi247.model.entity.Food;
import com.example.SanChoi247.model.entity.User;
import com.example.SanChoi247.service.FoodService;

import jakarta.servlet.http.HttpSession;

@Controller
public class FoodController {
    
    @Autowired
    private FoodService foodService;
    
    // View all food items for an owner
    @GetMapping("/owner/food-drinks")
    public String viewAllFoods(Model model, HttpSession session) {
        try {
            User user = (User) session.getAttribute("UserAfterLogin");
            if (user == null || user.getRole() != 'C') {
                return "redirect:/Login";
            }
            
            List<Food> foodItems = foodService.getAllFoodsByOwnerId(user.getUid());
            model.addAttribute("foodItems", foodItems);
            return "owner/manageFoodDrinks";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to load food and drinks: " + e.getMessage());
            return "owner/manageFoodDrinks"; // Return the same template with error message instead of 500 error
        }
    }
    
    // Show form to add new food/drink
    @GetMapping("/owner/food-drinks/add")
    public String showAddFoodForm(HttpSession session) {
        User user = (User) session.getAttribute("UserAfterLogin");
        if (user == null || user.getRole() != 'C') {
            return "redirect:/Login";
        }
        return "owner/addFoodDrink";
    }
}