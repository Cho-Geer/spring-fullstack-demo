package com.example.demo_mysql;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;
    
    public Role savRole(Role role){
        return roleRepository.save(role);
    }

    public void deleteRole(Long roleId){
        roleRepository.deleteById(roleId);
    }

    public List<Role> getAllRoles(){
        return roleRepository.findAll();
    }

    public Role findByRoleName(String roleName){
        return roleRepository.findByRoleName(roleName).orElse(null);
    }
}
