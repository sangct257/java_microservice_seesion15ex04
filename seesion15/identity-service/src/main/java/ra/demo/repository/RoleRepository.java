package ra.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ra.demo.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
}
