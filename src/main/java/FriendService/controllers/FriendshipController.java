package FriendService.controllers;

import FriendService.dto.FriendDTO;
import FriendService.services.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
@Tag(name="Friend Service", description="Работа с сервисом друзей")
public class FriendshipController {

    private final FriendService friendService;

    @PutMapping("/{id}/approve")
    @Operation(summary = "Подтверждение дружбы")
    public void approveFriendRequest(@PathVariable Long id){
        friendService.approveFriendRequest(id);
    }

    @PutMapping("/block/{id}")
    @Operation(summary = "Блокировка друга")
    public void blockFriend(@PathVariable Long id){
        friendService.blockFriend(id);
    }

    @PostMapping("/{id}/request")
    @Operation(summary = "Добавление друга")
    public void sendFriendshipRequest(@PathVariable Long id){
        friendService.sendFriendshipRequest(id);
    }

    @PostMapping("/subscribe/{id}")
    @Operation(summary = "Подписка")
    public void subscribe(@PathVariable Long id){
        friendService.subscribe(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удаление друга")
    public void delete(@PathVariable Long id){
        friendService.delete(id);
    }


  @GetMapping
  @ResponseBody
 @Operation(summary = "Получение всех друзей")
  public List<FriendDTO> getAllFriends(){
       return friendService.getAllFriends();
  }

    @GetMapping("{accountId}")
    @Operation(summary = "Получение друга по id")
    public FriendDTO getFriendById(@PathVariable Long accountId){
        return friendService.getFriendById(accountId);
    }

    @GetMapping("/ids")
    @Operation(summary = "Получение id друзей")
    public List<Long> getFriendId(){
        return friendService.getFriendId();
    }

    @GetMapping("/count")
    @Operation(summary = "Получение количества друзей")
    public Long getFriendCount(){
        return friendService.getFriendCount();
    }

    @GetMapping("/ids/block")
    @Operation(summary = "Получение id заблокированных друзей")
    public List<Long> getBlockFriendId(){
        return friendService.getBlockFriendId();
    }

    @GetMapping("/filter")
    @Operation(summary = "Получение друзей с помощью фильтра")
    public Page<FriendDTO> findAllFriends(
            @RequestParam(name = "p", defaultValue = "1") Integer page,
            @RequestParam(name = "firstName", required = false) String firstName,
            @RequestParam(name = "lastName", required = false) String lastName
           ) {
        if (page < 1) {
            page = 1;
        }
        return friendService.find(page, firstName, lastName);
    }


}
