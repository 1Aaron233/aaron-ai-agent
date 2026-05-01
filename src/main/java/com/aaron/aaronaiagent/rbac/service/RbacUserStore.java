package com.aaron.aaronaiagent.rbac.service;

import com.aaron.aaronaiagent.rbac.model.RbacMenu;
import com.aaron.aaronaiagent.rbac.model.RbacRole;
import com.aaron.aaronaiagent.rbac.model.RbacUser;

import java.util.List;
import java.util.Optional;

public interface RbacUserStore {

    Optional<RbacUser> findByUsername(String username);

    Optional<RbacUser> findById(Long userId);

    List<RbacUser> findAllUsers();

    List<RbacRole> findAllRoles();

    List<RbacMenu> findMenusByUserId(Long userId);
}
