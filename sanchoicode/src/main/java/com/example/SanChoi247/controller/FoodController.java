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

    @GetMapping("/owner/food-drinks/edit/{id}")
    public String showEditFoodForm(@PathVariable("id") int foodId, Model model, HttpSession session) {
        try {
            User user = (User) session.getAttribute("UserAfterLogin");
            if (user == null || user.getRole() != 'C') {
                return "redirect:/Login";
            }
            
            Food food = foodService.getFoodById(foodId);
            
            // Check if the food belongs to the logged-in owner
            if (food == null || food.getOwner().getUid() != user.getUid()) {
                return "redirect:/owner/food-drinks?error=unauthorized";
            }
            
            model.addAttribute("food", food);
            return "owner/editFoodDrink";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to load food/drink details");
            return "redirect:/owner/food-drinks";
        }
    }
    
    // Process editing food/drink
    @PostMapping("/owner/food-drinks/edit/{id}")
    public String updateFood(
            @PathVariable("id") int foodId,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") double price,
            @RequestParam("category") String category,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @RequestParam(value = "available", required = false, defaultValue = "false") boolean available,
            HttpSession session,
            Model model) {
        try {
            User user = (User) session.getAttribute("UserAfterLogin");
            if (user == null || user.getRole() != 'C') {
                return "redirect:/Login";
            }
            
            Food existingFood = foodService.getFoodById(foodId);
            
            // Check if the food belongs to the logged-in owner
            if (existingFood == null || existingFood.getOwner().getUid() != user.getUid()) {
                return "redirect:/owner/food-drinks?error=unauthorized";
            }
            
            foodService.updateFood(foodId, name, description, price, category, imageFile, available);
            return "redirect:/owner/food-drinks?success=updated";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to update food/drink");
            model.addAttribute("foodId", foodId);
            return "redirect:/owner/food-drinks/edit/" + foodId;
        }
    }
    
    // Delete food/drink
    @PostMapping("/owner/food-drinks/delete/{id}")
    public ResponseEntity<String> deleteFood(@PathVariable("id") int foodId, HttpSession session) {
        try {
            User user = (User) session.getAttribute("UserAfterLogin");
            if (user == null || user.getRole() != 'C') {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login required");
            }
            
            Food existingFood = foodService.getFoodById(foodId);
            
            // Check if the food belongs to the logged-in owner
            if (existingFood == null || existingFood.getOwner().getUid() != user.getUid()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized");
            }
            
            foodService.deleteFood(foodId);
            return ResponseEntity.ok("Food/drink deleted successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete");
        }
    }
}