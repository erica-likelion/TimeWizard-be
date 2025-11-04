package timeWizard.bilnut.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import timeWizard.bilnut.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
