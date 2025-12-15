package com.revhub.follow.repository;

import com.revhub.follow.model.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    Page<Follow> findByFollowerUsername(String followerUsername, Pageable pageable);

    Page<Follow> findByFollowingUsername(String followingUsername, Pageable pageable);

    List<Follow> findByFollowerUsername(String followerUsername);

    List<Follow> findByFollowingUsername(String followingUsername);

    boolean existsByFollowerUsernameAndFollowingUsername(String followerUsername, String followingUsername);

    void deleteByFollowerUsernameAndFollowingUsername(String followerUsername, String followingUsername);

    long countByFollowerUsername(String followerUsername);

    long countByFollowingUsername(String followingUsername);
}
