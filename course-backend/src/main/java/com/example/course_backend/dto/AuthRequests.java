package com.example.course_backend.dto;

public class AuthRequests {

    public static class LoginRequest {
        private String email;
        private String password;
        public LoginRequest() {}
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegisterCustomerRequest {
        private String name;
        private String email;
        private String password;
        public RegisterCustomerRequest() {}
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegisterMentorRequest {
        private String name;
        private String email;
        private String password;
        private String bio;
        private java.util.List<String> expertise;
        public RegisterMentorRequest() {}
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getBio() { return bio; }
        public void setBio(String bio) { this.bio = bio; }
        public java.util.List<String> getExpertise() { return expertise; }
        public void setExpertise(java.util.List<String> expertise) { this.expertise = expertise; }
    }
}
