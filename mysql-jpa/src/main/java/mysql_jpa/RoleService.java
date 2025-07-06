package  main.java.mysql_jpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;
    public List<RoleEntity> findAll() {
        return roleRepository.findAll();
    }
    public RoleEntity findById(Long roleId) {
        return roleRepository.findById(roleId).orElse(null);
    }
    
    public RoleEntity findByRoleName(String roleName) {
        return roleRepository.findByRoleName(roleName).orElse(null);
    }

    public List<RoleEntity> findByRoleNameLike(String roleName) {
        return roleRepository.findByRoleNameLike(roleName);
    }

    public RoleEntity save(RoleEntity roleEntity) {
        return roleRepository.save(roleEntity);
    }
    public RoleEntity update(Long roleId, RoleEntity roleEntity) {
        if (roleRepository.findById(roleId).isPresent()) {
            roleEntity.setRoleId(roleId);
            return roleRepository.save(roleEntity);
        }
        return null;
    }
    public void deleteById(Long roleId) {
        roleRepository.deleteById(roleId);
    }
}
