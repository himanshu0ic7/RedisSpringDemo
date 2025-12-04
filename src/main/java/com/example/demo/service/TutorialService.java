package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import com.example.demo.model.Tutorial;
import com.example.demo.repo.TutorialRepository;

@Service
public class TutorialService {

    @Autowired
    private TutorialRepository tutorialRepository;

    public static final String CACHE_TUTORIAL = "tutorial";
    public static final String CACHE_LIST = "tutorials_list";
    public static final String CACHE_SEARCH = "tutorials_search";
    public static final String CACHE_PUBLISHED = "tutorials_published";

    @Cacheable(value = CACHE_LIST)
    public List<Tutorial> findAll() {
        doLongRunningTask();
        return tutorialRepository.findAll();
    }

    @Cacheable(value = CACHE_SEARCH, key = "#title")
    public List<Tutorial> findByTitleContaining(String title) {
        doLongRunningTask();
        return tutorialRepository.findByTitleContaining(title);
    }

    @Cacheable(value = CACHE_TUTORIAL, key = "#id")
    public Optional<Tutorial> findById(String id) {
        doLongRunningTask();
        return tutorialRepository.findById(id);
    }

    @Cacheable(value = CACHE_PUBLISHED, key = "#isPublished")
    public List<Tutorial> findByPublished(boolean isPublished) {
        doLongRunningTask();
        return tutorialRepository.findByPublished(isPublished);
    }

    @CachePut(value = CACHE_TUTORIAL, key = "#result.id")
    @Caching(evict = {
        @CacheEvict(value = CACHE_LIST, allEntries = true),
        @CacheEvict(value = CACHE_SEARCH, allEntries = true),
        @CacheEvict(value = CACHE_PUBLISHED, allEntries = true)
    })
    public Tutorial save(Tutorial tutorial) {
        return tutorialRepository.save(tutorial);
    }
    
    @CachePut(value = CACHE_TUTORIAL, key = "#id")
    @Caching(evict = {
        @CacheEvict(value = CACHE_LIST, allEntries = true),
        @CacheEvict(value = CACHE_SEARCH, allEntries = true),
        @CacheEvict(value = CACHE_PUBLISHED, allEntries = true)
    })
    public Optional<Tutorial> update(String id, Tutorial tutorial) {
        return tutorialRepository.findById(id).map(existing -> {
            tutorial.setId(id);
            return tutorialRepository.save(tutorial);
        });
    }

    @Caching(evict = {
        @CacheEvict(value = CACHE_TUTORIAL, key = "#id"),
        @CacheEvict(value = CACHE_LIST, allEntries = true),
        @CacheEvict(value = CACHE_SEARCH, allEntries = true),
        @CacheEvict(value = CACHE_PUBLISHED, allEntries = true)
    })
    public void deleteById(String id) {
        tutorialRepository.deleteById(id);
    }

    @CacheEvict(value = { CACHE_TUTORIAL, CACHE_LIST, CACHE_SEARCH, CACHE_PUBLISHED }, allEntries = true)
    public void deleteAll() {
        tutorialRepository.deleteAll();
    }

    private void doLongRunningTask() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}