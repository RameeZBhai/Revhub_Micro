package com.revhub.post.repository;

import com.revhub.post.entity.Post;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {
    // Standard MongoRepository methods are sufficient for String ID
}
