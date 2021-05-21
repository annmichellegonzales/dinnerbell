package com.example.dinnerbell.models;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "images")
public class Image {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(nullable = false)
  private String url;

  @OneToOne
  private Restaurant restaurant;

  @ManyToMany(mappedBy = "images")
  private List<Review> reviews;

  public Image() {
  }

  public Image(long id, String url, Restaurant restaurant) {
    this.id = id;
    this.url = url;
    this.restaurant = restaurant;
  }

  public Image(String url, Restaurant restaurant) {
    this.url = url;
    this.restaurant = restaurant;
  }

  public List<Review> getRestaurantReviews() {
    return reviews;
  }

  public void setRestaurantReviews(List<Review> reviews) {
    this.reviews = reviews;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Restaurant getRestaurant() {
    return restaurant;
  }

  public void setRestaurant(Restaurant restaurant) {
    this.restaurant = restaurant;
  }
}