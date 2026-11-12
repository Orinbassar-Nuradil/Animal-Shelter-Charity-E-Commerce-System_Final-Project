package com.pawhaven.adoption;

import jakarta.validation.constraints.*;

public class AdoptionForm {
    @NotBlank(message = "Enter your name.")
    @Size(max = 80)
    private String applicantName;

    @NotBlank(message = "Enter your email.")
    @Email(message = "Enter a valid email address.")
    @Size(max = 254)
    private String email;

    @NotBlank(message = "Enter a phone number.")
    @Size(max = 32)
    @Pattern(regexp = "(?=(?:[^0-9]*[0-9]){7,15}[^0-9]*$)[+0-9() .-]+", message = "Use 7–15 digits and optional telephone punctuation.")
    private String phone;

    @NotBlank(message = "Tell us about the home you can offer.")
    @Size(max = 1000)
    private String housing;

    @NotBlank(message = "Tell us why you would like to adopt.")
    @Size(max = 1000)
    private String reason;

    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getHousing() { return housing; }
    public void setHousing(String housing) { this.housing = housing; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

}
