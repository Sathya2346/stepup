package com.stepup.controller;

import com.stepup.model.User;
import com.stepup.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String name,
            @RequestParam String mobileNumber,
            @RequestParam String address,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            HttpServletRequest request,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) {
            return "redirect:/login";
        }

        // Trim all inputs
        name = name.trim();
        mobileNumber = mobileNumber.trim();
        address = address.trim();

        // Server-side validation
        // Name: letters and spaces only, minimum 4 characters
        if (!name.matches("^[A-Za-z\\s]{4,}$")) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Name must be at least 4 characters and contain only letters and spaces.");
            return redirectBack(request);
        }

        // Phone: exactly 10 digits
        if (!mobileNumber.matches("^[0-9]{10}$")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phone number must be exactly 10 digits.");
            return redirectBack(request);
        }

        // Address: reject dangerous special characters
        if (address != null && !address.isEmpty() && address.matches(".*[@#$%^&*!~`{}|\\\\<>].*")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Address contains invalid special characters.");
            return redirectBack(request);
        }

        User user = userRepository.findById(sessionUser.getId()).orElse(null);
        if (user != null) {
            user.setName(name);
            user.setMobileNumber(mobileNumber);
            user.setAddress(address);

            // Handle profile picture upload
            if (profileImage != null && !profileImage.isEmpty()) {
                try {
                    // Validate file type
                    String contentType = profileImage.getContentType();
                    if (contentType == null || !contentType.startsWith("image/")) {
                        redirectAttributes.addFlashAttribute("errorMessage",
                                "Only image files (JPG, PNG, GIF) are allowed.");
                        return redirectBack(request);
                    }

                    // Validate file size (5MB max)
                    if (profileImage.getSize() > 5 * 1024 * 1024) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Image size must be less than 5MB.");
                        return redirectBack(request);
                    }

                    // Create upload directory if it doesn't exist
                    Path profileUploadDir = Paths.get(uploadDir, "profiles").toAbsolutePath().normalize();
                    Files.createDirectories(profileUploadDir);

                    // Generate unique filename
                    String originalFilename = profileImage.getOriginalFilename();
                    String extension = "";
                    if (originalFilename != null && originalFilename.contains(".")) {
                        extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    }
                    String newFilename = "profile_" + user.getId() + "_" + UUID.randomUUID().toString().substring(0, 8)
                            + extension;

                    // Save the file
                    Path targetPath = profileUploadDir.resolve(newFilename);
                    Files.copy(profileImage.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                    // Update profile picture URL
                    user.setProfilePicture("/uploads/profiles/" + newFilename);

                } catch (IOException e) {
                    e.printStackTrace();
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Failed to upload profile picture. Please try again.");
                    return redirectBack(request);
                }
            }

            userRepository.save(user);

            // Sync session
            session.setAttribute("user", user);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        }

        return redirectBack(request);
    }

    private String redirectBack(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }
}
