package com.prettygoodorg.pluto_api.api.auth.repository;

import com.prettygoodorg.pluto_api.api.auth.entity.PendingUser;
import org.springframework.data.repository.CrudRepository;

public interface PendingUserRepository extends CrudRepository<PendingUser, String> {}
