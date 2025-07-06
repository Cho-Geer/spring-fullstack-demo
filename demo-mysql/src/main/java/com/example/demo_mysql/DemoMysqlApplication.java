package com.example.demo_mysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class DemoMysqlApplication implements CommandLineRunner {
    @Autowired
    private DataSource dataSource;
	@Autowired
    private RoleService roleService;

    @Override
    public void run(String... args) throws Exception{
        System.out.println("\n=== 开始测试数据库操作 ===");

        // 创建测试数据前先检查是否已存在
        createRoleIfNotExists("TestRole1", "Test Role 1");
        createRoleIfNotExists("TestRole2", "Test Role 2");
        createRoleIfNotExists("TestRole3", "Test Role 3");

        // 获取所有角色
        List<Role> roles = roleService.getAllRoles();
        System.out.println("获取到的角色数量: " + roles.size());
        for (Role role : roles) {
            System.out.println("角色ID: " + role.getRoleId() + ", 角色名: " + role.getRoleName() + ", 描述: " + role.getDescription());
        }
        System.out.println("\n=== 数据库操作测试完成 ===");
    }

    private void createRoleIfNotExists(String roleName, String description) {
        try {
            // 检查角色是否已存在
            if (roleService.findByRoleName(roleName) == null) {
                Role role = new Role();
                role.setRoleName(roleName);
                role.setDescription(description);
                roleService.savRole(role);
                System.out.println("创建新角色: " + roleName);
            } else {
                System.out.println("角色已存在: " + roleName);
            }
        } catch (Exception e) {
            System.out.println("处理角色 " + roleName + " 时出错: " + e.getMessage());
        }
    }

	public static void main(String[] args) {
		SpringApplication.run(DemoMysqlApplication.class, args);
	}

	@PostConstruct
    public void checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("数据库连接成功！");
            System.out.println("数据库URL: " + connection.getMetaData().getURL());
            System.out.println("数据库用户: " + connection.getMetaData().getUserName());
        } catch (SQLException e) {
            System.err.println("数据库连接失败: " + e.getMessage());
        }
    }

}
