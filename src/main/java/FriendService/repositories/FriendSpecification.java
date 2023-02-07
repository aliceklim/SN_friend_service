package FriendService.repositories;

import FriendService.dto.FriendDTO;
import FriendService.model.FriendshipStatus;
import org.springframework.data.jpa.domain.Specification;


public class FriendSpecification {

    public static Specification<FriendshipStatus> firstNameLike (String firstName) {
        return (root, criteriaQuery, criteriaBuilder)
                -> criteriaBuilder.like(root.get("firstName"), String.format("%%%s%%", firstName));

    }

    public static Specification<FriendshipStatus> lastNameLike (String lastName) {
        return (root, criteriaQuery, criteriaBuilder)
                -> criteriaBuilder.like(root.get("lastName"), String.format("%%%s%%", lastName));

    }


}

