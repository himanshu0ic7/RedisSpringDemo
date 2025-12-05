package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(TutorialService.class);

    public static final String CACHE_TUTORIAL = "tutorial";
    public static final String CACHE_LIST = "tutorials_list";
    public static final String CACHE_SEARCH = "tutorials_search";
    public static final String CACHE_PUBLISHED = "tutorials_published";

    @Cacheable(value = CACHE_LIST)
    public List<Tutorial> findAll() {
        logger.info("Cache MISS: Fetching all tutorials from Database...");
        doLongRunningTask();
        return tutorialRepository.findAll();
    }

    @Cacheable(value = CACHE_SEARCH, key = "#title")
    public List<Tutorial> findByTitleContaining(String title) {
        logger.info("Cache MISS: Searching for '{}' in Database...", title);
        doLongRunningTask();
        return tutorialRepository.findByTitleContaining(title);
    }

    @Cacheable(value = CACHE_TUTORIAL, key = "#id", unless = "#result == null")
    public Tutorial findById(String id) {
        logger.info("Cache MISS: Fetching ID {} from Database...", id);
        doLongRunningTask();
        return tutorialRepository.findById(id).orElse(null);
    }

    @Cacheable(value = CACHE_PUBLISHED, key = "#isPublished")
    public List<Tutorial> findByPublished(boolean isPublished) {
        logger.info("Cache MISS: Fetching published status {} from Database...", isPublished);
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
        logger.info("Saving new tutorial to DB and clearing Lists Cache...");
        return tutorialRepository.save(tutorial);
    }

    @CachePut(value = CACHE_TUTORIAL, key = "#id")
    @Caching(evict = {
        @CacheEvict(value = CACHE_LIST, allEntries = true),
        @CacheEvict(value = CACHE_SEARCH, allEntries = true),
        @CacheEvict(value = CACHE_PUBLISHED, allEntries = true)
    })
    public Tutorial update(String id, Tutorial tutorial) {
        logger.info("Updating ID {} in DB and clearing Lists Cache...", id);
        Optional<Tutorial> existingData = tutorialRepository.findById(id);

        if (existingData.isPresent()) {
            tutorial.setId(id);
            return tutorialRepository.save(tutorial);
        }
        return null;
    }

    @Caching(evict = {
        @CacheEvict(value = CACHE_TUTORIAL, key = "#id"),
        @CacheEvict(value = CACHE_LIST, allEntries = true),
        @CacheEvict(value = CACHE_SEARCH, allEntries = true),
        @CacheEvict(value = CACHE_PUBLISHED, allEntries = true)
    })
    public void deleteById(String id) {
        logger.info("Deleting ID {} from DB and Evicting Cache...", id);
        tutorialRepository.deleteById(id);
    }

    @CacheEvict(value = { CACHE_TUTORIAL, CACHE_LIST, CACHE_SEARCH, CACHE_PUBLISHED }, allEntries = true)
    public void deleteAll() {
        logger.info("Deleting ALL data and Emptying Cache...");
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