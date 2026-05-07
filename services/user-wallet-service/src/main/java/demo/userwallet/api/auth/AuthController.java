package demo.userwallet.api.auth;

import demo.userwallet.application.AuthService;
import demo.userwallet.api.auth.AuthResponses.LoginResponse;
import demo.userwallet.api.auth.AuthResponses.RegisterResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public RegisterResponse register(@Valid @RequestBody RegisterRequest req) {
    var userId = authService.register(req.username().trim(), req.password());
    return new RegisterResponse(userId);
  }

  @PostMapping("/login")
  public LoginResponse login(@Valid @RequestBody LoginRequest req) {
    var token = authService.login(req.username().trim(), req.password());
    return new LoginResponse(token, "Bearer");
  }
}

