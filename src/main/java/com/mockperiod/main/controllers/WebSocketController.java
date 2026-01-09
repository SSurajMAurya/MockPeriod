//package com.mockperiod.main.controllers;
//
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.mockperiod.main.serviceImpl.WebSocketService;
//
//import lombok.RequiredArgsConstructor;
//
//@RestController
//@RequestMapping("/api/notifications")
//@RequiredArgsConstructor
//public class WebSocketController {
//
//    private final WebSocketService notificationService;
//
//   
//
//    @PostMapping("/send-to-user")
//    public ResponseEntity<String> sendToUser(@RequestBody UserNotificationRequest request) {
//        NotificationMessage message = new NotificationMessage(
//            "INFO", 
//            request.getTitle(), 
//            request.getMessage()
//        );
//        
//        notificationService.sendToUser(request.getUsername(), message);
//        return ResponseEntity.ok("Notification sent to user: " + request.getUsername());
//    }
//
//    @PostMapping("/send-by-criteria")
//    public ResponseEntity<String> sendByCriteria(@RequestBody CriteriaNotificationRequest request) {
//        NotificationCriteria criteria = new NotificationCriteria();
//        criteria.setDepartment(request.getDepartment());
//        criteria.setRole(request.getRole());
//        
//        NotificationMessage message = new NotificationMessage(
//            "ALERT", 
//            request.getTitle(), 
//            request.getMessage()
//        );
//        
//        notificationService.sendToUsersByCriteria(criteria, message);
//        return ResponseEntity.ok("Notification sent to users matching criteria");
//    }
//}
//
//// Request DTOs
//class UserNotificationRequest {
//    private String username;
//    private String title;
//    private String message;
//    // getters, setters
//}
//
//class CriteriaNotificationRequest {
//    private String department;
//    private String role;
//    private String title;
//    private String message;
//    // getters, setters
//}
