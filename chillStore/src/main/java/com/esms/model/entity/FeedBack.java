/** package com.esms.model.entity;

 import jakarta.persistence.*;

 import java.util.Date;

 @Entity
 @Table(name = "feedback")
 public class Feedback {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 @Column(name = "feedback_id")
 private int feedback_id;
 @Column(name = "customer_id")
 private int customer_id;
 @Column(name = "product_id")
 private int product_id;
 @Column(name = "rating")
 private int rating;
 @Column(name = "comment")
 private String comment;
 @Column(name = "status")
 private String status;
 @Column(name = "created_at")
 private Date created_at;

 public Feedback() {
 }

 public Feedback(int feedback_id, int customer_id, int product_id, int rating, String comment, String status, Date created_at) {
 this.feedback_id = feedback_id;
 this.customer_id = customer_id;
 this.product_id = product_id;
 this.rating = rating;
 this.comment = comment;
 this.status = status;
 this.created_at = created_at;
 }

 public int getFeedback_id() {
 return feedback_id;
 }

 public void setFeedback_id(int feedback_id) {
 this.feedback_id = feedback_id;
 }

 public int getCustomer_id() {
 return customer_id;
 }

 public void setCustomer_id(int customer_id) {
 this.customer_id = customer_id;
 }

 public int getProduct_id() {
 return product_id;
 }

 public void setProduct_id(int product_id) {
 this.product_id = product_id;
 }

 public int getRating() {
 return rating;
 }

 public void setRating(int rating) {
 this.rating = rating;
 }

 public String getComment() {
 return comment;
 }

 public void setComment(String comment) {
 this.comment = comment;
 }

 public String getStatus() {
 return status;
 }

 public void setStatus(String status) {
 this.status = status;
 }

 public Date getCreated_at() {
 return created_at;
 }

 public void setCreated_at(Date created_at) {
 this.created_at = created_at;
 }
 } */
