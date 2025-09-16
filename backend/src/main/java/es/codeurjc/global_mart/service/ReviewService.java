package es.codeurjc.global_mart.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.codeurjc.global_mart.dto.Reviewss.ReviewDTO;
import es.codeurjc.global_mart.dto.Reviewss.ReviewMapper;
import es.codeurjc.global_mart.model.Review;
import es.codeurjc.global_mart.repository.ReviewRepository;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewMapper reviewMapper;

    public ReviewDTO createReview(String username, String comment, int calification) {
        Review review = new Review(username, comment, calification);
        System.out.println("Review created: " + review);
        reviewRepository.save(review);
        return reviewMapper.toReviewDTO(review);
    }

    public ReviewDTO addReview(ReviewDTO reviewDTO, String username) {
        Review review = new Review(username, reviewDTO.comment(), reviewDTO.calification());
        reviewRepository.save(review);
        return reviewMapper.toReviewDTO(review);
    }

    public List<ReviewDTO> getAllReviews() {
        return reviewMapper.toReviewsDTO(reviewRepository.findAll());
    }

    public Optional<ReviewDTO> getReviewById(Long id) {
        return reviewRepository.findById(id).map(reviewMapper::toReviewDTO);
    }

    public ReviewDTO updateReview(Long id, String comment, int calification) {
        Optional<Review> optionalReview = reviewRepository.findById(id);
        if (optionalReview.isPresent()) {
            Review review = optionalReview.get();
            review.setComment(comment);
            review.setCalification(calification);
            reviewRepository.save(review);
            return reviewMapper.toReviewDTO(review);
        } else {
            throw new RuntimeException("Review not found with id " + id);
        }
    }

    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }

}