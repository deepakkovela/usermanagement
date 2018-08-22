package com.thd.ob.usermanagement.controller;

import com.thd.ob.usermanagement.domain.User;
import com.thd.ob.usermanagement.utils.SpannerUtil;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("users")
@CrossOrigin(origins = {"http://localhost:3000", "http://35.231.78.107","http://35.231.78.107:8080"},
        maxAge = 4800, allowCredentials = "false")
public class UserController {

    //private List<User> userList = new ArrayList<User>();

    @Autowired
    SpannerUtil spannerUtil;

    @PostMapping(path = "/print")
    public User printUser(@RequestBody User user){
        System.out.print(user);
        return user;
    }

    @GetMapping
    public String ping(){
        return LocalDateTime.now().toString();
    }

    @PostMapping(path = "/register")
    public String register(@RequestBody User user){

        String exUser = spannerUtil.readbyId(user.getUser());

        if(exUser != null){
            return "Error: User exists already !";
        }

        spannerUtil.insertUser(user.getUser(),user.getPassword());

        return "Success: User created !";
    }

    @PostMapping(path = "/login")
    public String login(@RequestBody User user){

        String exUser = spannerUtil.readbyId(user.getUser());

        if(exUser != null && exUser.contains(user.getPassword())){
            return "Success: Welcome " + user.getUser();
        }

        return "Error: Invalid Credentials !";
    }

    @PostMapping
    public User insert(@RequestBody User user){
        System.out.print(user);
        spannerUtil.insertUser(user.getUser(),user.getPassword());
        return user;
    }

    @DeleteMapping(path = "/{userId}")
    public String delete(@PathVariable String userId){
        spannerUtil.delete(userId);
        return "Deleted !";
    }

    @GetMapping(path = "/{userId}")
    public String printUser(@PathVariable String userId){
        String user = spannerUtil.readbyId(userId);
        System.out.print(user);
        return user;
    }

    @GetMapping(path = "/all")
    public String findAll(){
        JSONArray users = spannerUtil.query();
        return users.toString();
    }



}
