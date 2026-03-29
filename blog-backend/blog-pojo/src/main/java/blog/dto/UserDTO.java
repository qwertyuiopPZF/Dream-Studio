package blog.dto;

import lombok.*;

@Data
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class UserDTO
{
    private  String username;

    private  String password;
}
