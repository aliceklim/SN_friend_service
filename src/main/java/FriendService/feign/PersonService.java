package FriendService.feign;

import dto.userDto.PersonDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import security.TokenAuthentication;

import javax.validation.Valid;

@FeignClient("person-service/api/v1/account")
public interface PersonService {

    @GetMapping("/{email}")
    PersonDTO getPersonDTOByEmail (@PathVariable(name = "email") String email);

    @GetMapping("/info/{id}")
    PersonDTO getPersonById (@PathVariable(name = "id") Long id);

    @GetMapping("/me")
    PersonDTO getMyAccount();




}
