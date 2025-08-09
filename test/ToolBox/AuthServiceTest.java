package ToolBox;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    @Test
    public void validCredentialsReturnTrue() {
        assertTrue(AuthService.authenticate("user1", "12345"));
    }

    @Test
    public void invalidCredentialsReturnFalse() {
        assertFalse(AuthService.authenticate("user1", "wrong"));
    }
}
