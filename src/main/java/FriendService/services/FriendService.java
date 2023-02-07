package FriendService.services;

import FriendService.constants.FriendshipStatusCode;
import FriendService.dto.FriendDTO;
import FriendService.feign.PersonService;
import FriendService.mappers.FriendshipMapper;
import FriendService.repositories.FriendSpecification;
import dto.userDto.PersonDTO;
import FriendService.exceptions.FriendshipException;
import FriendService.model.Friendship;
import FriendService.model.FriendshipStatus;
import FriendService.repositories.FriendshipRepository;
import FriendService.repositories.FriendshipStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static FriendService.constants.FriendshipStatusCode.*;


@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendshipRepository friendshipRepository;
    private final FriendshipStatusRepository friendshipStatusRepository;
    private final PersonService personService;
    private final FriendshipMapper friendshipMapper;

    public void approveFriendRequest(Long id) {
        friendshipRepository.findById(id)
                .flatMap(f -> friendshipStatusRepository.findById(f.getStatusId()))
                .filter(s -> s.getStatusCode() == REQUEST)
                .or(() -> {
                    throw new FriendshipException("The user hasn't a friend request", HttpStatus.BAD_REQUEST);
                })
                .map(s -> {
                    s.setStatusCode(FRIEND);
                    return s;
                })
                .ifPresent(friendshipStatusRepository::save);
    }

    public void blockFriend(Long id) {
        friendshipRepository.findById(id)
                .flatMap(f -> friendshipStatusRepository.findById(f.getStatusId()))
                .or(() -> {
                    throw new FriendshipException("The user doesn't exist", HttpStatus.BAD_REQUEST);
                })
                .map(s -> {
                    s.setStatusCode(BLOCKED);
                    return s;
                })
                .ifPresent(friendshipStatusRepository::save);
    }

    public void subscribe(Long id) {
        friendshipRepository.findById(id)
                .flatMap(f -> friendshipStatusRepository.findById(f.getStatusId()))
                .map(s -> {
                    s.setStatusCode(SUBSCRIBED);
                    return s;
                })
                .ifPresent(friendshipStatusRepository::save);
    }

    public void delete(Long id) {
        Friendship friendship = friendshipRepository
                .findById(id)
                .orElseThrow(() -> new FriendshipException("Friendship request with the id doesn't exist", HttpStatus.BAD_REQUEST));
        FriendshipStatus friendshipStatus = friendshipStatusRepository
                .findById(friendship.getStatusId())
                .orElseThrow(() -> new FriendshipException("Friendship status with the id doesn't exist", HttpStatus.BAD_REQUEST));
        friendshipRepository.delete(friendship);
        friendshipStatusRepository.delete(friendshipStatus);
    }

    @Transactional
    public void sendFriendshipRequest(Long id) {
        PersonDTO dstUser = personService.getPersonById(id);
        FriendshipStatus friendshipStatus = FriendshipStatus.builder()
                .time(new Date())
                .name(dstUser.getFirstName() + " " + dstUser.getLastName())
                .statusCode(REQUEST)
                .build();
        friendshipStatusRepository.save(friendshipStatus);

        Friendship friendship = Friendship.builder()
                .statusId(friendshipStatus.getId())
                .dstPersonId(id)
                .srcPersonId(personService.getMyAccount().getId())
                .build();
        friendshipRepository.save(friendship);
    }

    public List<FriendDTO> getAllFriends() {
        return friendshipRepository.findBySrcPersonId(personService.getMyAccount().getId())
                .stream()
                .filter(friendship ->
                        friendshipStatusRepository.findById(friendship.getStatusId())
                                .get().getStatusCode().equals(FriendshipStatusCode.FRIEND))
                .map(friendship -> {
                    PersonDTO friend = personService.getPersonById(friendship.getDstPersonId());
                    return new FriendDTO(
                            friend.getId(),
                            friend.getPhoto(),
                            friend.getFirstName(),
                            friend.getLastName(),
                            friend.getBirthDay(),
                            friend.getIsOnline(),
                            friendshipStatusRepository.findById(friendship.getStatusId()).get().getStatusCode());
                })
                .collect(Collectors.toList());

    }

    public Long getFriendCount(){
        return friendshipRepository.findBySrcPersonId(personService.getMyAccount().getId())
                .stream()
                .filter( friendship ->
                        friendshipStatusRepository.findById(friendship.getStatusId())
                                .get().getStatusCode().equals(FriendshipStatusCode.FRIEND))
                .count();
    }

    public List<Long> getBlockFriendId(){
        return friendshipRepository.findBySrcPersonId(personService.getMyAccount().getId())
                .stream()
                .filter(friendship ->
                        friendshipStatusRepository.findById(friendship.getStatusId())
                                .map(FriendshipStatus::getStatusCode)
                                .filter(statusCode -> statusCode.equals(FriendshipStatusCode.BLOCKED))
                                .isPresent())
                .map(Friendship::getDstPersonId)
                .collect(Collectors.toList());

    }

    public List<Long> getFriendId(){
        return friendshipRepository.findBySrcPersonId(personService.getMyAccount().getId())
                .stream()
                .filter(friendship ->
                        friendshipStatusRepository.findById(friendship.getStatusId())
                                .map(FriendshipStatus::getStatusCode)
                                .filter(statusCode -> statusCode.equals(FRIEND))
                                .isPresent())
                .map(Friendship::getDstPersonId)
                .collect(Collectors.toList());
    }

    public FriendDTO getFriendById(Long id){

        PersonDTO dstUser = personService.getPersonById(id);
        return friendshipRepository.findBySrcPersonId(personService.getMyAccount().getId())
                .stream()
                .filter(friendship -> friendship.getDstPersonId().equals(dstUser.getId()) &&
                        friendshipStatusRepository.findById(friendship.getStatusId())
                                .get().getStatusCode().equals(FriendshipStatusCode.FRIEND))
                .findFirst()
                .map(friendship -> {
                    return new FriendDTO(
                            dstUser.getId(),
                            dstUser.getPhoto(),
                            dstUser.getFirstName(),
                            dstUser.getLastName(),
                            dstUser.getBirthDay(),
                            dstUser.getIsOnline(),
                            friendshipStatusRepository.findById(friendship.getStatusId()).get().getStatusCode());
                }).orElseThrow(() ->  new FriendshipException ("Friendship doesn't exist", HttpStatus.BAD_REQUEST));

    }

    public Page<FriendshipStatus> find( Integer page, String firstName, String lastName) {

        Specification<FriendshipStatus> specification = Specification.where(null);
        if (firstName != null) {
            specification = specification.and(FriendSpecification.firstNameLike(firstName));
        }

        if (lastName != null) {
            specification = specification.and(FriendSpecification.lastNameLike(lastName));
        }

         List<FriendDTO> list = getAllFriends();

        int start = (page - 1) * 5;
        int end = Math.min(start + 5, list.size());
        Page<FriendshipStatus> pageOfFriends = new PageImpl<>(list.subList(start, end),
                PageRequest.of(page - 1, 5), list.size());


        return friendshipStatusRepository.findAll(specification, PageRequest.of(page - 1, 5)).


    }



    }



