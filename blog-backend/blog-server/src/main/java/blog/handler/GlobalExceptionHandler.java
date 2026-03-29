package blog.handler;

import blog.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler
{
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Result<Void>> handleAuthenticationException(AuthenticationException exception)
    {
        String message = exception.getMessage();
        if ("Bad credentials".equalsIgnoreCase(message) || "UserDetailsService returned null, which is an interface contract violation".equalsIgnoreCase(message)) {
            message = "用户名或密码错误";
        }
        if (message == null || message.isBlank()) {
            message = "认证失败，请重新登录";
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.error(401, message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgumentException(IllegalArgumentException exception)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.error(400, exception.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Result<Void>> handleIllegalStateException(IllegalStateException exception)
    {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Result.error(503, exception.getMessage()));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Result<Void>> handleDuplicateKeyException(DuplicateKeyException exception)
    {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Result.error(400, "数据已存在，请更换后重试"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception exception)
    {
        log.error("Unhandled exception", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.error(500, "服务器异常，请稍后重试"));
    }
}
