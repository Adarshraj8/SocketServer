package UberBackend.UberSocketServer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RoomController {

	@GetMapping("/room/{room}")
    public String getRoomPage(@PathVariable String room, Model model) {
        model.addAttribute("roomId", room);
        return "chatroom-view"; // This resolves to chatroom.html in /templates
    }
}
