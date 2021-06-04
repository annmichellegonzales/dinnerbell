package com.example.dinnerbell.controllers;

import com.example.dinnerbell.models.*;
import com.example.dinnerbell.repositories.CategoryRepo;
import com.example.dinnerbell.repositories.ImageRepo;
import com.example.dinnerbell.repositories.RestaurantRepo;
import com.example.dinnerbell.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;


@Controller
public class RestaurantController {

    private final RestaurantRepo restaurantsdao;
    private final CategoryRepo categoriesdao;
    private final UserRepo usersdao;
    private final ImageRepo imageDao;

  @Value("${file_upload_path}")
  private String uploadPath;

    public RestaurantController(RestaurantRepo restaurantsdao, CategoryRepo categoriesdao, UserRepo usersdao, ImageRepo imageDao) {
        this.restaurantsdao = restaurantsdao;
        this.categoriesdao = categoriesdao;
        this.usersdao = usersdao;
        this.imageDao = imageDao;
    }


    @GetMapping("/restaurant/create")
    public String showCreateRestaurantForm(Model model) {
      model.addAttribute("restaurant", new Restaurant());
      model.addAttribute("categories", categoriesdao.findAll());
        return "business/create_account";
    }

  @PostMapping("/restaurant/create")
  public String createRestaurant(@ModelAttribute Restaurant restaurant,@RequestParam(name = "categories")List<Category> categories){
    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User creator = usersdao.getOne(user.getId());
    restaurant.setCategories(categories);
    restaurantsdao.save(restaurant);
    creator.setRestaurant(restaurant);
    usersdao.save(creator);
    return "redirect:/restaurant/details/" + restaurant.getId();
  }


    @GetMapping("/restaurant")
    public String restaurantProfile(Model model) {
      model.addAttribute("restaurants",restaurantsdao.findAll());
      User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      User currentUser = usersdao.getOne(user.getId());
      List<Restaurant> restaurants = restaurantsdao.findAll();
      model.addAttribute("restaurants",restaurants);
      model.addAttribute("restaurant", restaurantsdao.getOne(currentUser.getRestaurant().getId()));
      model.addAttribute("user",currentUser);

      return "business/restaurant-profile";

    }

    @GetMapping("/restaurant/upload/{id}")
    public String uploadRestaurantPhotoForm(@PathVariable("id") long id, Model model){
      Restaurant restaurant = restaurantsdao.getOne(id);
      model.addAttribute("restaurant",restaurant);
      return "business/upload-restaurant-photo";
    }

    @PostMapping("/restaurant/upload/{id}")
    public String UploadRestaurantPhotoResult(@ModelAttribute Restaurant restaurant,@RequestParam(name = "file") MultipartFile uploadedFile, Model model) throws NullPointerException{
      Image image = new Image();
    if(!uploadedFile.getOriginalFilename().isEmpty()){
      String filename = uploadedFile.getOriginalFilename().replace(" ","_").toLowerCase();
      String filepath = Paths.get(uploadPath,filename).toString();
      File destinationFile = new File(filepath);
      try {
        uploadedFile.transferTo(destinationFile);
        model.addAttribute("message","File successfully uploaded");
      } catch (IOException e) {
        e.printStackTrace();
        model.addAttribute("message","Oops! Something went wrong!" + e);
      }
      image.setUrl(filename);
    }
    image.setRestaurant(restaurant);
    imageDao.save(image);

    return "redirect:/restaurant/details/" + restaurant.getId();
    }

    @GetMapping("/restaurant/details/{id}")
    public String restaurants(@PathVariable long id, Model model){
        Restaurant restaurant = restaurantsdao.getOne(id);
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = usersdao.getOne(user.getId());
        List<Image> imagesForRestaurant = imageDao.findImageByRestaurant(restaurant);

        model.addAttribute("images", imagesForRestaurant);
        model.addAttribute("restaurant", restaurant);
        model.addAttribute("user",currentUser);
        return "business/details";
    }

//@GetMapping("/random-selector")
//  public String randomSelector() {
//      return "business/randomSelector";
//  }







    @PostMapping("/restaurant/details/{id}")
  public String addToFavorites(@PathVariable("id") long id){
      User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      User currentUser = usersdao.getOne(user.getId());
      Restaurant restaurant = restaurantsdao.getOne(id);
      List<User> favorites = restaurant.getFavorites();

      if (!favorites.contains(currentUser)){
        favorites.add(currentUser);
        restaurant.setFavorites(favorites);
        restaurantsdao.save(restaurant);
      }
      else{
        restaurant.setFavorites(null);
        restaurantsdao.save(restaurant);
      }
      return "redirect:/restaurant/details/" + restaurant.getId();
  }

    @GetMapping("/restaurant/edit/{id}")
    public String updateRestaurantProfileForm(Model model, @PathVariable Long id) {
        Restaurant restaurant = restaurantsdao.getOne(id);

        model.addAttribute("categories", categoriesdao.findAll());
        model.addAttribute("restaurant", restaurant);
        return "business/edit-restaurant-profile";
    }


    @PostMapping("/restaurant/edit/{id}")
    public String updateRestaurantProfileResults(@ModelAttribute("restaurant") Restaurant restaurant,
                                                 @RequestParam(name = "categories")List<Category> categories) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User creator = usersdao.getOne(currentUser.getId());
        Restaurant restaurantToEdit = restaurantsdao.getOne(restaurant.getId());

        restaurantToEdit.setCategories(categories);
        restaurantToEdit.setRestaurant_name(restaurant.getRestaurant_name());
        restaurantToEdit.setStreet_address(restaurant.getStreet_address());
        restaurantToEdit.setCity(restaurant.getCity());
        restaurantToEdit.setState(restaurant.getState());
        restaurantToEdit.setZip_code(restaurant.getZip_code());
        restaurantToEdit.setPhone_number(restaurant.getPhone_number());
        restaurantToEdit.setWebsite_link(restaurant.getWebsite_link());
        restaurantToEdit.setMenu_link(restaurant.getMenu_link());
        restaurantToEdit.setHours(restaurant.getHours());
        restaurantToEdit.setDescription(restaurant.getDescription());
        restaurantToEdit.setElder_eats_link(restaurant.getElder_eats_link());
        restaurantsdao.save(restaurantToEdit);
        creator.setRestaurant(currentUser.getRestaurant());
        usersdao.save(creator);
        return "redirect:/restaurant/details/" + restaurant.getId();
    }

      //    this was used to make sure the delete a
      //    single picture uploaded by a restaurant owner worked
  @GetMapping("/restaurant/uploads/show/{id}")
  public String testDelete(@PathVariable long id, Model model){
      Restaurant restaurant = restaurantsdao.getOne(id);
      model.addAttribute("restaurant", restaurant);
      return "business/uploadsByRestaurants";
  }

  @GetMapping("/restaurant/delete/upload/{id}")
  public String deleteUploadedRestaurantImages(@PathVariable("id") long id){
      Image image = imageDao.getOne(id);
      imageDao.deleteById(image.getId());
    return "redirect:/restaurant/details/" + image.getRestaurant().getId();
  }

  @GetMapping("/restaurants/exclusives")
  public String showAllRestaurants(Model model) {
      List<Restaurant> restaurants = restaurantsdao.findAll();
      List<Category> categories = categoriesdao.findAll();

      model.addAttribute("restaurants",restaurants);
      model.addAttribute("categories", categories);
      return "post/exclusives";
  }



    }



