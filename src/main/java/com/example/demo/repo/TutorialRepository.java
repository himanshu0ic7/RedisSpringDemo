package com.example.demo.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Tutorial;

public interface TutorialRepository extends JpaRepository<Tutorial, String>{
	List<Tutorial>  findByPublished(Boolean published);
	List<Tutorial> findByTitleContaining(String title);
}
